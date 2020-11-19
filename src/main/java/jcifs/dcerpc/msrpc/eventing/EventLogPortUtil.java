package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.DcerpcTcpHandle;

import java.io.IOException;

public class EventLogPortUtil {

    private final String server;

    public EventLogPortUtil(String server) {
        this.server = server;
    }

    public int getPort(int connectTimeout, int readTimeout) {
        try (DcerpcTcpHandle ehandle = new DcerpcTcpHandle(server, 135, "even6")) {
            ehandle.setConnectTimeout(connectTimeout);
            ehandle.setSoTimeout(readTimeout);
            ehandle.epmBind();
            return ehandle.getPort();
        } catch (IOException ioe) {
            return -1;
        }
    }

}
