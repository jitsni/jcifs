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

/*
 * Represents a query for events in an event log and the settings that define how the query is
 * executed and on what computer the query is executed on.
 *
 * @author Jitendra Kotamraju
 */
public class EventLogQuery {
    final String path;
    final PathType pathType;
    final String query;
    final EventLogSession session;
    final boolean reverseDirection;

    /**
     * Specifies that a string contains a name of an event log or the file system path to an event log file
     */
    public enum PathType {
        LogName,        // A path parameter contains the name of the event log
        FilePath        // A path parameter contains the file system path to an event log file
    }

    /**
     * Initializes a new instance of the EventLogQuery class by specifying the target of the
     * query and the event query. The target can be an active event log or a log file.
     *
     * @param path name of the event log to query, or the path to the event log file
     * @param pathType specifies the name of an event log, or the path to an event log file
     * @param query query used to retrieve events that match the query conditions
     * @param session session that access the Event Log service on a remote computer
     * @param reverseDirection read events from the newest event in an event log to the oldest event in the log
     */
    public EventLogQuery(String path, PathType pathType, String query, EventLogSession session, boolean reverseDirection) {
        this.path = path;
        this.pathType = pathType;
        this.query = query;
        this.session = session;
        this.reverseDirection = reverseDirection;
    }

}
