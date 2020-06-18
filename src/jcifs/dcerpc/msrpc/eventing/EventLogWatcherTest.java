package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.msrpc.eventing.EventLogQuery.PathType;

import java.io.FileInputStream;
import java.io.InputStream;
import java.time.LocalTime;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

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
    }

}