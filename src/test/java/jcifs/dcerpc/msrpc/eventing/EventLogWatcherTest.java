package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.msrpc.eventing.EventLogQuery.PathType;

import javax.xml.stream.XMLStreamException;
import java.io.*;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.nio.charset.StandardCharsets.UTF_8;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogWatcherTest {

    private static volatile Exception exception;

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EventingTest properties-file");
            return;
        }
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }

        String hostname = properties.getProperty("hostname");
        String domain = properties.getProperty("domain");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        EventLogSession session = new EventLogSession(hostname, domain, user, password);

        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        EventLogQuery query = new EventLogQuery("Security", PathType.LogName, xpath, session, false);

        try(EventLogWatcher watcher = new EventLogWatcher(query, EventLogWatcherTest::eventWritten)) {
            watcher.start();
            while (true) {
                if (exception != null) {
                    throw exception;
                } else {
                    TimeUnit.SECONDS.sleep(5);
                }
            }
        }
    }

    private static void eventWritten(EventRecord record) {
        if (record.exception != null) {
            exception = record.exception;
            return;
        }
        System.out.println(LocalTime.now() + " Received event = " + record);
        BinXmlParser parser = new BinXmlParser();
        BinXmlNode node = new BinXmlNode();
        parser.parseDocument(node, record.buf, record.binXmlOffset(), record.binXmlSize);
        String xml = node.children.get(0).xml();
        //System.out.println(xml);
        try(Reader reader = new StringReader(xml)) {
            Event event = Event.event(reader);
            System.out.println("\t" + event);
        } catch (XMLStreamException|IOException e) {
            e.printStackTrace();
        }
    }

}