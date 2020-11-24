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

import jcifs.dcerpc.DcerpcMessage;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;
import jcifs.dcerpc.ndr.NdrObject;
import jcifs.dcerpc.rpc.policy_handle;
import jcifs.dcerpc.rpc.uuid_t;

public class epm {

    public static String getSyntax() {
        return "e1af8308-5d1f-11c9-91a4-08002b14a0fa:3.0";
    }

    public static final int error_status_ok = 0;
    public static class ept_map extends DcerpcMessage {
        public uuid_t object;
        public twr_t map_tower;
        public policy_handle entry_handle;
        public int max_towers;
        public int num_towers;
        public twr_t[] towers;
        public int status;

        public int getOpnum() {
            return 3;
        }

        public ept_map(uuid_t object, twr_t map_tower, policy_handle entry_handle, int max_towers, int num_towers, twr_t[] towers, int status) {
            this.object = object;
            this.map_tower = map_tower;
            this.entry_handle = entry_handle;
            this.max_towers = max_towers;
            this.num_towers = num_towers;
            this.towers = towers;
            this.status = status;

            ptype = 0;
            flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
        }

        public void encode_in(NdrBuffer _dst) throws NdrException {
            _dst.enc_ndr_referent(object, 1);
            if (object != null) {
                object.encode(_dst);
            }

            _dst.enc_ndr_referent(map_tower, 1);
            if (map_tower != null) {
                map_tower.encode(_dst);
            }

            entry_handle.encode(_dst);
            _dst.enc_ndr_long(max_towers);
        }

        public void decode_out(NdrBuffer _src) throws NdrException {
            entry_handle.decode(_src);
            num_towers = _src.dec_ndr_long();
            int _towerss = _src.dec_ndr_long();
            _src.dec_ndr_long();
            int _towersl = _src.dec_ndr_long();
            _src.advance(4 * _towersl);
            int _towersi = _src.index;
            if (towers == null) {
                if (_towerss < 0 || _towerss > 0xFFFF) {
                    throw new NdrException("invalid array conformance");
                }

                towers = new twr_t[_towerss];
            }
            _src = _src.derive(_towersi);
            for (int _i = 0; _i < _towersl; _i++) {
                if (towers[_i] == null) {
                    towers[_i] = new twr_t();
                }
                towers[_i].decode(_src);
            }
            status = _src.dec_ndr_long();
        }
    }

    public static class twr_t extends NdrObject {

        public int tower_length;
        public byte[] tower_octet_string;

        public void encode(NdrBuffer _dst) throws NdrException {
            _dst.align(4);
            int _tower_octet_strings = tower_length;
            _dst.enc_ndr_long(_tower_octet_strings);
            _dst.enc_ndr_long(tower_length);
            int _tower_octet_stringi = _dst.index;
            _dst.advance(1 * _tower_octet_strings);

            _dst = _dst.derive(_tower_octet_stringi);
            for (int _i = 0; _i < _tower_octet_strings; _i++) {
                _dst.enc_ndr_small(tower_octet_string[_i]);
            }
        }

        public void decode(NdrBuffer _src) throws NdrException {
            _src.align(4);
            int _tower_octet_strings = (int) _src.dec_ndr_long();
            tower_length = _src.dec_ndr_long();
            int _tower_octet_stringi = _src.index;
            _src.advance(1 * _tower_octet_strings);

            if (tower_octet_string == null) {
                if (_tower_octet_strings < 0 || _tower_octet_strings > 0xFFFF) {
                    throw new NdrException("invalid array conformance");
                }
                tower_octet_string = new byte[_tower_octet_strings];
            }
            _src = _src.derive(_tower_octet_stringi);
            for (int _i = 0; _i < _tower_octet_strings; _i++) {
                tower_octet_string[_i] = (byte)_src.dec_ndr_small();
            }
        }
    }
}
