package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.msrpc.eventing.EventLogQuery;
import jcifs.dcerpc.msrpc.eventing.EventLogRecordWritten;
import jcifs.dcerpc.msrpc.eventing.EventLogSession;

/*
 * Allows you to subscribe to incoming events. Each time a desired event is published to an event log,
 * the EventRecordWritten event is raised, and the method that handles this event will be executed.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogWatcher {
    private final EventLogQuery logQuery;
    private final EventLogSession session;
    private final EventLogRecordWritten eventCallback;

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
}
