package jcifs.dcerpc.msrpc.eventing;

public interface EventLogRecordWritten {
    void OnEntryWritten(byte[] evt);
}
