/**
 * Copyright 2020 Jitendra Kotamraju.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRegisterRemoteSubscription;
import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRemoteSubscriptionNext;
import jcifs.dcerpc.msrpc.eventing.even6.EvtRpcRemoteSubscriptionWaitAsync;

import java.io.Closeable;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static jcifs.dcerpc.msrpc.eventing.even6.*;

/**
 * Allows to subscribe to incoming events. Each time a desired event is published to an event log,
 * the EventRecordWritten event is raised, and the method that handles this event will be executed.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogWatcher implements Closeable {
    private static final AtomicInteger threadNo = new AtomicInteger();
    private static final int PULL_TIMEOUT = 15000;               // socket read timeout for fetching event messages
    private static final int WAIT_TIMEOUT = 2 * 60 * 1000;       // socket read timeout for waitAsync message
    private static final int REQUESTED_RECORDS = 5;

    private final EventLogQuery query;
    private final EventLogRecordWritten eventCallback;
    private final String bookmark;
    private final boolean readExistingEvents;
    private final int flags;

    private EvtRpcRegisterRemoteSubscription subscription;
    private int requestedRecords = REQUESTED_RECORDS;
    private int pullTimeout = PULL_TIMEOUT;
    private int waitTimeout = WAIT_TIMEOUT;
    private volatile boolean closed;
    private volatile boolean ioException;

    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query.
     * Specifies a query for the event subscription. When an event is logged that matches
     * the criteria expressed in the query, then the EventRecordWritten event is raised.
     *
     * @param query a query for the event subscription
     * @param eventCallback a callback to receive matched event
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
     * @param eventCallback a callback to receive matched event
     */
    public EventLogWatcher(EventLogQuery query, String bookmark, EventLogRecordWritten eventCallback) {
        this(query, bookmark, false, eventCallback);
    }

    /**
     * Initializes a new instance of the EventLogWatcher class by specifying an event query,
     * a bookmark that is used as starting position for the query, and a Boolean value that
     * determines whether to read the events that already exist in the event log.
     *
     * @param query a query for the event subscription
     * @param bookmark a starting position in the event log
     * @param readExistingEvents whether to read the events that already exist in the event log
     * @param eventCallback a callback to receive matched event
     */
    public EventLogWatcher(EventLogQuery query, String bookmark, boolean readExistingEvents,
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

    public void setRequestedRecords(int records) {
        this.requestedRecords = records;
    }

    public void setPullTimeout(int pullTimeout) {
        this.pullTimeout = pullTimeout;
    }

    public void setWaitTimeout(int waitTimeout) {
        this.waitTimeout = waitTimeout;
    }

    public void start() {
        new Thread(this::run, "EventLogWatcher-" + threadNo.getAndIncrement()).start();
    }

    private void run() {
        try {
            subscription = new EvtRpcRegisterRemoteSubscription(query.path, query.query, null, flags);
            query.session.sendPull(subscription, pullTimeout);
            if (subscription.retVal != 0) {
                throw new EventLogException("EvtRpcRegisterRemoteSubscription return value = " + subscription.retVal);
            }

            while (!closed) {
                EvtRpcRemoteSubscriptionWaitAsync wait = new EvtRpcRemoteSubscriptionWaitAsync(subscription.handle);
                query.session.sendWait(wait, waitTimeout);
                if (!closed && wait.retVal != 0) {
                    throw new EventLogException("EvtRpcRemoteSubscriptionWaitAsync return value = " + wait.retVal);
                }
                pullEvents();
            }
        } catch (Exception e) {
            if (e instanceof IOException) {
                ioException = true;
            }
            if (!closed) {
                EventLogException ee = e instanceof EventLogException ? (EventLogException) e : new EventLogException(e);
                eventCallback.onEntryWritten(new EventRecord(ee));
            }
        }
    }

    /*
     * pull loop is triggered by
     *     1. EvtRpcRemoteSubscriptionWaitAsync unblocks when there are new events
     *     2. TODO (may be a periodic timer). Even if wait async TCP connection is stuck,
     *        the events can still be pulled (though less frequently). Also, we can increase
     *        wait async timeout, that means less teardowns when there are no events
     *        within socket read timeout peroid.
     */
    private void pullEvents() throws IOException {
        int recvRecords = requestedRecords;

        while (!closed && recvRecords == requestedRecords) {
            EvtRpcRemoteSubscriptionNext pull = new EvtRpcRemoteSubscriptionNext(
                    subscription.handle, requestedRecords, pullTimeout, 0);
            query.session.sendPull(pull, pullTimeout + 1000);
            if (pull.retVal != 0) {
                throw new EventLogException("EvtRpcRemoteSubscriptionNext return value = " + pull.retVal);
            }
            recvRecords = pull.numActualRecords;

            for (int i = 0; i < pull.numActualRecords; i++) {
                EventRecord record = new EventRecord(pull.resultBuffer, pull.eventDataIndices[i], pull.eventDataSizes[i]);
                eventCallback.onEntryWritten(record);
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
        if (!closed) {
            closed = true;

            if (!ioException) {
                try {
                    // Cancel any pending EvtRpcRemoteSubscriptionWaitAsync or EvtRpcRemoteSubscriptionNext request
                    EvtRpcCancel cancel = new EvtRpcCancel(subscription.control);
                    query.session.sendPull(cancel, pullTimeout);


                    // Close handles
                    EvtRpcClose pull = new EvtRpcClose(subscription.handle);
                    query.session.sendPull(pull, pullTimeout);

                    EvtRpcClose wait = new EvtRpcClose(subscription.control);
                    query.session.sendPull(wait, pullTimeout);
                } catch (Exception e) {
                    // ignore
                }
            }

            query.session.close();
        }
    }

}
