package jcifs.dcerpc.msrpc.eventing;

/**
 * Defines values for the type of authentication used during a Remote Procedure Call (RPC) login to a server.
 * This login occurs when you create a EventLogSession object that specifies a connection to a remote computer.
 *
 * @author Jitendra Kotamraju
 */
public enum SessionAuthentication {

    /**
     * Use the default authentication method during RPC login. The default authentication is equivalent to Negotiate.
     */
    DEFAULT,

    /**
     * Use Kerberos authentication during RPC login.
     */
    KERBEROS,

    /**
     * Use the Negotiate authentication method during RPC login. This allows the client application
     * to select the most appropriate authentication method (NTLM or Kerberos) for the situation.
     */
    NEGOTIATE,

    /**
     * Use Windows NT LAN Manager (NTLM) authentication during RPC login.
     */
    NTLM

}
