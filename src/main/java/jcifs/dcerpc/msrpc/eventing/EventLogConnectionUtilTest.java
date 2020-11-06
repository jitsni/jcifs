package jcifs.dcerpc.msrpc.eventing;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogConnectionUtilTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("Usage: java EventLogUtilTest properties-file");
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

        int connectionTimeout = 5000;
        int epmTimeout = 3000;
        int pullTimeout = 3000;
        int waitTimeout = 3000;
        int totalTimeout = 7000;

        try (EventLogConnectionUtil util = new EventLogConnectionUtil(hostname, domain, user, password, "Security")) {
            util.setConnectionTimeout(connectionTimeout);
            util.setEpmTimeout(epmTimeout);
            util.setPullTimeout(pullTimeout);
            util.setWaitTimeout(waitTimeout);
            util.setRequestedRecords(1);

            EventLogConnectionUtil.ConnectionStatus connectionStatus = util.testConnection(totalTimeout, MILLISECONDS);
            System.out.println(connectionStatus);
        }
    }

}