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
package jcifs.dcerpc;

import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.dcerpc.ndr.NdrException;

/*
 * @author Jitendra Kotamraju
 */
public class Auth3 extends DcerpcMessage {

    @Override
    public DcerpcException getResult() {
        throw new IllegalStateException("no response to Auth3 message");
    }

    public Auth3() {
        ptype = 16;
        flags = DCERPC_FIRST_FRAG | DCERPC_LAST_FRAG;
    }

    @Override
    public int getOpnum() {
        return 0;
    }

    @Override
    public void encode_in(NdrBuffer buf) throws NdrException {
        // pad - any 4 bytes
        buf.enc_ndr_long(0x56565656);
    }

    @Override
    public void decode_out(NdrBuffer buf) throws NdrException {
        throw new IllegalStateException("no response to Auth3 message");
    }
}
