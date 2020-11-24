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
