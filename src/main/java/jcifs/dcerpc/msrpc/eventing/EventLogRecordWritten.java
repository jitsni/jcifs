package jcifs.dcerpc.msrpc.eventing;

/*
 * A callback that gets called every time an event is published that matches the
 * criteria specified in the event query.
 *
 * @author Jitendra Kotamraju
 */
public interface EventLogRecordWritten {
    /**
     * @param record matched event
     */
    void onEntryWritten(EventRecord record);
}
