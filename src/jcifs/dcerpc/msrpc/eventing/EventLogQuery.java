package jcifs.dcerpc.msrpc.eventing;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogQuery {
    enum PathType {
        LogName,        // A path parameter contains the name of the event log
        FilePath        // A path parameter contains the file system path to an event log file
    }

    EventLogQuery(String path, PathType pathType, String query) {

    }

    EventLogQuery(String path, PathType pathType, String query, EventLogSession session, boolean reverseDirection) {

    }

}
