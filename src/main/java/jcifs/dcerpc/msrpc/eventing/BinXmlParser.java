package jcifs.dcerpc.msrpc.eventing;

import jcifs.util.Encdec;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/*
 * A recursive-descent parser for BinXml (MS-EVEN6 2.2.12)
 *
 * @author Jitendra Kotamraju
 */
public class BinXmlParser {
    private static final byte MORE = (byte) 0x40;

    private static final byte EOF = (byte) 0x00;
    private static final byte OPEN_START_ELEMENT = (byte) 0x01;
    private static final byte CLOSE_START_ELEMENT= (byte) 0x02;
    private static final byte CLOSE_EMPTY_ELEMENT= (byte) 0x03;
    private static final byte END_ELEMENT = (byte) 0x04;
    private static final byte VALUE_TEXT = (byte) 0x05;
    private static final byte ATTRIBUTE = (byte) 0x06;
    private static final byte CDATA_SECTION = (byte) 0x07;
    private static final byte CHAR_REF = (byte) 0x08;
    private static final byte ENTITY_REF = (byte) 0x09;
    private static final byte PI_TARGET = (byte) 0x0a;
    private static final byte PI_DATA = (byte) 0x0b;
    private static final byte TEMPLATE_INSTANCE = (byte) 0x0c;
    private static final byte NORMAL_SUBSTITUTION = (byte) 0x0d;
    private static final byte OPTIONAL_SUBSTITUTION = (byte) 0x0e;
    private static final byte FRAGMENT_HEADER = (byte) 0x0f;

    static final byte NULL_TYPE = 0x00;
    private static final byte STRING_TYPE = 0x01;
    private static final byte ANSI_STRING_TYPE = 0x02;
    private static final byte INT8_TYPE = 0x03;
    private static final byte UINT8_TYPE = 0x04;
    private static final byte INT16_TYPE = 0x05;
    private static final byte UINT16_TYPE = 0x06;
    private static final byte INT32_TYPE = 0x07;
    private static final byte UINT32_TYPE = 0x08;
    private static final byte INT64_TYPE = 0x09;
    private static final byte UINT64_TYPE = 0x0a;
    private static final byte GUID_TYPE = 0x0f;
    private static final byte FILE_TIME_TYPE = 0x11;
    private static final byte SID_TYPE = 0x13;
    private static final byte HEX_INT64_TYPE = 0x15;
    private static final byte BIN_XML_TYPE = 0x21;

    private static final Instant ZERO = Instant.parse("1601-01-01T00:00:00Z");

    int inTemplate;

    // Document = 0*1Prolog Fragment 0*1Misc EOFToken
    int parseDocument(BinXmlNode element, byte[] buf, int offset, int length) {
        offset = parseFragment(element, buf, offset, length);
        if (buf[offset] == EOF) {
            offset++;                   // EOFToken
        } else {
            throw new RuntimeException("No EOF at the end of Document. offset = " + offset);
        }
        return offset;
    }

    // Fragment = 0*FragmentHeader ( Element / TemplateInstance )
    int parseFragment(BinXmlNode node, byte[] buf, int offset, int length) {
        while (buf[offset] == FRAGMENT_HEADER) {
            offset = parseFragmentHeader(buf, offset, length);
        }
        if (isElement(buf[offset])) {
            offset = parseElement(node, buf, offset, length);
        } else if (buf[offset] == TEMPLATE_INSTANCE) {
            offset = parseTemplateInstance(node, buf, offset, length);
        } else {
            throw new RuntimeException();
        }
        return offset;
    }

    // TemplateInstance = TemplateInstanceToken TemplateDef TemplateInstanceData
    private int parseTemplateInstance(BinXmlNode node, byte[] buf, int offset, int length) {
        BinXmlNode.BinXmlTemplate child = new BinXmlNode.BinXmlTemplate();
        node.addChild(child);

        offset++;
        offset = parseTemplateDef(child, buf, offset, length);
        offset = child.templateInstanceDataOffset;
        offset = parseTemplateInstanceData(child, buf, offset, length);
        return offset;
    }

    // TemplateInstanceData = ValueSpec *Value
    private int parseTemplateInstanceData(BinXmlNode.BinXmlTemplate template, byte[] buf, int offset, int length) {
        int noValues = Encdec.dec_uint32le(buf, offset);
        List<ValueEntry> entries = new ArrayList<>();
        template.setValues(entries);
        offset = parseValueSpec(entries, buf, offset, length);
        for(int i=0; i < noValues; i++) {
            offset = parseValue(entries.get(i), buf, offset, length);
        }
        return offset;
    }

    // Value = StringValue / AnsiStringValue / Int8Value / UInt8Value / Int16Value /
    //         UInt16Value / Int32Value / UInt32Value / Int64Value / UInt64Value /
    //         Real32Value / Real64Value / BoolValue / BinaryValue / GuidValue /
    //         SizeTValue / FileTimeValue / SysTimeValue / SidValue / HexInt32Value /
    //         HexInt64Value / BinXmlValue / StringArrayValue / AnsiStringArrayValue /
    //         Int8ArrayValue / UInt8ArrayValue / Int16ArrayValue / UInt16ArrayValue /
    //         Int32ArrayValue / UInt32ArrayValue / Int64ArrayValue / UInt64ArrayValue /
    //         Real32ArrayValue / Real64ArrayValue / BoolArrayValue / GuidArrayValue /
    //         SizeTArrayValue / FileTimeArrayValue / SysTimeArrayValue / SidArrayValue /
    //         HexInt32ArrayValue / HexInt64ArrayValue
    private int parseValue(ValueEntry entry, byte[] buf, int offset, int length) {
        switch (entry.valueType) {
            case NULL_TYPE :
                entry.setValue(null);
                break;
            case STRING_TYPE:
                String str = unicodeString(entry.valueByteLength/2, buf, offset, length);
                entry.setValue(str);
                break;
            case UINT8_TYPE :
                entry.setValue(buf[offset]);
                break;
            case UINT16_TYPE :
                entry.setValue(Encdec.dec_uint16le(buf, offset));
                break;
            case UINT32_TYPE :
                entry.setValue(Encdec.dec_uint32le(buf, offset));
                break;
            case UINT64_TYPE :
                entry.setValue(Encdec.dec_uint64le(buf, offset));
                break;
            case GUID_TYPE :
                String guid = guid(buf, offset, length);
                entry.setValue(guid);
                break;
            case FILE_TIME_TYPE :
                String time = fileTime(buf, offset, length);
                entry.setValue(time);
                break;
            case HEX_INT64_TYPE :
                String hex = hexInt64(buf, offset, length);
                entry.setValue(hex);
                break;
            case SID_TYPE :
                String sid = sid(buf, offset, length);
                entry.setValue(sid);
                break;
            case BIN_XML_TYPE :
                BinXmlNode node = new BinXmlNode();
                entry.setValue(node);
                int after = parseFragment(node, buf, offset, length);
                // assert after == offset + entry.valueByteLength;
                break;
            default:
                throw new UnsupportedOperationException(String.format("TODO valueType=0x%02x", entry.valueType));
        }
        offset += entry.valueByteLength;
        return offset;
    }

    // ValueSpec = NumValues *ValueSpecEntry
    private int parseValueSpec(List<ValueEntry> entries, byte[] buf, int offset, int length) {
        int noValues = Encdec.dec_uint32le(buf, offset);
        offset += 4;            // NumValues

        for(int i=0; i < noValues; i++) {
            offset = parseValueSpecEntry(entries::add, buf, offset, length);
        }
        return offset;
    }

    // ValueSpecEntry = ValueByteLength ValueType %x00
    private int parseValueSpecEntry(Consumer<ValueEntry> entry, byte[] buf, int offset, int length) {

        int valueByteLength = Encdec.dec_uint16le(buf, offset);
        ValueEntry valueEntry = new ValueEntry(valueByteLength);
        offset += 2;            // ValueByteLength
        offset = parseValueType(valueEntry::setValueType, buf, offset, length);
        offset++;               // %x00

        entry.accept(valueEntry);
        return offset;
    }

    // ValueType = NullType / StringType / AnsiStringType / Int8Type / UInt8Type / Int16Type /
    //             UInt16Type / Int32Type / UInt32Type / Int64Type / Int64Type / Real32Type /
    //             Real64Type / BoolType / BinaryType / GuidType / SizeTType / FileTimeType /
    //             SysTimeType / SidType / HexInt32Type / HexInt64Type / BinXmlType /
    //             StringArrayType / AnsiStringArrayType / Int8ArrayType / UInt8ArrayType /
    //             Int16ArrayType / UInt16ArrayType / Int32ArrayType / UInt32ArrayType/
    //             Int64ArrayType / UInt64ArrayType / Real32ArrayType / Real64ArrayType /
    //             BoolArrayType / GuidArrayType / SizeTArrayType / FileTimeArrayType /
    //             SysTimeArrayType / SidArrayType / HexInt32ArrayType / HexInt64ArrayType
    private int parseValueType(Consumer<Byte> consumer, byte[] buf, int offset, int length) {
        consumer.accept(buf[offset]);

        offset += 1;            // TODO HexInt32ArrayType = %x00 %x94 is 2 bytes ??
        return offset;
    }

    // TemplateDef = %b0 TemplateId TemplateDefByteLength 0*FragmentHeader Element EOFToken
    private int parseTemplateDef(BinXmlNode.BinXmlTemplate template, byte[] buf, int offset, int length) {
        inTemplate++;
        offset++;                   // %b0
        offset += 16;               // GUID
        int templateDefByteLength = Encdec.dec_uint32le(buf, offset);
        offset += 4;                // TemplateDefByteLength
        template.templateInstanceDataOffset = offset + templateDefByteLength;
        while (buf[offset] == FRAGMENT_HEADER) {
            offset = parseFragmentHeader(buf, offset, length);
        }
        offset = parseElement(template, buf, offset, length);
        if (buf[offset] == EOF) {
            offset++;               // EOFToken
        } else {
            throw new RuntimeException("No EOF at the end of TemplateDef. offset = " + offset);
        }
        inTemplate--;
        return offset;
    }

    // Element =
    //    ( StartElement CloseStartElementToken Content EndElementToken ) /
    //    ( StartElement CloseEmptyElementToken )
    private int parseElement(BinXmlNode node, byte[] buf, int offset, int length) {
        BinXmlNode.BinXmlElement child = new BinXmlNode.BinXmlElement();
        node.addChild(child);

        offset = parseStartElement(child, buf, offset, length);
        if (buf[offset] == CLOSE_START_ELEMENT) {
            offset++;               // CloseStartElementToken
            offset = parseContent(child, buf, offset, length);
            if (buf[offset] == END_ELEMENT) {
                offset++;
            } else {
                throw new RuntimeException("offset = " + offset);
            }
        } else if (buf[offset] == CLOSE_EMPTY_ELEMENT) {
            offset++;
        } else {
            throw new RuntimeException();
        }
        return offset;
    }

    // Content = 0*(Element / CharData / CharRef / EntityRef / CDATASection / PI)
    private int parseContent(BinXmlNode.BinXmlElement element, byte[] buf, int offset, int length) {
        while (isElement(buf[offset]) ||
                isCharData(buf[offset]) ||
                isCharRef(buf[offset]) ||
                isEntityRef(buf[offset]) ||
                isCdata(buf[offset]) ||
                buf[offset] == PI_DATA) {
            if (isElement(buf[offset])) {
                offset = parseElement(element, buf, offset, length);
            } else if (isCharData(buf[offset])) {
                offset = parseCharData(element, buf, offset, length);
            } else if (isCharRef(buf[offset])) {
                offset = parseCharRef(buf, offset, length);
            } else if (isEntityRef(buf[offset])) {
                offset = parseEntityRef(buf, offset, length);
            } else if (isCdata(buf[offset])) {
                offset = parseCdata(buf, offset, length);
            } else if (buf[offset] == PI_DATA) {
                offset = parsePiData(buf, offset, length);
            }
        }
        return offset;
    }

    private int parseCdata(byte[] buf, int offset, int length) {
        throw new UnsupportedOperationException("TODO");
    }

    private int parsePiData(byte[] buf, int offset, int length) {
        throw new UnsupportedOperationException("TODO");
    }

    private boolean isElement(byte token) {
        return token == OPEN_START_ELEMENT || token == moreToken(OPEN_START_ELEMENT);
    }

    private boolean isCharData(byte token) {
        return isValueText(token) || isSubstitution(token);
    }

    private boolean isCharRef(byte token) {
        return token == CHAR_REF || token == moreToken(CHAR_REF);
    }

    private boolean isEntityRef(byte token) {
        return token == ENTITY_REF || token == moreToken(ENTITY_REF);
    }

    private boolean isCdata(byte token) {
        return token == CDATA_SECTION || token == moreToken(CDATA_SECTION);
    }

    private boolean isSubstitution(byte token) {
        return token == NORMAL_SUBSTITUTION || token == OPTIONAL_SUBSTITUTION;
    }

    private boolean isValueText(byte token) {
        return token == VALUE_TEXT || token == moreToken(VALUE_TEXT);
    }

    // CharData = ValueText / Substitution
    private int parseCharData(BinXmlNode.BinXmlElement element, byte[] buf, int offset, int length) {
        if (isValueText(buf[offset])) {
            offset = parseValueText(element::setText, buf, offset, length);
        } else if (isSubstitution(buf[offset])) {
            offset = parseSubstitution(element::setTextSubstitution, buf, offset, length);
        } else {
            throw new RuntimeException();
        }

        return offset;
    }

    // StartElement = OpenStartElementToken 0*1DependencyId ElementByteLength Name 0*1AttributeList
    private int parseStartElement(BinXmlNode.BinXmlElement element, byte[] buf, int offset, int length) {
        boolean more = more(buf[offset]);

        offset++;                   // OpenStartElementToken
        if (inTemplate > 0) {       // in template or not
            offset += 2;            // DependencyId
        }
        offset += 4;                // ElementByteLength

        offset = parseName(element::setTag, buf, offset, length);
        if (more) {
            offset = parseAttributeList(element, buf, offset, length);
        }

        return offset;
    }

    // AttributeList = AttributeListByteLength 1*Attribute
    private int parseAttributeList(BinXmlNode.BinXmlElement element, byte[] buf, int offset, int length) {
        Encdec.dec_uint32le(buf, offset);
        offset += 4;

        boolean more;
        do {
            more = more(buf[offset]);
            offset = parseAttribute(element, buf, offset, length);
        } while (more);

        return offset;
    }

    // FragmentHeader = FragmentHeaderToken MajorVersion MinorVersion Flags
    private int parseFragmentHeader(byte[] buf, int offset, int length) {
        assert buf[offset] == FRAGMENT_HEADER;

        offset += 1;                    // FragmentHeaderToken
        offset += 1;                    // MajorVersion
        offset += 1;                    // MinorVersion
        offset += 1;                    // Flags

        return offset;
    }

    // Attribute = AttributeToken Name AttributeCharData ; Emit using Attribute Rule
    private int parseAttribute(BinXmlNode.BinXmlElement element, byte[] buf, int offset, int length) {
        offset++;                       // AttributeToken
        BinXmlNode.Attribute attribute = new BinXmlNode.Attribute();
        element.addAttribute(attribute);
        offset = parseName(attribute::setName, buf, offset, length);
        offset = parseAttributeCharData(attribute, buf, offset, length);

        return offset;
    }

    // AttributeCharData = 0*(ValueText / Substitution / CharRef / EntityRef)
    private int parseAttributeCharData(BinXmlNode.Attribute attribute, byte[] buf, int offset, int length) {
        while (isValueText(buf[offset]) ||
                isSubstitution(buf[offset]) ||
                isCharRef(buf[offset]) ||
                isEntityRef(buf[offset])) {
            if (isValueText(buf[offset])) {
                offset = parseValueText(attribute::setValue, buf, offset, length);
            } else if (isSubstitution(buf[offset])) {
                offset = parseSubstitution(attribute::setSubstitution, buf, offset, length);
            } else if (isCharRef(buf[offset])) {
                offset = parseCharRef(buf, offset, length);
            } else if (isEntityRef(buf[offset])) {
                offset = parseEntityRef(buf, offset, length);
            } else {
                throw new UnsupportedOperationException("TODO");
            }
        }
        return offset;
    }

    // Substitution = NormalSubstitution / OptionalSubstitution
    private int parseSubstitution(Consumer<BinXmlNode.Substitution> consumer, byte[] buf, int offset, int length) {
        offset = buf[offset] == NORMAL_SUBSTITUTION
                ? parseNormalSubstitution(consumer, buf, offset, length)
                : parseOptionalSubstitution(consumer, buf, offset, length);
        return offset;
    }

    // NormalSubstitution = NormalSubstitutionToken SubstitutionId ValueType
    private int parseNormalSubstitution(Consumer<BinXmlNode.Substitution> consumer, byte[] buf, int offset, int length) {
        offset++;               // NormalSubstitutionToken
        short substitutionId = Encdec.dec_uint16le(buf, offset);
        offset += 2;            // SubstitutionId

        BinXmlNode.Substitution substitution = new BinXmlNode.Substitution(false, substitutionId);
        offset = parseValueType(substitution::setValueType, buf, offset, length);
        consumer.accept(substitution);
        return offset;
    }

    // OptionalSubstitution = OptionalSubstitutionToken SubstitutionId ValueType
    private int parseOptionalSubstitution(Consumer<BinXmlNode.Substitution> consumer, byte[] buf, int offset, int length) {
        offset++;               // OptionalSubstitutionToken
        short substitutionId = Encdec.dec_uint16le(buf, offset);
        offset += 2;            // SubstitutionId

        BinXmlNode.Substitution substitution = new BinXmlNode.Substitution(true, substitutionId);
        offset = parseValueType(substitution::setValueType, buf, offset, length);
        consumer.accept(substitution);
        return offset;
    }

    // EntityRef = EntityRefToken Name
    private int parseEntityRef(byte[] buf, int offset, int length) {
        offset++;               // EntityRefToken
        offset = parseName(x -> {}, buf, offset, length);
        return offset;
    }

    // CharRef = CharRefToken WORD
    private int parseCharRef(byte[] buf, int offset, int length) {
        offset++;               // CharRefToken
        offset += 2;            // WORD
        return offset;
    }

    // Name = NameHash NameNumChars NullTerminatedUnicodeString
    private int parseName(Consumer<String> nameConsumer, byte[] buf, int offset, int length) {
        offset += 2;            // NameHash

        int noChars = Encdec.dec_uint16le(buf, offset);
        offset += 2;            // NameNumChars

        offset = parseNullTerminatedUnicodeString(nameConsumer, noChars, buf, offset, length);
        return offset;
    }

    // NullTerminatedUnicodeString = StringValue %x00 %x00
    private int parseNullTerminatedUnicodeString(Consumer<String> nameConsumer, int noChars, byte[] buf, int offset, int length) {
        String str = unicodeString(noChars, buf, offset, length);
        nameConsumer.accept(str);
        offset += 2 * noChars;
        offset += 2;            // %x00 %x00

        return offset;
    }

    // ValueText = ValueTextToken StringType LengthPrefixedUnicodeString
    private int parseValueText(Consumer<String> consumer, byte[] buf, int offset, int length) {
        offset++;               // ValueTextToken
        offset++;               // StringType
        offset = parseLengthPrefixedUnicodeString(consumer, buf, offset, length);
        return offset;
    }

    // LengthPrefixedUnicodeString = NumUnicodeChars StringValue
    private int parseLengthPrefixedUnicodeString(Consumer<String> consumer, byte[] buf, int offset, int length) {
        int noChars = Encdec.dec_uint16le(buf, offset);
        offset += 2;            // NumUnicodeChars
        String str = unicodeString(noChars, buf, offset, length);
        consumer.accept(str);
        offset += 2 * noChars;

        return offset;
    }

    private boolean more(byte b) {
        return (b & MORE) == MORE;
    }

    private byte moreToken(byte b) {
        return (byte) (b | MORE);
    }

    private String unicodeString(int noChars, byte[] buf, int offset, int length) {
        char[] chars = new char[noChars];
        for(int i=0; i < noChars; i++) {
            chars[i] = (char) Encdec.dec_uint16le(buf, offset + 2 * i);
        }
        return new String(chars);
    }

    /* MS-DTYP spec
       2.4.2.2 SID--Packet Representation

            +---------------+---------------+-------------------------------+
            |Revision (8)   | SubAuthority  |    Identifier Authority (48)  |
            |               | Count (8)     |                               |
            +---------------+-----------------------------------------------+
            |                            ...                                |
            +---------------------------------------------------------------+
            |                   SubAuthority1 (32)                          |
            +---------------------------------------------------------------+
            |                   SubAuthority2 (32)                          |
            +---------------------------------------------------------------+
            |                   ...                                         |
            +---------------------------------------------------------------+

       2.4.2.1 SID String Format Syntax
       ; If the identifier authority is < 2^32, the
       ; identifier authority is represented as a decimal
       ; number
       ; If the identifier authority is >= 2^32,
       ; the identifier authority is represented in
       ; hexadecimal
     */
    private String sid(byte[] buf, int offset, int length) {
        // byte revision = buf[offset];
        int subAuthorityCount = Byte.toUnsignedInt(buf[offset + 1]);
        // TODO assuming identifierAuthority is < 2^32
        int identifierAuthority = Encdec.dec_uint32be(buf, offset + 4);

        StringBuilder sb = new StringBuilder("S-1-").append(identifierAuthority);
        for(int i=0; i < subAuthorityCount; i++) {
            long subAuthority = Integer.toUnsignedLong(Encdec.dec_uint32le(buf, offset + 8 + 4 * i));
            sb.append("-").append(subAuthority);
        }
        return sb.toString();
    }

    private String hexInt64(byte[] buf, int offset, int length) {
        long value = Encdec.dec_uint64le(buf, offset);
        return "0x" + Long.toHexString(value);
    }

    /* MS-DTYP spec
       2.3.4.2 GUID--Packet Representation

        +---------------------------------------------------------------+
        |                       Data1 (32)                              |
        +------------------------------+--------------------------------+
        |          Data2 (16)          |           Data3 (16)           |
        +------------------------------+--------------------------------+
        |                       Data4 (64)                              |
        +---------------------------------------------------------------+
        |                   ...                                         |
        +---------------------------------------------------------------+

    */
    private String guid(byte[] buf, int offset, int length) {
        int data1 = Encdec.dec_uint32le(buf, offset);
        int data2 = Short.toUnsignedInt(Encdec.dec_uint16le(buf, offset + 4));
        int data3 = Short.toUnsignedInt(Encdec.dec_uint16le(buf, offset + 6));

        StringBuilder sb = new StringBuilder("{");
        sb.append(Integer.toHexString(data1));
        sb.append("-");
        sb.append(Integer.toHexString(data2));
        sb.append("-");
        sb.append(Integer.toHexString(data3));
        sb.append("-");
        sb.append(Integer.toHexString(Byte.toUnsignedInt(buf[offset+8])));
        sb.append(Integer.toHexString(Byte.toUnsignedInt(buf[offset+9])));
        sb.append("-");
        for(int i=10; i < 16; i++) {
            sb.append(Integer.toHexString(Byte.toUnsignedInt(buf[offset + i])));
        }
        sb.append("}");

        return sb.toString().toUpperCase();
    }

    private String fileTime(byte[] buf, int offset, int length) {
        long fileTime = Encdec.dec_uint64le(buf, offset);

        Duration duration = Duration.of(fileTime / 10, ChronoUnit.MICROS)
                .plus(fileTime % 10 * 100, ChronoUnit.NANOS);
        Instant instant = ZERO.plus(duration);
        return instant.toString();
    }

    static class ValueEntry {
        final int valueByteLength;
        byte valueType;
        Object value;

        ValueEntry(int valueByteLength) {
            this.valueByteLength = valueByteLength;
        }

        void setValue(Object value) {
            this.value = value;
        }

        void setValueType(byte valueType) {
            this.valueType = valueType;
        }

        @Override
        public String toString() {
            return String.format("(type=%02x length=%d)", valueType, valueByteLength);
        }
    }

}
