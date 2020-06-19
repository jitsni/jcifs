package jcifs.dcerpc.msrpc.eventing;

import org.junit.Test;

import java.io.InputStreamReader;
import java.io.Reader;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

/**
 * @author Jitendra Kotamraju
 */
public class LogoffEventTest {

    @Test
    public void logoffEvent() throws Exception {
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("4634.xml"), UTF_8)) {
            LogoffEvent event = (LogoffEvent) Event.event(reader);
            assertEquals(4634, event.eventId);
            assertEquals(0, event.version);
            assertEquals(0, event.level);
            assertEquals(12545, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2015-09-09T02:27:57.877205900Z", event.timeCreated);
            assertEquals(230019, event.eventRecordId);
            assertEquals(516, event.processId);
            assertEquals(832, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("DC01.contoso.local", event.computer);

            assertEquals("S-1-5-90-1", event.targetUserSid);
            assertEquals("DWM-1", event.targetUserName);
            assertEquals("Window Manager", event.targetDomainName);
            assertEquals("0x1a0992", event.targetLogonId);
            assertEquals(2, event.logonType);
        }
    }
}
