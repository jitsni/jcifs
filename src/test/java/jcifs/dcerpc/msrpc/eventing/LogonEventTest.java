/**
 * Copyright 2020-2024 Jitendra Kotamraju.
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
public class LogonEventTest {

    @Test
    public void logonEvent() throws Exception {
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("4624.xml"), UTF_8)) {
            LogonEvent event = (LogonEvent) Event.event(reader);
            assertEquals(4624, event.eventId);
            assertEquals(2, event.version);
            assertEquals(0, event.level);
            assertEquals(12544, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2015-11-12T00:24:35.079785200Z", event.timeCreated);
            assertEquals(211, event.eventRecordId);
            assertEquals("{00D66690-1CDF-0000-AC66-D600DF1CD101}", event.activityId);
            assertEquals(716, event.processId);
            assertEquals(760, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("WIN-GG82ULGC9GO", event.computer);

            assertEquals("S-1-5-18", event.subjectUserSid);
            assertEquals("WIN-GG82ULGC9GO$", event.subjectUserName);
            assertEquals("WORKGROUP", event.subjectDomainName);
            assertEquals("0x3e7", event.subjectLogonId);
            assertEquals("S-1-5-21-1377283216-344919071-3415362939-500", event.targetUserSid);
            assertEquals("Administrator", event.targetUserName);
            assertEquals("WIN-GG82ULGC9GO", event.targetDomainName);
            assertEquals("0x8dcdc", event.targetLogonId);
            assertEquals(2, event.logonType);
            assertEquals("User32", event.logonProcessName);
            assertEquals("Negotiate", event.authenticationPackageName);
            assertEquals("WIN-GG82ULGC9GO", event.workstationName);
            assertEquals("{00000000-0000-0000-0000-000000000000}", event.logonGuid);
            assertEquals("-", event.transmittedServices);
            assertEquals("-", event.lmPackageName);
            assertEquals("0", event.keyLength);
            assertEquals("0x44c", event.logonProcessId);
            assertEquals("C:\\\\Windows\\\\System32\\\\svchost.exe", event.processName);
            assertEquals("127.0.0.1", event.ipAddress);
            assertEquals(0, event.ipPort);
            assertEquals("%%1833", event.impersonationLevel);
            assertEquals("C:\\\\Windows\\\\System32\\\\svchost.exe", event.processName);
            assertEquals("C:\\\\Windows\\\\System32\\\\svchost.exe", event.processName);
            assertEquals("C:\\\\Windows\\\\System32\\\\svchost.exe", event.processName);
            assertEquals("-", event.restrictedAdminMode);
            assertEquals("-", event.targetOutboundUserName);
            assertEquals("-", event.targetOutboundDomainName);
            assertEquals("%%1843", event.virtualAccount);
            assertEquals("0x0", event.targetLinkedLogonId);
            assertEquals("%%1842", event.elevatedToken);
        }
    }

    @Test
    public void logonEventForwarded() throws Exception {
        try(Reader reader = new InputStreamReader(getClass().getResourceAsStream("4624-forwarded.xml"), UTF_8)) {
            LogonEvent event = (LogonEvent) Event.event(reader);
            assertEquals(4624, event.eventId);
            assertEquals(1, event.version);
            assertEquals(0, event.level);
            assertEquals(12544, event.task);
            assertEquals(0, event.opcode);
            assertEquals("0x8020000000000000", event.keywords);
            assertEquals("2024-07-04T06:08:51.681644400Z", event.timeCreated);
            assertEquals(305041, event.eventRecordId);
            assertEquals(504, event.processId);
            assertEquals(2816, event.threadId);
            assertEquals("Security", event.channel);
            assertEquals("ADSERVER.nimbus.local", event.computer);

            assertEquals("S-1-0-0", event.subjectUserSid);
            assertEquals("-", event.subjectUserName);
            assertEquals("-", event.subjectDomainName);
            assertEquals("0x0", event.subjectLogonId);
            assertEquals("S-1-5-18", event.targetUserSid);
            assertEquals("ADSERVER$", event.targetUserName);
            assertEquals("NIMBUS", event.targetDomainName);
            assertEquals("0x7f6a6a4", event.targetLogonId);
            assertEquals(3, event.logonType);
            assertEquals("Kerberos", event.logonProcessName);
            assertEquals("Kerberos", event.authenticationPackageName);
            assertEquals("-", event.workstationName);
            assertEquals("{8DF5AC95-5B93-F823-C32B-5728D8A360B8}", event.logonGuid);
            assertEquals("-", event.transmittedServices);
            assertEquals("-", event.lmPackageName);
            assertEquals("0", event.keyLength);
            assertEquals("0x0", event.logonProcessId);
            assertEquals("-", event.processName);
            assertEquals("10.83.41.71", event.ipAddress);
            assertEquals(52698, event.ipPort);
        }
    }
}
