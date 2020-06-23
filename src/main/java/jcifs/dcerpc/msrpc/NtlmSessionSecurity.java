package jcifs.dcerpc.msrpc;

import jcifs.dcerpc.DcerpcException;
import jcifs.util.Encdec;
import jcifs.util.HMACT64;
import jcifs.util.RC4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
 * NTLM2 Session Security as per
 * http://davenport.sourceforge.net/ntlm.html#ntlm2SessionSecurity
 *
 * @author Jitendra Kotamraju
 */
class NtlmSessionSecurity {
    private static final byte[] CLIENT_TO_SERVER_SIGNING =
            "session key to client-to-server signing key magic constant\u0000".getBytes();
    private static final byte[] SERVER_TO_CLIENT_SIGNING =
            "session key to server-to-client signing key magic constant\u0000".getBytes();
    private static final byte[] CLIENT_TO_SERVER_SEALING =
            "session key to client-to-server sealing key magic constant\u0000".getBytes();
    private static final byte[] SERVER_TO_CLIENT_SEALING =
            "session key to server-to-client sealing key magic constant\u0000".getBytes();

    private final boolean keyExch;

    private final byte[] clientSigningKey;
    private final RC4 clientSealingHandle;
    private int clientSequence = 0;

    private final byte[] serverSigningKey;
    private final RC4 serverSealingHandle;
    private int serverSequence = 0;

    private static byte[] computeKey(byte[] sessionKey, byte[] constant) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(sessionKey);
        md5.update(constant);
        return md5.digest();
    }

    NtlmSessionSecurity(byte[] sessionKey, boolean keyExch) throws DcerpcException {
        this.keyExch = keyExch;

        try {
            clientSigningKey = computeKey(sessionKey, CLIENT_TO_SERVER_SIGNING);
            byte[] clientSealingKey = computeKey(sessionKey, CLIENT_TO_SERVER_SEALING);
            clientSealingHandle = new RC4(clientSealingKey);

            serverSigningKey = computeKey(sessionKey, SERVER_TO_CLIENT_SIGNING);
            byte[] serverSealingKey = computeKey(sessionKey, SERVER_TO_CLIENT_SEALING);
            serverSealingHandle = new RC4(serverSealingKey);
        } catch (NoSuchAlgorithmException ne) {
            throw new DcerpcException("Failed to compute NTLM signing and sealing keys", ne);
        }
    }

    private static byte[] computeSignature(byte[] signingKey, int sequence, byte[] data, int off, int len) {
        byte[] signature = new byte[16];
        Encdec.enc_uint32le(1, signature, 0);
        Encdec.enc_uint32le(sequence, signature, 12);
        HMACT64 hmac = new HMACT64(signingKey);
        hmac.update(signature, 12, 4);
        hmac.update(data, off, len);
        System.arraycopy(hmac.digest(), 0, signature, 4, 8);
        return signature;
    }

    /*
     * buf - entire packet (header + body + sec_trailer + auth_data)
     * sign would sign header + body + sec_trailer
     */
    void sign(byte[] buf, int off, int len) {
        byte[] signature = computeSignature(clientSigningKey, clientSequence, buf, off, len - 16);   // except auth_data

        if (keyExch) {
            clientSealingHandle.update(signature, 4, 8, signature, 4);
        }

        // copy signature into auth_data
        System.arraycopy(signature, 0, buf, off + len - 16, signature.length);

        ++clientSequence;
    }

    /*
     * buf - entire packet (header + body + sec_trailer + auth_data)
     * seal would seal only body
     * sign would sign header + body + sec_trailer
     */
    void seal(byte[] buf, int off, int len) {
        byte[] signature = computeSignature(clientSigningKey, clientSequence, buf, off, len - 16);   // except auth_data

        // encrypt only body (in place)
        clientSealingHandle.update(buf, off + 24, len - 24 - 8 - 16, buf, off + 24);

        if (keyExch) {
            clientSealingHandle.update(signature, 4, 8, signature, 4);
        }

        // copy signature into auth_data
        System.arraycopy(signature, 0, buf, off + len - 16, signature.length);

        ++clientSequence;
    }

    void verifySign(byte[] buf, int off, int len) throws DcerpcException {
        if (keyExch) {
            serverSealingHandle.update(buf, off + len - 16 + 4, 8, buf, off + len - 16 + 4);
        }

        // verify signature
        byte[] signature = computeSignature(serverSigningKey, serverSequence, buf, off, len - 16);   // except auth_data
        if (!equals(signature, 0, buf, len - 16, 16)) {
            throw new DcerpcException("NTLM Signature mismatch for received msg");
        }

        ++serverSequence;
    }

    /*
     * buf - entire packet (header + body + sec_trailer + auth_data)
     * unseal would seal only body
     */
    void unseal(byte[] buf, int off, int len) throws DcerpcException {
        // decrypt only body (in place)
        serverSealingHandle.update(buf, off + 24, len - 24 - 8 - 16, buf, off + 24);

        if (keyExch) {
            serverSealingHandle.update(buf, off + len - 16 + 4, 8, buf, off + len - 16 + 4);
        }

        // verify signature
        byte[] signature = computeSignature(serverSigningKey, serverSequence, buf, off, len - 16);   // except auth_data
        if (!equals(signature, 0, buf, off + len - 16, 16)) {
            throw new DcerpcException("NTLM Signature mismatch for received msg");
        }

        ++serverSequence;
    }

    private boolean equals(byte[] a, int aFromIndex, byte[] b, int bFromIndex, int length) {
        for (int i=0; i < length; i++) {
            if (a[aFromIndex + i] != b[bFromIndex + i]) {
                return false;
            }
        }
        return true;
    }

}