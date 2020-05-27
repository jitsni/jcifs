package jcifs.dcerpc.msrpc.eventing;

/*
 * @author Jitendra Kotamraju
 */
public class EventLogSession {

    private final String server;
    private final String domain;
    private final String user;
    private final String password;
    private final SessionAuthentication logOnType;

    /**
     * Initializes a new EventLogSession object, and establishes a connection with the Event Log
     * service on the specified computer. The specified credentials (user name and password)
     * are used for the credentials to access the remote computer
     *
     * @param server the name of the computer on which to connect to the Event Log service
     * @param domain the domain of the specified user
     * @param user the user name used to connect to the remote computer
     * @param password the password used to connect to the remote computer
     * @param logOnType the type of connection to use for the connection to the remote computer
     */
    public EventLogSession(String server, String domain, String user, String password,
            SessionAuthentication logOnType) {
        this.server = server;
        this.domain = domain;
        this.user = user;
        this.password = password;
        this.logOnType = logOnType;
    }
}
