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
    private final int port;
    private final boolean encrypted;
    private final NtlmPasswordAuthentication auth;

    private DcerpcTcpHandle pullHandle;
    private DcerpcTcpHandle waitHandle;

    private int connectionTimeout = -1;
    private int epmTimeout = -1;

    public EventLogSession(String server, String domain, String user, String password) {
        this(server, -1, domain, user, password, true);
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
     */
    public EventLogSession(String server, int port, String domain, String user, String password) {
        this(server, port, domain, user, password, true);
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
    public EventLogSession(String server, int port, String domain, String user, String password, boolean encrypted) {
        this.server = server;
        this.port = port;
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
        pullHandle = new DcerpcTcpHandle(server, port, "even6");
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
