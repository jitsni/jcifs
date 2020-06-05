package jcifs.dcerpc.msrpc.eventing;

import jcifs.util.Encdec;

/*
 * 2.2.17 Result Set in MS-EVEN6
 *
 * @author Jitendra Kotamraju
 */
public class EventRecord {
    final Exception exception;

    final int totalSize;
    final int headerSize;
    final int eventOffset;
    final int bookmarkOffset;
    final int binXmlSize;
    final byte[] buf;
    final int offset;
    final int length;
    final int bookmarkSize;
    final int recordIdsOffset;
    final long recordId;

    EventRecord(byte[] buf, int offset, int length) {
        this.buf = buf;
        this.offset = offset;
        this.length = length;

        totalSize = Encdec.dec_uint32le(buf, offset);
        headerSize = Encdec.dec_uint32le(buf, offset + 4);
        eventOffset = Encdec.dec_uint32le(buf, offset + 8);
        bookmarkOffset = Encdec.dec_uint32le(buf, offset + 12);
        binXmlSize = Encdec.dec_uint32le(buf, offset + 16);
        bookmarkSize = Encdec.dec_uint32le(buf, offset + bookmarkOffset);
        recordIdsOffset = Encdec.dec_uint32le(buf, offset + bookmarkOffset + 20);
        recordId = Encdec.dec_uint64le(buf, offset + bookmarkOffset + recordIdsOffset);

        exception = null;
    }

    EventRecord(Exception exception) {
        this.exception = exception;

        this.buf = null;
        this.offset = 0;
        this.length = 0;

        totalSize = 0;
        headerSize = 0;
        eventOffset = 0;
        bookmarkOffset = 0;
        binXmlSize = 0;
        bookmarkSize = 0;
        recordIdsOffset = 0;
        recordId = 0;
    }

    int binXmlOffset() {
        return offset + 20;
    }

    @Override
    public String toString() {
        return exception != null ? exception.toString() : "[recordId=" + recordId + " binXmlSize=" + binXmlSize + "]";
    }
}
