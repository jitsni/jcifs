/**
 * Copyright 2023 Jitendra Kotamraju.
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
import static org.junit.Assert.assertNull;

public class EmptyEventDataTest {

    @Test
    public void logonEvent() throws Exception {
        try (Reader reader = new InputStreamReader(getClass().getResourceAsStream("eventdata-empty.xml"), UTF_8)) {
            LogonEvent event = (LogonEvent) Event.event(reader);

            assertEquals(4624, event.eventId);
            assertEquals(2, event.version);
            assertEquals(0, event.level);
            assertEquals(12544, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2023-07-11T02:17:05.427501500Z", event.timeCreated);
            assertEquals(685663, event.eventRecordId);
            assertNull(event.activityId);
            assertEquals(640, event.processId);
            assertEquals(1568, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("WIN-88JLS7VE383.stevetest.local", event.computer);

            assertEquals("S-1-0-0", event.subjectUserSid);
            assertEquals("-", event.subjectUserName);
            assertEquals("-", event.subjectDomainName);
            assertEquals("0x0", event.subjectLogonId);
            assertEquals("S-1-5-18", event.targetUserSid);
            assertEquals("WIN-88JLS7VE383$", event.targetUserName);
            assertEquals("STEVETEST.LOCAL", event.targetDomainName);
            assertEquals("0x26d6091", event.targetLogonId);
            assertEquals(3, event.logonType);
            assertEquals("Kerberos", event.logonProcessName);
            assertEquals("Kerberos", event.authenticationPackageName);
            assertEquals("", event.workstationName);
            assertEquals("{9343DF5D-795-8993-C6E-4AA56FAB82D}", event.logonGuid);
            assertEquals("-", event.transmittedServices);
            assertEquals("-", event.lmPackageName);
            assertEquals("0", event.keyLength);
            assertEquals("0x0", event.logonProcessId);
            assertEquals("-", event.processName);
            assertEquals("::1", event.ipAddress);
            assertEquals(50591, event.ipPort);
            assertEquals("%%1833", event.impersonationLevel);
            assertEquals("-", event.processName);
            assertEquals("-", event.restrictedAdminMode);
            assertEquals("-", event.targetOutboundUserName);
            assertEquals("-", event.targetOutboundDomainName);
            assertEquals("%%1843", event.virtualAccount);
            assertEquals("0x0", event.targetLinkedLogonId);
            assertEquals("%%1842", event.elevatedToken);
        }
    }
}

