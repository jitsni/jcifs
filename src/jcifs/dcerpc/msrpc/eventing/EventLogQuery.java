package jcifs.dcerpc.msrpc.eventing;

/*
 * Represents a query for events in an event log and the settings that define how the query is
 * executed and on what computer the query is executed on.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogQuery {
    private final String path;
    private final PathType pathType;
    private final String query;
    private final EventLogSession session;
    private final boolean reverseDirection;

    enum PathType {
        LogName,        // A path parameter contains the name of the event log
        FilePath        // A path parameter contains the file system path to an event log file
    }

    /**
     * Initializes a new instance of the EventLogQuery class by specifying the target of the
     * query and the event query. The target can be an active event log or a log file.
     *
     * @param path
     * @param pathType
     * @param query
     * @param session
     * @param reverseDirection
     */
    public EventLogQuery(String path, PathType pathType, String query, EventLogSession session, boolean reverseDirection) {
        this.path = path;
        this.pathType = pathType;
        this.query = query;
        this.session = session;
        this.reverseDirection = reverseDirection;
    }

}
