package jcifs.dcerpc.msrpc.eventing;

/**
 * @author Jitendra Kotamraju
 */
public class EventLogException extends RuntimeException {

    public EventLogException(String s) {
        super(s);
    }

    public EventLogException(Throwable cause) {
        super(cause);
    }

}
