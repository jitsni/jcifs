package jcifs.dcerpc.msrpc.eventing;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import static javax.xml.stream.XMLStreamConstants.*;

/*
   <Event xmlns="http://schemas.microsoft.com/win/2004/08/events/event">
     <System>
       <Provider Name="Microsoft-Windows-Security-Auditing" Guid="{54849625-5478-4994-A5BA-3E3B0328C30D}"/>
       <EventID>4624</EventID>
       <Version>2</Version>
       <Level>0</Level>
       <Task>12544</Task>
       <Opcode>0</Opcode>
       <Keywords>0x8020000000000000</Keywords>
       <TimeCreated SystemTime="2015-11-12T00:24:35.079785200Z"/>
       <EventRecordID>211</EventRecordID>
       <Correlation ActivityID="{00D66690-1CDF-0000-AC66-D600DF1CD101}"/>
       <Execution ProcessID="716" ThreadID="760"/>
       <Channel>Security</Channel>
       <Computer>WIN-GG82ULGC9GO</Computer>
       <Security/>
     </System>
     <EventData>
       <Data Name="SubjectUserSid">S-1-5-18</Data>
       ...
 * @author Jitendra Kotamraju
 */
public class Event {
    public final int eventId;
    public final int version;
    public final int level;
    public final int task;
    public final int opcode;
    public final String keywords;
    public final String timeCreated;
    public final int eventRecordId;
    public final String activityId;
    public final int processId;
    public final int threadId;
    public final String channel;
    public final String computer;

    protected Event(int eventId, int version, int level, int task, int opcode, String keywords, String timeCreated,
                int eventRecordId, String activityId, int processId, int threadId, String channel, String computer) {

        this.eventId = eventId;
        this.version = version;
        this.level = level;
        this.task = task;
        this.opcode = opcode;
        this.keywords = keywords;
        this.timeCreated = timeCreated;
        this.eventRecordId = eventRecordId;
        this.activityId = activityId;
        this.processId = processId;
        this.threadId = threadId;
        this.channel = channel;
        this.computer = computer;
    }

    @Override
    public String toString() {
        return "Event{" +
                "eventId=" + eventId +
                ", version=" + version +
                ", level=" + level +
                ", task=" + task +
                ", opcode=" + opcode +
                ", keywords='" + keywords + '\'' +
                ", eventRecordId=" + eventRecordId +
                ", processId=" + processId +
                ", threadId=" + threadId +
                ", channel='" + channel + '\'' +
                ", computer='" + computer + '\'' +
                '}';
    }

    public static Event event(Reader reader) throws XMLStreamException {
        int eventId = -1;
        int version = -1;
        int level = -1;
        int task = -1;
        int opcode = -1;
        String keywords = null;
        String timeCreated = null;
        int eventRecordId = -1;
        String activityId = null;
        int processId = -1;
        int threadId = -1;
        String channel = null;
        String computer = null;
        Map<String, String> eventData = new HashMap<>();

        XMLStreamReader sr = XMLInputFactory.newInstance().createXMLStreamReader(reader);
        while (sr.hasNext()) {
            int eventType = sr.next();
            if (eventType == START_ELEMENT) {
                switch (sr.getLocalName()) {
                    case "EventID":
                        eventId = parseIntegerText(sr);
                        break;
                    case "Version":
                        version = parseIntegerText(sr);
                        break;
                    case "Level":
                        level = parseIntegerText(sr);
                        break;
                    case "Task":
                        task = parseIntegerText(sr);
                        break;
                    case "Opcode":
                        opcode = parseIntegerText(sr);
                        break;
                    case "Keywords":
                        keywords = parseStringText(sr);
                        break;
                    case "TimeCreated":
                        Map<String, String> time = parseAttributes(sr);
                        timeCreated = time.get("SystemTime");
                        break;
                    case "EventRecordID":
                        eventRecordId = parseIntegerText(sr);
                        break;
                    case "Correlation":
                        Map<String, String> correlation = parseAttributes(sr);
                        activityId = correlation.get("ActivityID");
                        break;
                    case "Execution":
                        Map<String, String> exec = parseAttributes(sr);
                        processId = Integer.parseInt(exec.getOrDefault("ProcessID", "-1"));
                        threadId = Integer.parseInt(exec.getOrDefault("ThreadID", "-1"));
                        break;
                    case "Channel":
                        channel = parseStringText(sr);
                        break;
                    case "Computer":
                        computer = parseStringText(sr);
                        break;
                    case "EventData":
                        if (eventId == 4624 || eventId == 4634) {
                            eventData = parseEventData(sr);
                        }
                        break;
                    default:
                        break;
                }
            }
        }

        Event event;
        switch (eventId) {
            case 4624:
                event = new LogonEvent(eventId, version, level, task, opcode, keywords, timeCreated,
                        eventRecordId, activityId, processId, threadId, channel, computer, eventData);
                break;
            case 4634:
                event = new LogoffEvent(eventId, version, level, task, opcode, keywords, timeCreated,
                        eventRecordId, activityId, processId, threadId, channel, computer, eventData);
                break;
            default:
                event = new Event(eventId, version, level, task, opcode, keywords, timeCreated,
                        eventRecordId, activityId, processId, threadId, channel, computer);
                break;
        }

        sr.close();

        return event;
    }

    private static int parseIntegerText(XMLStreamReader sr) throws XMLStreamException {
        assert sr.getEventType() == START_ELEMENT;
        int eventType = sr.next();
        assert eventType == CHARACTERS;
        String text = sr.getText();
        int value = Integer.parseInt(text);
        sr.next();
        assert sr.getEventType() == END_ELEMENT;
        return value;
    }

    private static Map<String, String> parseAttributes(XMLStreamReader sr) throws XMLStreamException {
        Map<String, String> exec = new HashMap<>();
        assert sr.getEventType() == START_ELEMENT;
        for(int i=0; i < sr.getAttributeCount(); i++) {
            exec.put(sr.getAttributeLocalName(i), sr.getAttributeValue(i));
        }
        sr.next();
        assert sr.getEventType() == END_ELEMENT;
        return exec;
    }

    private static String parseStringText(XMLStreamReader sr) throws XMLStreamException {
        assert sr.getEventType() == START_ELEMENT;
        int eventType = sr.next();
        assert eventType == CHARACTERS;
        String text = sr.getText();
        sr.next();
        assert sr.getEventType() == END_ELEMENT;
        return text;
    }

    private static Map<String, String > parseEventData(XMLStreamReader sr) throws XMLStreamException {
        Map<String, String> eventData = new HashMap<>();
        while (sr.hasNext()) {
            int eventType = sr.next();
            if (eventType == START_ELEMENT && sr.getLocalName().equals("Data")) {
                String attribute = sr.getAttributeValue(0);
                eventType = sr.next();
                assert eventType == CHARACTERS;
                String value = sr.getText();
                eventData.put(attribute, value);
            } else if (eventType == END_ELEMENT && sr.getLocalName().equals("EventData")) {
                break;
            }
        }
        assert sr.getEventType() == END_ELEMENT;
        return eventData;
    }

}
