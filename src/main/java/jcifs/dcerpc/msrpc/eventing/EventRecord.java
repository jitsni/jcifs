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

import jcifs.util.Encdec;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

/*
 * 2.2.17 Result Set in MS-EVEN6
 *
 * @author Jitendra Kotamraju
 */
public class EventRecord {
    public final EventLogException exception;

    public final int totalSize;
    public final int headerSize;
    public final int eventOffset;
    public final int bookmarkOffset;
    public final int binXmlSize;
    public final byte[] buf;
    public final int offset;
    public final int length;
    public final int bookmarkSize;
    public final int recordIdsOffset;
    public final long recordId;

    public final Event event;

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
        event = parseEvent();

        exception = null;
    }

    EventRecord(EventLogException exception) {
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

        event = null;
    }

    int binXmlOffset() {
        return offset + 20;
    }

    public Event event() {
        return event;
    }

    private Event parseEvent() {
        BinXmlParser parser = new BinXmlParser(buf, binXmlOffset(), binXmlSize);
        String xml = parser.xml();
        try(Reader reader = new StringReader(xml)) {
            return Event.event(reader);
        } catch (XMLStreamException | IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return exception != null ? exception.toString() : "[recordId=" + recordId + " binXmlSize=" + binXmlSize + "]";
    }
}
