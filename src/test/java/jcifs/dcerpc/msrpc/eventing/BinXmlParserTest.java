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

import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javax.xml.bind.DatatypeConverter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/*
 * @author Jitendra Kotamraju
 */
public class BinXmlParserTest {

    // 4.4 Simple BinXml Example
    // <Event>
    // <Element1>abc</Element1>
    // <Element2> def &amp;&#60; ghi </Element2>
    // <Element3 AttrA='abc' AttrB='def&amp;&#60;ghi'/>
    // </Event>
    String xmlSimple =
        "0f01010001f2000000ba0c0500450076" +
        "0065006e0074000000020122000000b5" +
        "79080045006c0065006d0065006e0074" +
        "00310000000205010300610062006300" +
        "040144000000b679080045006c006500" +
        "6d0065006e0074003200000002450105" +
        "00200064006500660020004924fb0300" +
        "61006d0070000000483c000501050020" +
        "00670068006900200004416b000000b7" +
        "79080045006c0065006d0065006e0074" +
        "0033000000500000004690d805004100" +
        "74007400720041000000050103006100" +
        "620063000691d8050041007400740072" +
        "00420000004501030064006500660049" +
        "24fb030061006d0070000000483c0005" +
        "010300670068006900030400";

    //  <Event xmlns="'http: //schemas.microsoft.com/win/2004/08/events/event'">
    //   <System>
    //     <Provider Name="'Microsoft-Windows-Wevttest'"
    //             Guid="'{03f41308-fa7b-4fb3-98b8-c2ed0a40d1ef}'"/>
    //     <EventID>100</EventID>
    //     <Version>0</Version>
    //     <Level>1</Level>
    //     <Task>100</Task>
    //     <Opcode>1</Opcode>
    //     <Keywords>0x4000000000e00000</Keywords>
    //     <TimeCreated SystemTime="'2006-0614T21:40:16.312Z'"/>
    //     <EventRecordID>5</EventRecordID>
    //     <Correlation/>
    //     <Execution ProcessID="'2088'" ThreadID="'2464'"/>
    //     <Channel>Microsoft-Windows-Wevttest/Operational/Wevttest</Channel>
    //     <Computer>michaelm4-lh.ntdev.corp.microsoft.com</Computer>
    //     <Security UserID="'S-1-5-21-397955417-626881126-188441444-2967838'"/>
    //   </System>
    //   <UserData>
    //     <MyEvent xmlns:autons2="'http: //schemas.microsoft.com/win/2004/08/events'" xmlns='myNs'>
    //       <Property>1</Property>
    //       <Property2>2</Property2>
    //     </MyEvent>
    //   </UserData>
    // </Event>
    String xmlTemplates =
        "0f0101000c004a464ccc16dc468e80a2" +
        "dc45ea949cbdef0400000f01010041ff" +
        "ffe3040000ba0c05004500760065006e" +
        "00740000007f00000006bc0f05007800" +
        "6d006c006e0073000000050135006800" +
        "7400740070003a002f002f0073006300" +
        "680065006d00610073002e006d006900" +
        "630072006f0073006f00660074002e00" +
        "63006f006d002f00770069006e002f00" +
        "32003000300034002f00300038002f00" +
        "6500760065006e00740073002f006500" +
        "760065006e0074000201ffff24040000" +
        "6f540600530079007300740065006d00" +
        "00000241ffffc1000000f17b08005000" +
        "72006f00760069006400650072000000" +
        "a6000000464b9504004e0061006d0065" +
        "00000005011a004d006900630072006f" +
        "0073006f00660074002d00570069006e" +
        "0064006f00770073002d005700650076" +
        "00740074006500730074000629150400" +
        "47007500690064000000050126007b00" +
        "30003300660034003100330030003800" +
        "2d0066006100370062002d0034006600" +
        "620033002d0039003800620038002d00" +
        "63003200650064003000610034003000" +
        "64003100650066007d00034103003d00" +
        "0000f56107004500760065006e007400" +
        "4900440000001f0000000629da0a0051" +
        "00750061006c00690066006900650072" +
        "00730000000e040006020e0300060401" +
        "0b001a00000018090700560065007200" +
        "730069006f006e000000020e0b000404" +
        "0100001600000064ce05004c00650076" +
        "0065006c000000020e00000404010200" +
        "14000000457b04005400610073006b00" +
        "0000020e0200060401010018000000ae" +
        "1e06004f00700063006f006400650000" +
        "00020e010004040105001c0000006acf" +
        "08004b006500790077006f0072006400" +
        "73000000020e0500150441ffff400000" +
        "003b8e0b00540069006d006500430072" +
        "006500610074006500640000001f0000" +
        "00063c7b0a0053007900730074006500" +
        "6d00540069006d00650000000e060011" +
        "03010a002600000046030d0045007600" +
        "65006e0074005200650063006f007200" +
        "6400490044000000020e0a000a0441ff" +
        "ff6d000000a2f20b0043006f00720072" +
        "0065006c006100740069006f006e0000" +
        "004c000000460af10a00410063007400" +
        "69007600690074007900490044000000" +
        "0e07000f0635c51100520065006c0061" +
        "00740065006400410063007400690076" +
        "006900740079004900440000000e1200" +
        "0f0341ffff55000000b8b50900450078" +
        "00650063007500740069006f006e0000" +
        "0038000000460ad70900500072006f00" +
        "63006500730073004900440000000e08" +
        "00080685390800540068007200650061" +
        "0064004900440000000e0900080301ff" +
        "ff78000000836107004300680061006e" +
        "006e0065006c0000000205012f004d00" +
        "6900630072006f0073006f0066007400" +
        "2d00570069006e0064006f0077007300" +
        "2d005700650076007400740065007300" +
        "74002f004f0070006500720061007400" +
        "69006f006e0061006c002f0057006500" +
        "7600740074006500730074000401ffff" +
        "660000003b6e080043006f006d007000" +
        "7500740065007200000002050125006d" +
        "00690063006800610065006c006d0034" +
        "002d006c0068002e006e007400640065" +
        "0076002e0063006f00720070002e006d" +
        "006900630072006f0073006f00660074" +
        "002e0063006f006d000441ffff320000" +
        "00a02e08005300650063007500720069" +
        "007400790000001700000006664c0600" +
        "55007300650072004900440000000e0c" +
        "001303040113001c0000003544080055" +
        "00730065007200440061007400610000" +
        "00020e13002104040014000000010004" +
        "00010004000200060002000600000000" +
        "00080015000800110000000000040008" +
        "000400080008000a00010004001c0013" +
        "00000000000000000000000000000000" +
        "00000000000000000083012100010164" +
        "0064000000e000000000409cf4d636fb" +
        "8fc60128080000a00900000600000000" +
        "00000000010500000000000515000000" +
        "5951b81766725d2564633b0b1e492d00" +
        "0f0101000c00a765057a0284f0a167ab" +
        "96df090d39a75401000041ffff040100" +
        "004ec007004d0079004500760065006e" +
        "0074000000a2000000464d770e007800" +
        "6d006c006e0073003a00610075007400" +
        "6f002d006e0073003200000005012f00" +
        "68007400740070003a002f002f007300" +
        "6300680065006d00610073002e006d00" +
        "6900630072006f0073006f0066007400" +
        "2e0063006f006d002f00770069006e00" +
        "2f0032003000300034002f0030003800" +
        "2f006500760065006e007400730006bc" +
        "0f050078006d006c006e007300000005" +
        "0104006d0079004e0073000201ffff1c" +
        "000000b5db0800500072006f00700065" +
        "007200740079000000020d0000080401" +
        "ffff1e000000bd110900500072006f00" +
        "7000650072007400790032000000020d" +
        "01000804040000000000080800000000" +
        "00000000000008070000000000000808" +
        "00000000000000000000180700001000" +
        "0000500072006f007000310000001000" +
        "0000500072006f007000320000000200" +
        "00000400080004000800010000000200" +
        "00000000";

    @Test
    public void simpleBinXml() throws Exception {
        byte[] buf = DatatypeConverter.parseHexBinary(xmlSimple);
        BinXmlParser parser = new BinXmlParser(buf, 0, buf.length);
        String xml = parser.xml();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = builderFactory.newDocumentBuilder();
        Document document = db.parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        // <Element1>abc</Element1>
        String expression = "/Event/Element1/text()";
        Node tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("abc", tnode.getNodeValue());

        // <Element2> def &amp;&#60; ghi </Element2>
        // TODO /Event/Element2/text() doesn't work yet

        // <Element3 AttrA='abc' AttrB='def&amp;&#60;ghi'/>
        expression = "string(/Event/Element3/@AttrA)";
        String attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("abc", attrValue);
    }

    @Test
    public void binXmlTemplates() throws Exception {
        byte[] buf = DatatypeConverter.parseHexBinary(xmlTemplates);
        BinXmlParser parser = new BinXmlParser(buf, 0, buf.length);
        String xml = parser.xml();

        DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder db = builderFactory.newDocumentBuilder();
        Document document = db.parse(new ByteArrayInputStream(xml.getBytes()));
        XPath xPath = XPathFactory.newInstance().newXPath();

        // <Event xmlns="'http: //schemas.microsoft.com/win/2004/08/events/event'">
        String expression = "string(/Event/@xmlns)";
        String attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        //TODO assertEquals("http://schemas.microsoft.com/win/2004/08/events/event", attrValue);

        //     <Provider Name="Microsoft-Windows-Wevttest" Guid="{03f41308-fa7b-4fb3-98b8-c2ed0a40d1ef}"/>
        expression = "string(/Event/System/Provider/@Name)";
        attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("Microsoft-Windows-Wevttest", attrValue);
        expression = "string(/Event/System/Provider/@Guid)";
        attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("{03f41308-fa7b-4fb3-98b8-c2ed0a40d1ef}", attrValue);

        //     <EventID>100</EventID>
        expression = "/Event/System/EventID/text()";
        Node tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("100", tnode.getNodeValue());

        //     <Version>0</Version>
        expression = "/Event/System/Version/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("0", tnode.getNodeValue());

        //     <Level>1</Level>
        expression = "/Event/System/Level/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("1", tnode.getNodeValue());

        //     <Task>100</Task>
        expression = "/Event/System/Task/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("100", tnode.getNodeValue());

        //     <Opcode>1</Opcode>
        expression = "/Event/System/Opcode/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("1", tnode.getNodeValue());

        //     <Keywords>0x4000000000e00000</Keywords>
        expression = "/Event/System/Keywords/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("0x4000000000e00000", tnode.getNodeValue());

        //     <TimeCreated SystemTime="'2006-0614T21:40:16.312Z'"/>
        expression = "string(/Event/System/TimeCreated/@SystemTime)";
        attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("2006-06-14T21:40:54.625807600Z", attrValue);

        //     <EventRecordID>6</EventRecordID>
        expression = "/Event/System/EventRecordID/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("6", tnode.getNodeValue());

        //     <Execution ProcessID="'2088'" ThreadID="'2464'"/>
        expression = "string(/Event/System/Execution/@ProcessID)";
        attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("2088", attrValue);
        expression = "string(/Event/System/Execution/@ThreadID)";
        attrValue = (String) xPath.compile(expression).evaluate(document, XPathConstants.STRING);
        assertEquals("2464", attrValue);

        //     <Channel>Microsoft-Windows-Wevttest/Operational/Wevttest</Channel>
        expression = "/Event/System/Channel/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("Microsoft-Windows-Wevttest/Operational/Wevttest", tnode.getNodeValue());

        //     <Computer>michaelm4-lh.ntdev.corp.microsoft.com</Computer>
        expression = "/Event/System/Computer/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("michaelm4-lh.ntdev.corp.microsoft.com", tnode.getNodeValue());

        //       <Property>1</Property>
        expression = "/Event/UserData/MyEvent/Property/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("1", tnode.getNodeValue());

        //       <Property2>2</Property2>
        expression = "/Event/UserData/MyEvent/Property2/text()";
        tnode = (Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE);
        assertEquals("2", tnode.getNodeValue());
    }

    @Test
    public void logonEvent() throws Exception {
        try(InputStream in = getClass().getResourceAsStream("event-28492.bin")) {
            byte[] buf = readNBytes(in, Integer.MAX_VALUE);
            BinXmlParser parser = new BinXmlParser(buf, 0, buf.length);
            String xml = parser.xml();
            Reader reader = new StringReader(xml);
            LogonEvent event = (LogonEvent) Event.event(reader);

            assertEquals(4624, event.eventId);
            assertEquals(1, event.version);
            assertEquals(0, event.level);
            assertEquals(12544, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2020-07-23T03:02:30.967149100Z", event.timeCreated);
            assertEquals(28492, event.eventRecordId);
            //assertEquals("{00D66690-1CDF-0000-AC66-D600DF1CD101}", event.activityId);
            assertEquals(468, event.processId);
            assertEquals(3224, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("ADSERVER.idfw.local", event.computer);

            assertEquals("S-1-0-0", event.subjectUserSid);
            assertEquals("-", event.subjectUserName);
            assertEquals("-", event.subjectDomainName);
            assertEquals("0x0", event.subjectLogonId);
            assertEquals("S-1-5-18", event.targetUserSid);
            assertEquals("ADSERVER$", event.targetUserName);
            assertEquals("IDFW", event.targetDomainName);
            assertEquals("0x10ac2b1", event.targetLogonId);
            assertEquals(3, event.logonType);
            assertEquals("Kerberos", event.logonProcessName);
            assertEquals("Kerberos", event.authenticationPackageName);
            assertEquals("-", event.workstationName);
            assertEquals("{134F33DF-F594-7333-AD80-41955568AED9}", event.logonGuid);
            assertEquals("-", event.transmittedServices);
            assertEquals("-", event.lmPackageName);
            assertEquals("0", event.keyLength);
            assertEquals("0x0", event.logonProcessId);
            assertEquals("-", event.processName);
            assertEquals("10.92.170.226", event.ipAddress);
            assertEquals(56190, event.ipPort);
            assertEquals("%%1833", event.impersonationLevel);
        }
    }

    @Test
    public void logoffEvent() throws Exception {
        try(InputStream in = getClass().getResourceAsStream("event-28493.bin")) {
            byte[] buf = readNBytes(in, Integer.MAX_VALUE);
            BinXmlParser parser = new BinXmlParser(buf, 0, buf.length);
            String xml = parser.xml();
            Reader reader = new StringReader(xml);
            LogoffEvent event = (LogoffEvent) Event.event(reader);

            assertEquals(4634, event.eventId);
            assertEquals(0, event.version);
            assertEquals(0, event.level);
            assertEquals(12545, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2020-07-23T03:02:31.295235300Z", event.timeCreated);
            assertEquals(28493, event.eventRecordId);
            assertEquals(468, event.processId);
            assertEquals(1140, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("ADSERVER.idfw.local", event.computer);

            assertEquals("S-1-5-18", event.targetUserSid);
            assertEquals("ADSERVER$", event.targetUserName);
            assertEquals("IDFW", event.targetDomainName);
            assertEquals("0x10ac2b1", event.targetLogonId);
            assertEquals(3, event.logonType);
        }
    }

    // JDK9's InputStream#readNBytes
    private byte[] readNBytes(InputStream in, int len) throws IOException {
        int DEFAULT_BUFFER_SIZE = 8192;
        int MAX_BUFFER_SIZE = Integer.MAX_VALUE - 8;;

        if (len < 0) {
            throw new IllegalArgumentException("len < 0");
        }

        List<byte[]> bufs = null;
        byte[] result = null;
        int total = 0;
        int remaining = len;
        int n;
        do {
            byte[] buf = new byte[Math.min(remaining, DEFAULT_BUFFER_SIZE)];
            int nread = 0;

            // read to EOF which may read more or less than buffer size
            while ((n = in.read(buf, nread,
                    Math.min(buf.length - nread, remaining))) > 0) {
                nread += n;
                remaining -= n;
            }

            if (nread > 0) {
                if (MAX_BUFFER_SIZE - total < nread) {
                    throw new OutOfMemoryError("Required array size too large");
                }
                total += nread;
                if (result == null) {
                    result = buf;
                } else {
                    if (bufs == null) {
                        bufs = new ArrayList<>();
                        bufs.add(result);
                    }
                    bufs.add(buf);
                }
            }
            // if the last call to read returned -1 or the number of bytes
            // requested have been read then break
        } while (n >= 0 && remaining > 0);

        if (bufs == null) {
            if (result == null) {
                return new byte[0];
            }
            return result.length == total ?
                    result : Arrays.copyOf(result, total);
        }

        result = new byte[total];
        int offset = 0;
        remaining = total;
        for (byte[] b : bufs) {
            int count = Math.min(b.length, remaining);
            System.arraycopy(b, 0, result, offset, count);
            offset += count;
            remaining -= count;
        }

        return result;
    }
}
