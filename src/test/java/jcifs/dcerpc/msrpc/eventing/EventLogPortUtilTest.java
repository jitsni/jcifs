package jcifs.dcerpc.msrpc.eventing;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogPortUtilTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java EventLogPortUtilTest properties-file");
            return;
        }
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }

        String hostname = properties.getProperty("hostname");
        int connectTimeout = 5000;
        int readTimeout = 3000;

        EventLogPortUtil util = new EventLogPortUtil(hostname);
        int port = util.getPort(connectTimeout, readTimeout);
        System.out.println(port);
    }

}