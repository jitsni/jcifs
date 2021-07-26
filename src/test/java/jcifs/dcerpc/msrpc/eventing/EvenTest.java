package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.*;
import jcifs.dcerpc.msrpc.eventlog;
import jcifs.smb.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

public class EvenTest {

    private DcerpcHandle rpcHandle;
    private rpc.policy_handle policyHandle;

    public static void main(String ... args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EvenTest properties-file");
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

        EvenTest eventLogTest = new EvenTest();
        eventLogTest.connect(hostname, domain, user, password);
    }

    private void connect(String hostname, String domain, String user, String password) throws Exception {
        try {
            openRPCConnection(hostname, domain, user, password);
            openEventLog(hostname);

            read();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeEventLog();
            closeRPCConnection();
        }
    }

    private void read() {
        long lastOffset = 0;
        while (true) {
            try {
                lastOffset = readEvents(lastOffset);
                Thread.sleep(5000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private long readEvents(long lastRead) throws Exception {
        long oldestRecordID = retrieveOldestRecordID();
        long noOfRecords = retrieveTotalRecords();
        System.out.printf("========= from = %d to = %d =============\n", lastRead, oldestRecordID + noOfRecords);

        lastRead = Math.max(lastRead, retrieveOldestRecordID());

        while (lastRead < oldestRecordID + noOfRecords) {
            eventlog.EventLogReadEventLog re =
                    new eventlog.EventLogReadEventLog(policyHandle,
                            eventlog.EVENTLOG_SEEK_READ | eventlog.EVENTLOG_FORWARDS_READ,
                            (int) lastRead, 32768);

            rpcHandle.sendrecv(re);
            checkNtStatus(re.retval);
            if (re.entries != null) {
                for (Object o : re.entries) {
                    eventlog.EventlogRecord entry = (eventlog.EventlogRecord) o;
                    if (entry.event_id == 4624) {
                        System.out.printf("LOGON %d source = %s computer=%s strings=%s\n",
                                entry.event_type, entry.source_name, entry.computer_name, Arrays.toString(entry.strings));
                    } else if (entry.event_id == 4634) {
                        System.out.printf("LOGOFF %d source = %s computer=%s strings=%s\n",
                                entry.event_type, entry.source_name, entry.computer_name, Arrays.toString(entry.strings));
                    } else {
                        System.out.printf("OTHER %d source = %s computer=%s strings=%s\n",
                                entry.event_type, entry.source_name, entry.computer_name, Arrays.toString(entry.strings));
                    }
                }

                lastRead += re.entries.size();
                System.out.printf("lastRead = %d last one = %d\n", lastRead, oldestRecordID + noOfRecords);
            }

        }

        return lastRead;
    }

    private void openRPCConnection(String hostname, String domain, String user, String password) throws Exception {
        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, password);
        rpcHandle = DcerpcHandle.getHandle("ncacn_np:" + hostname + "[\\PIPE\\eventlog]", auth);
    }

    private void closeRPCConnection() throws IOException {
        rpcHandle.close();
    }

    private void openEventLog(String hostname) throws Exception {
        eventlog.EventLogOpenEventLog open = new eventlog.EventLogOpenEventLog(
                new UnicodeString("Security", false),
                new UnicodeString(hostname, false));

        rpcHandle.sendrecv(open);
        checkNtStatus(open.retval);
        policyHandle = open.handle;
    }

    private void closeEventLog() throws Exception  {
        eventlog.EventLogCloseEventLog ce = new eventlog.EventLogCloseEventLog(policyHandle);
        rpcHandle.sendrecv(ce);
        checkNtStatus(ce.retval);
    }

//    private long getLastRecordNumber() throws Exception {
//        return retrieveOldestRecordID() + retrieveTotalRecords() - 1;
//    }

    private long retrieveOldestRecordID() throws IOException {
        eventlog.EventLogGetOldestEntry go = new eventlog.EventLogGetOldestEntry(policyHandle);

        rpcHandle.sendrecv(go);
        checkNtStatus(go.retval);
        return go.oldestEntryNumber & 0xFFFFFFFFL;
    }

    private long retrieveTotalRecords() throws IOException {
        eventlog.EventLogGetNumRecords gn = new eventlog.EventLogGetNumRecords(policyHandle);

        rpcHandle.sendrecv(gn);
        checkNtStatus(gn.retval);

        return gn.numberOfRecords & 0xFFFFFFFFL;
    }

    private void checkNtStatus(int nt_status) {
        // Base on nt_status to throw out exception.
        if (nt_status != NtStatus.NT_STATUS_OK) {
            throw new RuntimeException("Incorrect status = " + nt_status);
        }
    }

}
