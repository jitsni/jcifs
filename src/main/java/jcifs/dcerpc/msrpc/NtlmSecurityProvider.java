package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcException;
import jcifs.dcerpc.DcerpcSecurityProvider;
import jcifs.dcerpc.ndr.NdrBuffer;
import jcifs.smb.NtlmContext;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;

/*
 * @author Jitendra Kotamraju
 */
public class NtlmSecurityProvider implements DcerpcSecurityProvider {
    private static final int MSRPC_REQUEST = 0x00;
    private static final int MSRPC_RESPONSE = 0x02;
    private static final int MSRPC_BIND = 0x0B;
    private static final int MSRPC_BINDACK = 0x0C;
    private static final int MSRPC_AUTH3 = 0x10;

    // Auth Types - Security Providers
    private static final int RPC_C_AUTHN_WINNT = 0x0A;

    // Auth Levels
    private static final int RPC_C_AUTHN_LEVEL_PKT_INTEGRITY = 5;
    private static final int RPC_C_AUTHN_LEVEL_PKT_PRIVACY   = 6;

    private byte[] type3;

    private final NtlmContext ntlmContext;
    private NtlmSessionSecurity sessionSecurity;
    private boolean privacy;

    public NtlmSecurityProvider(NtlmPasswordAuthentication auth) {
        ntlmContext = new NtlmContext(auth, true);
    }

    // secures BIND, AUTH3, REQUEST messages
    public void wrap(NdrBuffer outgoing) throws DcerpcException {
        try {
            outgoing.setIndex(2);
            int ptype = outgoing.dec_ndr_small();
            outgoing.setIndex(outgoing.getLength());
            // sec_trailer structure MUST be 4-byte aligned
            int auth_pad_length = outgoing.align(4, (byte)0);

            // write sec_trailer structure
            outgoing.setIndex(outgoing.getLength());
            outgoing.enc_ndr_small(RPC_C_AUTHN_WINNT);       // auth_type = NTLMSSP
            outgoing.enc_ndr_small(                          // auth_level
                    privacy ? RPC_C_AUTHN_LEVEL_PKT_PRIVACY : RPC_C_AUTHN_LEVEL_PKT_INTEGRITY);
            outgoing.enc_ndr_small(auth_pad_length);
            outgoing.enc_ndr_small(0);                     // auth_reserved
            outgoing.enc_ndr_long(79231);                  // auth_context_id

            if (ptype == MSRPC_BIND || ptype == MSRPC_AUTH3) {
                byte[] token = (ptype == MSRPC_BIND)
                        ? ntlmContext.initSecContext(new byte[0], 0, 0) // BIND has Type1Message token
                        : type3;                                        // AUTH3 has Type3Message token
                outgoing.writeOctetArray(token, 0, token.length);

                outgoing.setIndex(8);
                outgoing.enc_ndr_short(outgoing.getLength());   // revised frag_length
                outgoing.enc_ndr_short(token.length);           // revised auth_length
            } else if (ptype == MSRPC_REQUEST) {
                int auth_length = 16;       // signature
                int frag_length = outgoing.getLength() + auth_length;
                outgoing.setIndex(8);
                outgoing.enc_ndr_short(frag_length);
                outgoing.enc_ndr_short(auth_length);
                outgoing.setIndex(16);
                outgoing.enc_ndr_long(frag_length - 8 - auth_length - 24);  // revised alloc hint

                if (privacy) {
                    sessionSecurity.seal(outgoing.getBuffer(), 0, frag_length);
                } else {
                    sessionSecurity.sign(outgoing.getBuffer(), 0, frag_length);
                }
                outgoing.setIndex(outgoing.getLength());
                outgoing.advance(auth_length);
            }
        } catch (SmbException e) {
            throw new DcerpcException("NTLM Security Provider wrap failure", e);
        }
    }

    // verifies BINDACK, RESPONSE messages
    public void unwrap(NdrBuffer incoming) throws DcerpcException {
        try {
            incoming.setIndex(2);
            int ptype = incoming.dec_ndr_small();
            incoming.setIndex(8);
            int frag_length = incoming.dec_ndr_short();
            int auth_length = incoming.dec_ndr_short();
            frag_length -= 8 + auth_length;

            if (ptype == MSRPC_BINDACK) {
                byte[] token = new byte[auth_length];
                System.arraycopy(incoming.getBuffer(), frag_length + 8, token, 0, auth_length); // Type2Message
                type3 = ntlmContext.initSecContext(token, 0, token.length);
                sessionSecurity = new NtlmSessionSecurity(ntlmContext.getSigningKey(), true, true);
            } else if (ptype == MSRPC_RESPONSE) {
                if (privacy) {
                    throw new UnsupportedOperationException("TODO");
                } else {
                    sessionSecurity.verifySign(incoming.getBuffer(), 0, incoming.getLength());
                }
            }

            incoming.setIndex(8);
            incoming.enc_ndr_short(frag_length);
            incoming.enc_ndr_short(0);
            incoming.setLength(frag_length);
        } catch (SmbException e) {
            throw new DcerpcException("NTLM Security Provider unwrap failure", e);
        }
    }
}
