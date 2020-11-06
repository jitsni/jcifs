package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.*;
import jcifs.dcerpc.msrpc.NtlmSecurityProvider;
import jcifs.smb.NtlmPasswordAuthentication;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketTimeoutException;

/*
 * Used to access the Event Log service on a remote computer so you can manage and gather
 * information about the event logs and event providers on the computer.
 *
 * Uses Windows NT LAN Manager (NTLM) authentication during RPC session
 *
 * @author Jitendra Kotamraju
 */
public class EventLogSession implements Closeable {
    private final String server;
    private final boolean encrypted;
    private final NtlmPasswordAuthentication auth;

    private DcerpcTcpHandle pullHandle;
    private DcerpcTcpHandle waitHandle;

    private int connectionTimeout = -1;
    private int epmTimeout = -1;

    /**
     * Initializes a new EventLogSession object, and establishes a connection with the Event Log
     * service on the specified computer. The specified credentials (user name and password)
     * are used for the credentials to access the remote computer
     *
     * @param server the name of the computer on which to connect to the Event Log service
     * @param domain the domain of the specified user
     * @param user the user name used to connect to the remote computer
     * @param password the password used to connect to the remote computer
     */
    public EventLogSession(String server, String domain, String user, String password) {
        this(server, domain, user, password, true);
    }

    /**
     * Initializes a new EventLogSession object, and establishes a connection with the Event Log
     * service on the specified computer. The specified credentials (user name and password)
     * are used for the credentials to access the remote computer
     *
     * @param server the name of the computer on which to connect to the Event Log service
     * @param domain the domain of the specified user
     * @param user the user name used to connect to the remote computer
     * @param password the password used to connect to the remote computer
     * @param encrypted true if transferred data is to be encrypted
     *                  false if transferred data is not to be encrypted (but only integrity)
     */
    public EventLogSession(String server, String domain, String user, String password, boolean encrypted) {
        this.server = server;
        this.encrypted = encrypted;

        auth = new NtlmPasswordAuthentication(domain, user, password);
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public void setEpmTimeout(int epmTimeout) {
        this.epmTimeout = epmTimeout;
    }

    void establishPullConnection() throws IOException {
        pullHandle = (DcerpcTcpHandle) DcerpcHandle.getHandle("ncacn_ip_tcp:" + server +"[even6]", auth);
        pullHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth, encrypted));
        if (connectionTimeout != -1) {
            pullHandle.setConnectTimeout(connectionTimeout);
        }
        if (epmTimeout != -1) {
            pullHandle.setSoTimeout(epmTimeout);
        }
        pullHandle.bind();
        DcerpcMessage auth3 = new Auth3();
        pullHandle.send(auth3);
    }

    void establishWaitConnection() throws IOException {
        int port = pullHandle.getPort();
        waitHandle = new DcerpcTcpHandle(server, port, "even6");
        waitHandle.setDcerpcSecurityProvider(new NtlmSecurityProvider(auth, encrypted));
        waitHandle.setAssocGroup(pullHandle.getAssocGroup());   // associate pull and wait connections
        if (connectionTimeout != -1) {
            waitHandle.setConnectTimeout(connectionTimeout);
        }
        if (epmTimeout != -1) {
            waitHandle.setSoTimeout(epmTimeout);
        }
        waitHandle.bind();
        DcerpcMessage auth3 = new Auth3();
        waitHandle.send(auth3);
    }

    @Override
    public void close() {
        try {
            if (pullHandle != null) {
                pullHandle.close();
                pullHandle = null;
            }
        } catch (IOException ioe) {
            // ignore ioe
        }

        try {
            if (waitHandle != null) {
                waitHandle.close();
                waitHandle = null;
            }
        } catch (IOException ioe) {
            // ignore ioe
        }
    }

    // synchronized since two threads: EventLogWatcher thread and close's caller thread
    synchronized void sendPull(DcerpcMessage msg, int timeout) throws IOException {
        if (pullHandle == null) {
            establishPullConnection();
        }
        pullHandle.setSoTimeout(timeout);
        pullHandle.sendrecv(msg);
    }

    void sendWait(even6.EvtRpcRemoteSubscriptionWaitAsync msg, int timeout) throws IOException {
        while (true) {
            if (waitHandle == null) {
                establishWaitConnection();
            }
            try {
                waitHandle.setSoTimeout(timeout);
                waitHandle.sendrecv(msg);
                break;
            } catch (SocketTimeoutException se) {
                // No new events within socket read timeout. Try again with a new connection
                waitHandle.close();
                waitHandle = null;
            }
        }
    }
}
