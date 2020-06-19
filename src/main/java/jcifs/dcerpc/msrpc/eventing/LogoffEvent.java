package jcifs.dcerpc.msrpc.eventing;

import java.util.Map;

/*
   https://docs.microsoft.com/en-us/windows/security/threat-protection/auditing/event-4634
   ...
   <EventData>
   <Data Name="TargetUserSid">S-1-5-90-1</Data>
   <Data Name="TargetUserName">DWM-1</Data>
   <Data Name="TargetDomainName">Window Manager</Data>
   <Data Name="TargetLogonId">0x1a0992</Data>
   <Data Name="LogonType">2</Data>
   ...

 * @author Jitendra Kotamraju
 */
public class LogoffEvent extends Event {
    public final String targetUserSid;
    public final String targetUserName;
    public final String targetDomainName;
    public final String targetLogonId;
    public final int logonType;

    protected LogoffEvent(int eventId, int version, int level, int task, int opcode, String keywords,
                String timeCreated, int eventRecordId, String activityId, int processId, int threadId,
                String channel, String computer, Map<String, String> eventData) {

        super(eventId, version, level, task, opcode, keywords, timeCreated, eventRecordId, activityId,
                processId, threadId, channel, computer);

        targetUserSid = eventData.get("TargetUserSid");
        targetUserName = eventData.get("TargetUserName");
        targetDomainName = eventData.get("TargetDomainName");
        targetLogonId = eventData.get("TargetLogonId");
        logonType = Integer.parseInt(eventData.getOrDefault("LogonType", "-1"));
    }

    @Override
    public String toString() {
        return "LogoffEvent{" +
                "targetUserName='" + targetUserName + '\'' +
                ", targetDomainName='" + targetDomainName + '\'' +
                ", logonType=" + logonType +
                '}';
    }
}
