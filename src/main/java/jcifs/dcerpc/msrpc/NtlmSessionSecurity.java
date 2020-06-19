package jcifs.dcerpc.msrpc;

import jcifs.util.Encdec;
import jcifs.util.HMACT64;
import jcifs.util.RC4;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/*
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
    private final byte[] signingKey;
    private final byte[] sealingKey;
    private final RC4 rc4;

    private int sequenceNumber = 0;

    private static byte[] computeKey(byte[] sessionKey, byte[] constant) throws NoSuchAlgorithmException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(sessionKey);
        md5.update(constant);
        return md5.digest();
    }

    NtlmSessionSecurity(byte[] sessionKey, boolean isClientToServer, boolean keyExch) {
        this.keyExch = keyExch;

        try {
            signingKey = computeKey(sessionKey,
                    isClientToServer ? CLIENT_TO_SERVER_SIGNING : SERVER_TO_CLIENT_SIGNING);
            sealingKey = computeKey(sessionKey,
                    isClientToServer ? CLIENT_TO_SERVER_SEALING : SERVER_TO_CLIENT_SEALING);
            rc4 = new RC4(sealingKey);
        } catch (NoSuchAlgorithmException ne) {
            throw new RuntimeException("Failed to compute NTLM signing and sealing keys", ne);
        }
    }

    private byte[] computeSignature(byte[] data, int off, int len) {
        byte[] signature = new byte[16];
        Encdec.enc_uint32le(1, signature, 0);
        Encdec.enc_uint32le(sequenceNumber, signature, 12);
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
        byte[] signature = computeSignature(buf, off, len - 16);   // except auth_data

        if (keyExch) {
            rc4.update(signature, 4, 8, signature, 4);
        }

        // copy signature into auth_data
        System.arraycopy(signature, 0, buf, len - 16, signature.length);

        ++sequenceNumber;
    }

    /*
     * buf - entire packet (header + body + sec_trailer + auth_data)
     * seal would seal only body
     * sign would sign header + body + sec_trailer
     */
    void seal(byte[] buf, int off, int len) {
        byte[] signature = computeSignature(buf, off, len - 16);   // except auth_data

        // encrypt only body (in place)
        rc4.update(buf, off + 24, len - 24 - 8 - 16, buf, off + 24);

        if (keyExch) {
            rc4.update(signature, 4, 8, signature, 4);
        }

        // copy signature into auth_data
        System.arraycopy(signature, 0, buf, len - 16, signature.length);

        ++sequenceNumber;
    }

    void verifySign(byte[] buf, int off, int len) {
//        byte[] signature = new byte[16];
//        System.arraycopy(buf, len - 16, signature, 0, 16);
//
//        byte[] signatureExpected = computeSignature(buf, off, len - 16);   // except auth_data
//
//        if (keyExch) {
//            rc4.update(signatureExpected, 4, 8, signatureExpected, 4);
//        }
//
//        if (!Arrays.equals(signatureExpected, signature)) {
//            throw new RuntimeException("Signature doesn't match");
//        }
//
//        ++sequenceNumber;
    }

}