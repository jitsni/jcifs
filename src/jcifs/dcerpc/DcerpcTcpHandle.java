package jcifs.dcerpc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.logging.Logger;

import jcifs.dcerpc.msrpc.EpmMap;
import jcifs.util.Encdec;

/*
 * @author Jitendra Kotamraju
 */
public class DcerpcTcpHandle extends DcerpcHandle {
    private static final Logger LOGGER = Logger.getLogger(DcerpcTcpHandle.class.getName());

    private static final int DEFAULT_SO_TIMEOUT = 60000;
    private static final int DEFAULT_CONN_TIMEOUT = 60000;

    private int port;
    private Socket socket;
    private OutputStream out;
    private InputStream in;

    public DcerpcTcpHandle(String url) throws DcerpcException {
        this.binding = DcerpcHandle.parseBinding(url);
    }

    private DcerpcTcpHandle(String server, int port, String endpoint) throws DcerpcException {
        this.binding = new DcerpcBinding("ncacn_ip_tcp", server);
        this.port = port;
        this.binding.setOption("endpoint", endpoint);
    }

    @Override
    public void bind() throws IOException {
        if (port != 135) {
            DcerpcTcpHandle ehandle = new DcerpcTcpHandle(binding.server, 135, "epm");
            ehandle.bind();
            EpmMap rpc = new EpmMap(binding.uuid, binding.major);
            ehandle.sendrecv(rpc);
            ehandle.close();
            LOGGER.info(String.format("%s epm port = %d", binding, rpc.getPort()));

            port = rpc.getPort();
        }

        try {
            InetAddress iaddr = InetAddress.getByName(binding.server);
            InetSocketAddress sockaddr = new InetSocketAddress(iaddr, port);
            socket = new Socket();
            socket.connect(sockaddr, DEFAULT_CONN_TIMEOUT);
            socket.setSoTimeout(DEFAULT_SO_TIMEOUT);
            socket.setKeepAlive(true);
            out = socket.getOutputStream();
            in = socket.getInputStream();

            super.bind();
        } catch (IOException ie) {
            close();
            throw ie;
        }
    }

    @Override
    public void close() throws IOException {
        state = 0;
        port = 0;
        if (socket != null) {
            try {
                socket.close();
            } finally {
                socket = null;
            }
        }
    }

    @Override
    protected void doSendFragment(byte[] buf, int off, int length, boolean isDirect) throws IOException {
        out.write(buf, off, length);
    }

    @Override
    protected void doReceiveFragment(byte[] buf, boolean isDirect) throws IOException {
        if (buf.length < max_recv) {
            throw new IllegalArgumentException("buffer too small");
        }

        // read pdu header (from which length of fragment will be known)
        readNBytesWithCheck(buf, 0, 24);
        int off = 24;

        if (buf[0] != 5 && buf[1] != 0) {
            throw new IOException("Unexpected DCERPC PDU header");
        }

        int length = Encdec.dec_uint16le(buf, 8);
        if (length > max_recv) {
            throw new IOException("Unexpected fragment length: " + length);
        }

        // read the rest of pdu
        readNBytesWithCheck(buf, off, length - off);
    }

    private void readNBytesWithCheck(byte[] b, int off, int len) throws IOException {
        int count = readNBytes(b, off, len);
        if (count != len) {
            String msg = String.format("Couldn't read all expected bytes = %d, read = %d", len, count);
            throw new IOException(msg);
        }
    }

    // JDK9's InputStream#readNBytes
    private int readNBytes(byte[] b, int off, int len) throws IOException {
        checkFromIndexSize(off, len, b.length);

        int n = 0;
        while (n < len) {
            int count = in.read(b, off + n, len - n);
            if (count < 0)
                break;
            n += count;
        }
        return n;
    }

    private static void checkFromIndexSize(int fromIndex, int size, int length) {
        if ((length | fromIndex | size) < 0 || size > length - fromIndex) {
            String msg = String.format("range check fromIndex=%d size=%d, length=%d", fromIndex, size, length);
            throw new IndexOutOfBoundsException(msg);
        }
    }
}
