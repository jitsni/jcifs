package jcifs.dcerpc.msrpc.eventing;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static jcifs.dcerpc.msrpc.eventing.EventLogConnectionUtil.ConnectionStatus.STATUS_IO;
import static jcifs.dcerpc.msrpc.eventing.EventLogConnectionUtil.ConnectionStatus.STATUS_OK;
import static jcifs.dcerpc.msrpc.eventing.EventLogConnectionUtil.ConnectionStatus.STATUS_TIMED_OUT;
import static jcifs.dcerpc.msrpc.eventing.EventLogConnectionUtil.ConnectionStatus.STATUS_UNKNOWN;

public class EventLogConnectionUtil implements AutoCloseable {

    public enum Status {
        OK,
        ERROR
    }

    public static class ConnectionStatus {
        public static final ConnectionStatus STATUS_OK = new ConnectionStatus(Status.OK, null);
        public static final ConnectionStatus STATUS_IO = new ConnectionStatus(Status.ERROR, "I/O error");
        public static final ConnectionStatus STATUS_UNKNOWN = new ConnectionStatus(Status.ERROR, "Unknown error");
        public static final ConnectionStatus STATUS_TIMED_OUT = new ConnectionStatus(Status.ERROR, "Timed out");

        public final Status status;
        public final String errorMsg;

        private ConnectionStatus(Status status, String errorMsg) {
            this.status = status;
            this.errorMsg = errorMsg;
        }
        @Override
        public String toString() {
            return errorMsg == null ? status.toString() : status + ", " + errorMsg;
        }

    }

    private final EventLogWatcher watcher;
    private final CompletableFuture<ConnectionStatus> connectionStatus;
    private final EventLogSession session;

    public EventLogConnectionUtil(String server, int port, String domain, String user, String password, String path) {
        session = new EventLogSession(server, port, domain, user, password);
        String xpath = "*";
        EventLogQuery query = new EventLogQuery(path, EventLogQuery.PathType.LogName, xpath, session, false);
        connectionStatus = new CompletableFuture<>();
        EventCallback callback = new EventCallback(connectionStatus);
        watcher = new EventLogWatcher(query, null, true, callback);
    }

    public void setConnectTimeout(int timeout) {
        session.setConnectionTimeout(timeout);
    }

    public void setEpmTimeout(int timeout) {
        session.setEpmTimeout(timeout);
    }

    public void setPullTimeout(int timeout) {
        watcher.setPullTimeout(timeout);
    }

    public void setWaitTimeout(int timeout) {
        watcher.setWaitTimeout(timeout);
    }

    public void setRequestedRecords(int records) {
        watcher.setRequestedRecords(records);
    }

    public ConnectionStatus testConnection(long timeout, TimeUnit unit) {
        ConnectionStatus status = STATUS_UNKNOWN;

        try {
            watcher.start();
            status = connectionStatus.get(timeout, unit);
        } catch (TimeoutException e) {
            status = STATUS_TIMED_OUT;
        } catch (ExecutionException e) {
            if (e.getCause() instanceof EventLogException && e.getCause().getCause() instanceof IOException) {
                status = STATUS_IO;
            }
        } catch (Exception e) {
            // ignore
        }

        return status;
    }

    @Override
    public void close() {
        watcher.close();
    }

    private static class EventCallback implements EventLogRecordWritten {
        private final CompletableFuture<ConnectionStatus> status;

        EventCallback(CompletableFuture<ConnectionStatus> status) {
            this.status = status;
        }

        @Override
        public void onEntryWritten(EventRecord record) {
            if (record.exception != null) {
                status.completeExceptionally(record.exception);
            } else {
                status.complete(STATUS_OK);
            }
        }
    }

}
