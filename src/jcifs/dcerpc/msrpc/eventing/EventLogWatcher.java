package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRegisterRemoteSubscription;
import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRemoteSubscriptionNext;
import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRemoteSubscriptionWaitAsync;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static jcifs.dcerpc.msrpc.eventing.even6.*;

/*
 * Allows to subscribe to incoming events. Each time a desired event is published to an event log,
 * the EventRecordWritten event is raised, and the method that handles this event will be executed.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogWatcher implements Closeable {
    private static final AtomicInteger threadNo = new AtomicInteger();
    private static final int PULL_TIMEOUT = 60000;               // socket read timeout for fetching event messages
    private static final int WAIT_TIMEOUT = 2 * 60 * 1000;      // socket read timeout for waitAsync message
    private static final int REQUESTED_RECORDS = 5;

    private final EventLogQuery query;
    private final EventLogRecordWritten eventCallback;
    private final String bookmark;
    private final boolean readExistingEvents;
    private final int flags;

    private EvtRpcRegisterRemoteSubscription subscription;
    private volatile boolean closed;

    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query.
     * Specifies a query for the event subscription. When an event is logged that matches
     * the criteria expressed in the query, then the EventRecordWritten event is raised.
     *
     * @param query a query for the event subscription
     */
    public EventLogWatcher(EventLogQuery query, EventLogRecordWritten eventCallback) {
        this(query, null, false, eventCallback);
    }

    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query
     * and a bookmark that is used as starting position for the query.
     *
     * The bookmark (placeholder) used as a starting position in the event log or stream of events.
     * Only events that have been logged after the bookmark event will be returned by the query.
     *
     * @param query a query for the event subscription
     * @param bookmark a starting position in the event log
     */
    public EventLogWatcher(EventLogQuery query, String bookmark, EventLogRecordWritten eventCallback) {
        this(query, bookmark, false, eventCallback);
    }

    private EventLogWatcher(EventLogQuery query, String bookmark, boolean readExistingEvents,
            EventLogRecordWritten eventCallback) {
        if (bookmark != null) {
            if (query.reverseDirection) {
                throw new IllegalArgumentException();
            }
            readExistingEvents = false;
        }

        this.query = query;
        this.bookmark = bookmark;
        this.readExistingEvents = readExistingEvents;
        this.eventCallback = eventCallback;
        this.flags = flags();
    }

    public void start() throws IOException {
        subscription = new EvtRpcRegisterRemoteSubscription(query.path, query.query, null, flags);
        query.session.sendPull(subscription, PULL_TIMEOUT);
        if (subscription.retVal != 0) {
            throw new EventLogException("EvtRpcRegisterRemoteSubscription return value = " + subscription.retVal);
        }

        new Thread(this::run, "EventLogWatcher-" + threadNo.getAndIncrement()).start();
    }

    private void run() {
        int recvRecords = 0;

        try {
            while (!closed) {
                if (recvRecords != REQUESTED_RECORDS) {
                    EvtRpcRemoteSubscriptionWaitAsync wait = new EvtRpcRemoteSubscriptionWaitAsync(subscription.handle);
                    query.session.sendWait(wait, WAIT_TIMEOUT);
                    if (wait.retVal != 0) {
                        throw new EventLogException("EvtRpcRemoteSubscriptionWaitAsync return value = " + wait.retVal);
                    }
                }

                EvtRpcRemoteSubscriptionNext pull = new EvtRpcRemoteSubscriptionNext(
                        subscription.handle, REQUESTED_RECORDS, PULL_TIMEOUT, 0);
                query.session.sendPull(pull, PULL_TIMEOUT + 1000);
                if (pull.retVal != 0) {
                    throw new EventLogException("EvtRpcRemoteSubscriptionNext return value = " + pull.retVal);
                }
                recvRecords = pull.numActualRecords;

                for (int i = 0; i < pull.numActualRecords; i++) {
                    EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                    eventCallback.onEntryWritten(record);
                }
            }
        } catch (Exception e) {
            if (!closed) {
                eventCallback.onEntryWritten(new EventRecord(e));
            }
        }
    }

    private int flags() {
        int flags = EvtSubscribePull;
        if (bookmark != null) {
            flags |= EvtSubscribeStartAfterBookmark;
        } else if (readExistingEvents) {
            flags |= EvtSubscribeStartAtOldestRecord;
        } else {
            flags |= EvtSubscribeToFutureEvents;
        }

        return flags;
    }

    @Override
    public void close() {
        closed = true;
        query.session.close();
    }

}
