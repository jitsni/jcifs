/* jcifs msrpc client library in Java
 * Copyright (C) 2006  "Michael B. Allen" <jcifs at samba dot org>
 *                     "Eric Glass" <jcifs at samba dot org>
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
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
