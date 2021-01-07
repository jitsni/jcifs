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

import java.util.Map;

/**
   https://docs.microsoft.com/en-us/windows/security/threat-protection/auditing/event-4624
   ...
   <EventData>
   <Data Name="SubjectUserSid">S-1-5-18</Data>
   <Data Name="SubjectUserName">WIN-GG82ULGC9GO$</Data>
   <Data Name="SubjectDomainName">WORKGROUP</Data>
   <Data Name="SubjectLogonId">0x3e7</Data>
   ...

 * @author Jitendra Kotamraju
 */
public class LogonEvent extends Event {
    public final String subjectUserSid;
    public final String subjectUserName;
    public final String subjectDomainName;
    public final String subjectLogonId;
    public final String targetUserSid;
    public final String targetUserName;
    public final String targetDomainName;
    public final String targetLogonId;
    public final int logonType;
    public final String logonProcessName;
    public final String authenticationPackageName;
    public final String workstationName;
    public final String logonGuid;
    public final String transmittedServices;
    public final String lmPackageName;
    public final String keyLength;
    public final String logonProcessId;
    public final String processName;
    public final String ipAddress;
    public final int ipPort;
    public final String impersonationLevel;
    public final String restrictedAdminMode;
    public final String targetOutboundUserName;
    public final String targetOutboundDomainName;
    public final String virtualAccount;
    public final String targetLinkedLogonId;
    public final String elevatedToken;

    protected LogonEvent(int eventId, int version, int level, int task, int opcode, String keywords,
               String timeCreated, int eventRecordId, String activityId, int processId, int threadId,
               String channel, String computer, Map<String, String> eventData) {
        super(eventId, version, level, task, opcode, keywords, timeCreated, eventRecordId, activityId,
                processId, threadId, channel, computer);

        subjectUserSid = eventData.get("SubjectUserSid");
        subjectUserName = eventData.get("SubjectUserName");
        subjectDomainName = eventData.get("SubjectDomainName");
        subjectLogonId = eventData.get("SubjectLogonId");
        targetUserSid = eventData.get("TargetUserSid");
        targetUserName = eventData.get("TargetUserName");
        targetDomainName = eventData.get("TargetDomainName");
        targetLogonId = eventData.get("TargetLogonId");
        logonType = Integer.parseInt(eventData.getOrDefault("LogonType", "-1"));
        logonProcessName = eventData.get("LogonProcessName");
        authenticationPackageName = eventData.get("AuthenticationPackageName");
        workstationName = eventData.get("WorkstationName");
        logonGuid = eventData.get("LogonGuid");
        transmittedServices = eventData.get("TransmittedServices");
        lmPackageName = eventData.get("LmPackageName");
        keyLength = eventData.get("KeyLength");
        logonProcessId = eventData.get("ProcessId");
        processName = eventData.get("ProcessName");
        ipAddress = eventData.get("IpAddress");
        ipPort = parseInt(eventData.getOrDefault("IpPort", "-1"));
        impersonationLevel = eventData.get("ImpersonationLevel");
        restrictedAdminMode = eventData.get("RestrictedAdminMode");
        targetOutboundUserName = eventData.get("TargetOutboundUserName");
        targetOutboundDomainName = eventData.get("TargetOutboundDomainName");
        virtualAccount = eventData.get("VirtualAccount");
        targetLinkedLogonId = eventData.get("TargetLinkedLogonId");
        elevatedToken = eventData.get("ElevatedToken");
    }

    private int parseInt(String str) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public String toString() {
        return "LogonEvent{" +
                "targetUserName='" + targetUserName + '\'' +
                ", targetDomainName='" + targetDomainName + '\'' +
                ", logonType=" + logonType +
                ", ipAddress='" + ipAddress + '\'' +
                ", ipPort=" + ipPort +
                ", logonID=" + targetLogonId +
                '}';
    }
}
