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
package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcConstants;
import jcifs.dcerpc.msrpc.epm.ept_map;
import jcifs.dcerpc.msrpc.epm.twr_t;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.rpc;
import jcifs.util.Encdec;
import jcifs.dcerpc.UUID;

/*
 * @author Jitendra Kotamraju
 */
public class EpmMap extends ept_map {
    private static final UUID DCERPC_UUID_ZERO = new UUID("00000000-0000-0000-0000-000000000000");
    private static final rpc.policy_handle DCERPC_HANDLE_ZERO = new rpc.policy_handle();
    static {
        DCERPC_HANDLE_ZERO.type = 0;
        DCERPC_HANDLE_ZERO.uuid = DCERPC_UUID_ZERO;
    }

    private static final int DOD_TCP = 0x07;
    private static final int DOD_IP = 0x09;
    private static final int UUID = 0x0d;
    private static final int RPC_CONNECTION_ORIENTED = 0x0b;

    private int port = -1;

    public EpmMap(UUID uuid, int vers) {
        super(DCERPC_UUID_ZERO, to_tower(uuid, vers), DCERPC_HANDLE_ZERO, 4, 0, new twr_t[4], 0);
    }

    private static int enc_uuid(UUID uuid, byte[] buf, int bi) {
        int start = bi;
        bi += Encdec.enc_uint32le(uuid.time_low, buf, bi);
        bi += Encdec.enc_uint16le(uuid.time_mid, buf, bi);
        bi += Encdec.enc_uint16le(uuid.time_hi_and_version, buf, bi);
        buf[bi++] = uuid.clock_seq_hi_and_reserved;
        buf[bi++] = uuid.clock_seq_low;
        System.arraycopy(uuid.node, 0, buf, bi, 6);
        bi += 6;
        return bi - start;
    }

    public void decode_out(NdrBuffer src) throws NdrException {
        super.decode_out(src);
        this.port = getTcpPort(this.towers[0]);
    }

    public int getPort() {
        return port;
    }

    private static int getTcpPort(twr_t twr) {
        byte[] buf = twr.tower_octet_string;
        int bi = 2;     // skip floor count bytes

        // Floor1 UUID, 25 bytes = LHS(2) + protocol(1) + UUID(16) + Version(2) + RHS(2) + Version Minor(2)
        bi += 25;
        // Floor2 UUID: 32bit NDR, 25 bytes = LHS(2) + protocol(1) + UUID(16) + Version(2) + RHS(2) + Version Minor(2)
        bi += 25;
        // Floor3 RPC Connection-oriented protocol, 7 bytes = LHS(2) + protocol(1) + RHS(2) + Version Minor(2)
        bi += 7;
        // Floor4 TCP
        bi = bi + 2;
        int protocol = buf[bi++] & 0xFF;
        assert protocol == DOD_TCP;
        bi = bi + 2;
        return Short.toUnsignedInt(Encdec.dec_uint16be(buf, bi));
    }

    private static twr_t to_tower(UUID uuid, int version) {
        byte[] buf = new byte[128];
        int bi = 0;
        bi += Encdec.enc_uint16le((short)5, buf, bi);       // Number of floors = 5

        // Floor 1 UUID with given uuid
        bi += Encdec.enc_uint16le((short)19, buf, bi);      // LHS length = 19
        buf[bi++] = UUID;
        bi += enc_uuid(uuid, buf, bi);
        bi += Encdec.enc_uint16le((short)version, buf, bi); // Version
        bi += Encdec.enc_uint16le((short)2, buf, bi);       // RHS length = 2
        bi += Encdec.enc_uint16le((short)0, buf, bi);       // Version Minor = 0

        // Floor 2 UUID: 32bit NDR
        bi += Encdec.enc_uint16le((short)19, buf, bi);       // LHS length = 19
        buf[bi++] = UUID;
        bi += enc_uuid(DcerpcConstants.DCERPC_UUID_SYNTAX_NDR, buf, bi);
        bi += Encdec.enc_uint16le((short)2, buf, bi);       // Version = 2.00
        bi += Encdec.enc_uint16le((short)2, buf, bi);       // RHS length = 2
        bi += Encdec.enc_uint16le((short)0, buf, bi);       // Version Minor = 0

        // Floor 3 RPC connection-oriented protocol
        bi += Encdec.enc_uint16le((short)1, buf, bi);       // LHS length = 1
        buf[bi++] = RPC_CONNECTION_ORIENTED;
        bi += Encdec.enc_uint16le((short)2, buf, bi);       // RHS length = 2
        bi += Encdec.enc_uint16le((short)0, buf, bi);       // Version Minor = 0

        // Floor 4 TCP Port:0
        bi += Encdec.enc_uint16le((short)1, buf, bi);       // LHS length = 1
        buf[bi++] = DOD_TCP;
        bi += Encdec.enc_uint16le((short)2, buf, bi);       // RHS length = 2
        bi += Encdec.enc_uint16be((short)0, buf, bi);       // TCP Port = 0

        // Floor 5 IP:0.0.0.0
        bi += Encdec.enc_uint16le((short)1, buf, bi);       // LHS length = 1
        buf[bi++] = DOD_IP;
        bi += Encdec.enc_uint16le((short)4, buf, bi);       // RHS length = 4
        bi += Encdec.enc_uint32le(0, buf, bi);            // IP: 0.0.0.0

        twr_t ret = new twr_t();
        ret.tower_length = bi;
        ret.tower_octet_string = buf;
        return ret;
    }

}
