package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.Auth3;
import jcifs.dcerpc.DcerpcHandle;
import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.DcerpcTcpHandle;
import jcifs.dcerpc.msrpc.NtlmSecurityProvider;
import jcifs.dcerpc.msrpc.eventing.EventLogQuery;
import jcifs.dcerpc.msrpc.eventing.EventLogRecordWritten;
import jcifs.dcerpc.msrpc.eventing.EventLogSession;
import jcifs.smb.NtlmPasswordAuthentication;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketTimeoutException;

/*
 * Allows you to subscribe to incoming events. Each time a desired event is published to an event log,
 * the EventRecordWritten event is raised, and the method that handles this event will be executed.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogWatcher implements Closeable {
    private final EventLogQuery logQuery;
    private final EventLogSession session;
    private final EventLogRecordWritten eventCallback;

    private DcerpcTcpHandle pullHandle;
    private DcerpcTcpHandle waitHandle;


    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query.
     *
     * @param logQuery
     */
    public EventLogWatcher(EventLogQuery logQuery, EventLogSession session, EventLogRecordWritten eventCallback) {
        this.logQuery = logQuery;
        this.session = session;
        this.eventCallback = eventCallback;
    }

    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query
     * and a bookmark that is used as starting position for the query.
     *
     *
     *
    public EventLogWatcher(EventLogQuery logQuery, EventBookmark bookmark) {

    }*/

    public void start() {

    }

    private static void pullUsingTwoConnections(DcerpcHandle rpcHandle, String hostname, NtlmPasswordAuthentication auth) throws Exception {
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

        DcerpcHandle waitHandle = null;

        while (true) {
            while (true) {
                if (waitHandle == null) {
                    waitHandle = DcerpcHandle.getHandle("ncacn_ip_tcp:" + hostname + "[mseven6]", auth);
                    waitHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth));
                    waitHandle.setAssocGroup(rpcHandle.getAssocGroup());           // Associates TCP connections
                    waitHandle.bind();
                    DcerpcMessage auth3 = new Auth3();
                    waitHandle.send(auth3);
                }

                even6.EvtRpcRemoteSubscriptionWaitAsync wait = new even6.EvtRpcRemoteSubscriptionWaitAsync(
                        subscription.handle);
                try {
                    waitHandle.sendrecv(wait);
                    assert wait.retVal == 0;
                    break;
                } catch (SocketTimeoutException se) {
                    waitHandle.close();
                    waitHandle = null;
                }
            }


            even6.EvtRpcRemoteSubscriptionNext pull = new even6.EvtRpcRemoteSubscriptionNext(
                    subscription.handle, numRequestedRecords, timeout, 0);
            rpcHandle.sendrecv(pull);

            System.out.println(pull);
            for(int i=0; i < pull.numActualRecords; i++) {
                EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                System.out.println("\t" + record);
            }
        }

    }

    @Override
    public void close() throws IOException {
        try {
            if (pullHandle != null) {
                pullHandle.close();
            }
        } catch (IOException ioe) {
            // ignore ioe
        }

        try {
            if (waitHandle != null) {
                waitHandle.close();
            }
        } catch (IOException ioe) {
            // ignore ioe
        }
    }
}
