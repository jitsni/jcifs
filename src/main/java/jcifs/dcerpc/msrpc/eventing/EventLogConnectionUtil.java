package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.DcerpcException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import static jcifs.dcerpc.DcerpcError.DCERPC_FAULT_ACCESS_DENIED;
import static jcifs.dcerpc.msrpc.eventing.EventLogConnectionUtil.ConnectionStatus.STATUS_OK;

public class EventLogConnectionUtil implements AutoCloseable {

    public enum Status {
        OK,
        ERROR
    }

    public static class ConnectionStatus {
        public static final ConnectionStatus STATUS_OK = new ConnectionStatus(Status.OK, null, null);
        public static final ConnectionStatus STATUS_IO = new ConnectionStatus(Status.ERROR, "I/O error", null);

        public final Status status;
        public final String errorMsg;
        public final Exception ex;

        private ConnectionStatus(Status status, String errorMsg, Exception ex) {
            this.status = status;
            this.errorMsg = errorMsg;
            this.ex = ex;
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
        ConnectionStatus status;

        try {
            watcher.start();
            status = connectionStatus.get(timeout, unit);
        } catch (TimeoutException e) {
            status = new ConnectionStatus(Status.ERROR, "Timed out", e);
        } catch (Exception e) {
            status = new ConnectionStatus(Status.ERROR, "Unknown error", e);

            if (e instanceof ExecutionException) {
                if (e.getCause() instanceof EventLogException) {
                    if (e.getCause().getCause() instanceof IOException) {
                        status = new ConnectionStatus(Status.ERROR, "I/O error", e);

                        if (e.getCause().getCause() instanceof DcerpcException) {
                            DcerpcException dce = (DcerpcException) e.getCause().getCause();
                            if (dce.getErrorCode() == DCERPC_FAULT_ACCESS_DENIED) {
                                status = new ConnectionStatus(Status.ERROR, "Access denied", e);
                            }
                        }
                    }
                }
            }
        }

        return status;
    }

    @Override
    public void close() {
        watcher.close();
    }

    private static class EventCallback implements Consumer<List<EventRecord>> {
        private final CompletableFuture<ConnectionStatus> status;

        EventCallback(CompletableFuture<ConnectionStatus> status) {
            this.status = status;
        }

        @Override
        public void accept(List<EventRecord> events) {
            EventRecord record = events.get(0);
            if (record.exception != null) {
                status.completeExceptionally(record.exception);
            } else {
                status.complete(STATUS_OK);
            }
        }
    }

}
