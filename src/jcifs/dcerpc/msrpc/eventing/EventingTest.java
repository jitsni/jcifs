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
        // query(rpcHandle);
        // pullSubscriptionWithWait(rpcHandle);
    }

    private static void pushSubscription(DcerpcHandle rpcHandle) throws IOException {
        String channel = "Security";
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;

        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                channel, xpath, bookmark, even6.EvtSubscribeToFutureEvents);
        rpcHandle.sendrecv(subscription);
        assert subscription.retVal == 0;
        assert subscription.queryChannelInfo[0].name.equals(channel);
        assert subscription.queryChannelInfo[0].status == 0;
        assert subscription.error.m_error == 0;
        assert subscription.error.m_subErr == 0;
        assert subscription.error.m_subErrParam == 0;

        while(true) {
            even6.EvtRpcRemoteSubscriptionNextAsync async = new even6.EvtRpcRemoteSubscriptionNextAsync(
                    subscription.handle, 5, 0);
            rpcHandle.sendrecv(async);
            assert async.retVal == 0;
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

    private static void pullSubscription(DcerpcHandle rpcHandle) throws Exception {
        String channel = "Security";
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;

        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                channel, xpath, bookmark, even6.EvtSubscribeToFutureEvents | even6.EvtSubscribePull);
        rpcHandle.sendrecv(subscription);

        assert subscription.retVal == 0;
        assert subscription.queryChannelInfo[0].name.equals(channel);
        assert subscription.queryChannelInfo[0].status == 0;
        assert subscription.error.m_error == 0;
        assert subscription.error.m_subErr == 0;
        assert subscription.error.m_subErrParam == 0;

        int timeout = 5000;
        int numRequestedRecords = 5;
        int pollInterval = 5000;

        while(true) {
            even6.EvtRpcRemoteSubscriptionNext pull = new even6.EvtRpcRemoteSubscriptionNext(
                    subscription.handle, numRequestedRecords, timeout, 0);
            rpcHandle.sendrecv(pull);
            assert pull.retVal == 0;
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

    private static void query(DcerpcHandle rpcHandle) throws Exception {
        String channel = "Application";
        String xpath = "*";

        even6.EvtRpcRegisterLogQuery query = new even6.EvtRpcRegisterLogQuery(
                channel, xpath, even6.EvtQueryChannelPath | even6.EvtReadOldestToNewest);
        rpcHandle.sendrecv(query);
        assert query.retVal == 0;
        assert query.queryChannelInfo[0].name.equals(channel);
        assert query.queryChannelInfo[0].status == 0;
        assert query.error.m_error == 0;
        assert query.error.m_subErr == 0;
        assert query.error.m_subErrParam == 0;

        int queryTimeout = 5000;
        int numRequestedRecords = 5;
        int pollInterval = 5000;
        while(true) {
            even6.EvtRpcQueryNext pull = new even6.EvtRpcQueryNext(query.handle, numRequestedRecords, queryTimeout, 0);
            rpcHandle.sendrecv(pull);
            assert pull.retVal == 0;
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

    private static void pullSubscriptionWithWait(DcerpcHandle rpcHandle) throws Exception {
        String channel = "Security";
        String xpath = "*[System[EventID=4624 or EventID=4634]]";
        String bookmark = null;

        even6.EvtRpcRegisterRemoteSubscription subscription = new even6.EvtRpcRegisterRemoteSubscription(
                channel, xpath, bookmark, even6.EvtSubscribeToFutureEvents | even6.EvtSubscribePull);
        rpcHandle.sendrecv(subscription);

        assert subscription.retVal == 0;
        assert subscription.queryChannelInfo[0].name.equals(channel);
        assert subscription.queryChannelInfo[0].status == 0;
        assert subscription.error.m_error == 0;
        assert subscription.error.m_subErr == 0;
        assert subscription.error.m_subErrParam == 0;

        int timeout = 5000;
        int numRequestedRecords = 5;

        while(true) {
            even6.EvtRpcRemoteSubscriptionNext pull = new even6.EvtRpcRemoteSubscriptionNext(
                    subscription.handle, numRequestedRecords, timeout, 0);
            rpcHandle.sendrecv(pull);
            assert pull.retVal == 0;

            System.out.println(pull);
            for(int i=0; i < pull.numActualRecords; i++) {
                EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                System.out.println("\t" + record);
            }
            if (numRequestedRecords != pull.numActualRecords) {
                even6.EvtRpcRemoteSubscriptionWaitAsync wait = new even6.EvtRpcRemoteSubscriptionWaitAsync(
                        subscription.handle);
                rpcHandle.sendrecv(wait);
                assert wait.retVal == 0;
            }
        }
    }

}