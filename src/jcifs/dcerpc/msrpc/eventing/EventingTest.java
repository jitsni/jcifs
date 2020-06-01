package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.Auth3;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.msrpc.NtlmSecurityProvider;
import jcifs.smb.NtlmPasswordAuthentication;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/*
 * @author Jitendra Kotamraju
 */
public class EventingTest {

    public static void main(String... args) throws Exception {
        if (args.length != 1) {
            System.out.println("java EventingTest properties-file");
        }
        Properties properties = new Properties();
        try(InputStream in = new FileInputStream(args[0])) {
            properties.load(in);
        }

        String hostname = properties.getProperty("hostname");
        String domain = properties.getProperty("domain");
        String user = properties.getProperty("user");
        String password = properties.getProperty("password");

        NtlmPasswordAuthentication auth = new NtlmPasswordAuthentication(domain, user, password);
        DcerpcHandle rpcHandle = DcerpcHandle.getHandle("ncacn_ip_tcp:" + hostname +"[mseven6]", auth);
        rpcHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth));
        rpcHandle.bind();

        DcerpcMessage auth3 = new Auth3();
        rpcHandle.send(auth3);

        pushSubscription(rpcHandle);
        // pullSubscription(rpcHandle);
        //query(rpcHandle);

    }

    static void pushSubscription(DcerpcHandle rpcHandle) throws IOException {
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;
        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                "Security", xpath, bookmark, even6.EvtSubscribeToFutureEvents);
        rpcHandle.sendrecv(subscription);

        while(true) {
            even6.EvtRpcRemoteSubscriptionNextAsync async = new even6.EvtRpcRemoteSubscriptionNextAsync(
                    subscription.handle, 5, 0);
            rpcHandle.sendrecv(async);
            System.out.println(async);
            for(int i=0; i < async.numActualRecords; i++) {
                EventRecord record = new EventRecord(async.resultBuffer, async.eventDataIndices[i], async.eventDataSizes[i]);
                System.out.println("\t" + record);

//                try(OutputStream out = new FileOutputStream("/tmp/binxml-" + i + ".xml")) {
//                    out.write(async.resultBuffer, record.binXmlOffset(), record.binXmlSize);
//                }
            }
        }
    }

    static void pullSubscription(DcerpcHandle rpcHandle) throws Exception {
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;
        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                "Security", xpath, bookmark, even6.EvtSubscribeToFutureEvents | even6.EvtSubscribePull);
        rpcHandle.sendrecv(subscription);

        int timeout = 5000;
        int numRequestedRecords = 5;
        while(true) {
            even6.EvtRpcRemoteSubscriptionNext pull = new even6.EvtRpcRemoteSubscriptionNext(
                    subscription.handle, 5, timeout, 0);
            rpcHandle.sendrecv(pull);
            System.out.println(pull);
            for(int i=0; i < pull.numActualRecords; i++) {
                EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                System.out.println("\t" + record);
            }
            if (numRequestedRecords != pull.numActualRecords) {
                Thread.sleep(5000);
            }
        }
    }

    static void query(DcerpcHandle rpcHandle) throws Exception {
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;
        even6.EvtRpcRegisterLogQuery query = new even6.EvtRpcRegisterLogQuery(
                "Security", xpath, even6.EvtQueryChannelPath | even6.EvtReadOldestToNewest);
        rpcHandle.sendrecv(query);

        int queryTimeout = 5000;
        int numRequestedRecords = 5;
        int pollInterval = 5000;
        while(true) {
            even6.EvtRpcQueryNext pull = new even6.EvtRpcQueryNext(query.handle, 5, queryTimeout, 0);
            rpcHandle.sendrecv(pull);
            System.out.println(pull);
            for(int i=0; i < pull.numActualRecords; i++) {
                EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                System.out.println("\t" + record);
            }
            if (numRequestedRecords != pull.numActualRecords) {
                Thread.sleep(pollInterval);
            }
        }
    }

}