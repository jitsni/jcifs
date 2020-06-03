package jcifs.dcerpc.msrpc.eventing;

import jcifs.dcerpc.*;
import jcifs.dcerpc.ndr.*;
import jcifs.dcerpc.rpc.policy_handle;

import java.util.Arrays;

/*
 * Initial version is midl generated (and then it is modified a lot)
 *
 * @author Jitendra Kotamraju
 */
public class even6 {

    public static String getSyntax() {
        return "f6beaff7-1e19-4fbb-9f8f-b89e2018337c:1.0";
    }

    public static final int MAX_PAYLOAD = 2 * 1024 * 1024;

    private static final int MAX_RPC_RECORD_COUNT = 1024;
    private static final int MAX_RPC_BATCH_SIZE = MAX_PAYLOAD;

    // Evt Path Flags
    public static final int EvtQueryChannelPath = 0x00000001;
    public static final int EvtQueryFilePath = 0x00000002;
    public static final int EvtReadOldestToNewest = 0x00000100;
    public static final int EvtReadNewestToOldest = 0x00000200;

    public static final int EvtSubscribeToFutureEvents = 0x00000001;
    public static final int EvtSubscribeStartAtOldestRecord = 0x00000002;
    public static final int EvtSubscribeStartAfterBookmark = 0x00000003;
    public static final int EvtSubscribePull = 0x10000000;

    public static class RpcInfo extends NdrObject {
        public int m_error;
        public int m_subErr;
        public int m_subErrParam;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_long(m_error);
            _dst.enc_ndr_long(m_subErr);
            _dst.enc_ndr_long(m_subErrParam);
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            m_error = _src.dec_ndr_long();
            m_subErr = _src.dec_ndr_long();
            m_subErrParam = _src.dec_ndr_long();
        }

        @Override
        public String toString() {
            return "(m_error=" + m_error + ", m_subErr=" + m_subErr + ", m_subErrParam=" + m_subErrParam + ")";
        }
    }

    // 2.2.11 EvtRpcQueryChannelInfo
    public static class EvtRpcQueryChannelInfo extends NdrObject {
        public String name;
        public int status;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            _dst.enc_ndr_referent(name, 1);
            _dst.enc_ndr_long(status);
            if (name != null) {
                _dst.enc_ndr_string(name);
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            int referent = _src.dec_ndr_long();         // name pointer
            status = _src.dec_ndr_long();
            if (referent != 0) {
                name = _src.dec_ndr_string();
            }
        }

        @Override
        public String toString() {
            return "(name=" + name + ", status=" + status + ")";
        }
    }

    // 3.1.4.12 EvtRpcRegisterLogQuery (Opnum 5)
    public static class EvtRpcRegisterLogQuery extends RegisterRequest {
        private final String channelPath;
        private final String query;
        private final int flags;

        public int getOpnum() { return 5; }

        public EvtRpcRegisterLogQuery(String channelPath, String query, int flags) {
            this.channelPath = channelPath;
            this.query = query;
            this.flags = flags;

            this.ptype = 0;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(channelPath, 1);
            if (channelPath != null) {
                _dst.enc_ndr_string(channelPath);
            }
            _dst.enc_ndr_string(query);
            _dst.enc_ndr_long(flags);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            super.decode_out(_src);
        }
    }

    // 3.1.4.13 EvtRpcQueryNext (Opnum 11)
    public static class EvtRpcQueryNext extends EventResponse {
        public final int timeOutEnd;
        public final int flags;

        public EvtRpcQueryNext(policy_handle handle, int numRequestedRecords, int timeOutEnd, int flags) {
            super(handle, numRequestedRecords);

            this.timeOutEnd = timeOutEnd;
            this.flags = flags;

            this.ptype = 0;
        }

        public int getOpnum() { return 0x0b; }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            handle.encode(_dst);
            _dst.enc_ndr_long(numRequestedRecords);
            _dst.enc_ndr_long(timeOutEnd);
            _dst.enc_ndr_long(flags);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            super.decode_out(_src);
        }
    }

    // 3.1.4.8 EvtRpcRegisterRemoteSubscription (Opnum 0)
    public static class EvtRpcRegisterRemoteSubscription extends RegisterRequest {
        public final String channelPath;
        public final String query;
        public final String bookmarkXml;
        public final int flags;

        public EvtRpcRegisterRemoteSubscription(String channelPath, String query, String bookmarkXml, int flags) {
            this.channelPath = channelPath;
            this.query = query;
            this.bookmarkXml = bookmarkXml;
            this.flags = flags;

            this.ptype = 0;
        }

        @Override
        public int getOpnum() { return 0; }

        @Override
        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(channelPath, 1);
            if (channelPath != null) {
                _dst.enc_ndr_string(channelPath);
            }
            _dst.enc_ndr_string(query);
            _dst.enc_ndr_referent(bookmarkXml, 1);
            if (bookmarkXml != null) {
                _dst.enc_ndr_string(bookmarkXml);
            }
            _dst.enc_ndr_long(flags);
        }

        @Override
        public void decode_out(NdrBuffer _src) throws NdrException {
            super.decode_out(_src);
        }

    }

    // 3.1.4.9 EvtRpcRemoteSubscriptionNextAsync (Opnum 1)
    public static class EvtRpcRemoteSubscriptionNextAsync extends EventResponse {
        public final int flags;

        public EvtRpcRemoteSubscriptionNextAsync(policy_handle handle, int numRequestedRecords, int flags) {
            super(handle, numRequestedRecords);
            this.flags = flags;

            this.ptype = 0;
        }

        @Override
        public int getOpnum() { return 1; }

        @Override
        public void encode_in(NdrBuffer _dst) throws NdrException {
            handle.encode(_dst);
            _dst.enc_ndr_long(numRequestedRecords);
            _dst.enc_ndr_long(flags);
        }

        @Override
        public void decode_out(NdrBuffer _src) throws NdrException {
            super.decode_out(_src);
        }
    }

    // 3.1.4.10 EvtRpcRemoteSubscriptionNext (Opnum 2)
    public static class EvtRpcRemoteSubscriptionNext extends EventResponse {
        public final int timeout;
        public final int flags;

        public EvtRpcRemoteSubscriptionNext(policy_handle handle, int numRequestedRecords, int timeout, int flags) {
            super(handle, numRequestedRecords);
            this.timeout = timeout;
            this.flags = flags;

            this.ptype = 0;
        }

        @Override
        public int getOpnum() { return 2; }

        @Override
        public void encode_in(NdrBuffer _dst) throws NdrException {
            handle.encode(_dst);
            _dst.enc_ndr_long(numRequestedRecords);
            _dst.enc_ndr_long(timeout);
            _dst.enc_ndr_long(flags);
        }

        @Override
        public void decode_out(NdrBuffer _src) throws NdrException {
            super.decode_out(_src);
        }
    }

    // 3.1.4.34 EvtRpcClose (Opnum 13)
    public static class EvtRpcClose extends DcerpcMessage {
        private final policy_handle handle;
        public int retVal = -1;

        public EvtRpcClose(policy_handle handle) {
            this.handle = handle;

            this.ptype = 0;
        }

        public int getOpnum() { return 13; }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            handle.encode(_dst);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            retVal = _src.dec_ndr_long();
        }
    }

    // 3.1.4.34 EvtRpcCancel (Opnum 14)
    public static class EvtRpcCancel extends DcerpcMessage {
        private final policy_handle control;
        public int retVal = -1;

        public EvtRpcCancel(policy_handle control) {
            this.control = control;

            this.ptype = 0;
        }

        public int getOpnum() { return 14; }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            control.encode(_dst);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            retVal = _src.dec_ndr_long();
        }
    }

    private static abstract class RegisterRequest extends DcerpcMessage {
        protected policy_handle handle;
        protected policy_handle control;
        public int queryChannelInfoSize;
        public EvtRpcQueryChannelInfo[] queryChannelInfo;
        public RpcInfo error;
        public int retVal = -1;

        @Override
        public void decode_out(NdrBuffer _src) throws NdrException {
            handle = new policy_handle();
            handle.decode(_src);

            control = new policy_handle();
            control.decode(_src);

            queryChannelInfoSize = _src.dec_ndr_long();

            int _queryChannelInfop = _src.dec_ndr_long();           // pointer
            if (_queryChannelInfop != 0) {
                int _queryChannelInfos = _src.dec_ndr_long();       // MaxCount
                if (_queryChannelInfos != queryChannelInfoSize) {
                    String msg = String.format("queryChannelInfoSize=%d != _queryChannelInfos=%d",
                            queryChannelInfoSize, _queryChannelInfos);
                    throw new NdrException(msg);
                }
                queryChannelInfo = new EvtRpcQueryChannelInfo[_queryChannelInfos];
                for (int _i = 0; _i < _queryChannelInfos; _i++) {
                    queryChannelInfo[_i] = new EvtRpcQueryChannelInfo();
                    queryChannelInfo[_i].decode(_src);
                }
            }

            error = new RpcInfo();
            error.decode(_src);

            retVal = _src.dec_ndr_long();
        }

        @Override
        public String toString() {
            return "channel info="+ Arrays.toString(queryChannelInfo) + ", error=" + error;
        }
    }

    private static abstract class EventResponse extends DcerpcMessage {
        protected final policy_handle handle;
        public final int numRequestedRecords;

        public int numActualRecords;
        public int[] eventDataIndices;
        public int[] eventDataSizes;
        public int resultBufferSize;
        public byte[] resultBuffer;
        public int retVal = -1;

        EventResponse(policy_handle handle, int numRequestedRecords) {
            this.handle = handle;
            if (numRequestedRecords > MAX_RPC_RECORD_COUNT) {
                String msg = String.format("request records = %d should be < MAX_RPD_RECORD_COUNT = %d",
                        numRequestedRecords, MAX_RPC_RECORD_COUNT);
                throw new IllegalArgumentException(msg);
            }
            this.numRequestedRecords = numRequestedRecords;
        }

        @Override
        public void decode_out(NdrBuffer _src) throws NdrException {
            numActualRecords = _src.dec_ndr_long();
            if (numActualRecords > numRequestedRecords) {
                String msg = String.format("numActualRecords=%d > numRequestedRecords=%d",
                        numActualRecords, numRequestedRecords);
                throw new NdrException(msg);
            }

            int _eventDataIndicesp = _src.dec_ndr_long();
            if (_eventDataIndicesp != 0) {
                int _eventDataIndicess = _src.dec_ndr_long();
                if (_eventDataIndicess != numActualRecords) {
                    String msg = String.format("numActualRecords=%d eventDataIndices=%d",
                            numActualRecords, _eventDataIndicess);
                    throw new NdrException(msg);
                }
                int _eventDataIndicesi = _src.index;
                _src.advance(4 * _eventDataIndicess);
                eventDataIndices = new int[_eventDataIndicess];
                _src = _src.derive(_eventDataIndicesi);
                for (int _i = 0; _i < _eventDataIndicess; _i++) {
                    eventDataIndices[_i] = _src.dec_ndr_long();
                }
            }

            int _eventDataSizesp = _src.dec_ndr_long();
            if (_eventDataSizesp != 0) {
                int _eventDataSizess = _src.dec_ndr_long();
                if (_eventDataSizess != numActualRecords) {
                    String msg = String.format("numActualRecords=%d eventDataSizess=%d",
                            numActualRecords, _eventDataSizess);
                    throw new NdrException(msg);
                }
                int _eventDataSizesi = _src.index;
                _src.advance(4 * _eventDataSizess);
                eventDataSizes = new int[_eventDataSizess];
                _src = _src.derive(_eventDataSizesi);
                for (int _i = 0; _i < _eventDataSizess; _i++) {
                    eventDataSizes[_i] = _src.dec_ndr_long();
                }
            }

            resultBufferSize = _src.dec_ndr_long();

            int _resultBufferp = _src.dec_ndr_long();
            if (_resultBufferp != 0) {
                int _resultBuffers = _src.dec_ndr_long();
                if (_resultBuffers != resultBufferSize) {
                    String msg = String.format("resultBufferSize=%d _resultBuffers=%d", resultBufferSize, _resultBuffers);
                    throw new NdrException(msg);
                }
                int _resultBufferi = _src.index;
                _src.advance(1 * _resultBuffers);
                resultBuffer = new byte[_resultBuffers];
                _src = _src.derive(_resultBufferi);
                for (int _i = 0; _i < _resultBuffers; _i++) {
                    resultBuffer[_i] = (byte) _src.dec_ndr_small();
                }
            }
            // TODO following work ?
            // System.arraycopy(_src.getBuffer(), _src.index, resultBuffer, 0, _resultBuffers);

            retVal = _src.dec_ndr_long();
        }

        @Override
        public String toString() {
            return "events = " + numActualRecords +
                    " indices = " + Arrays.toString(eventDataIndices) +
                    " sizes = " + Arrays.toString(eventDataSizes) +
                    " buffer size = " + resultBufferSize;
        }
    }

}
