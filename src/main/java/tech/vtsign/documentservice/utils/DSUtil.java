package tech.vtsign.documentservice.utils;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;

public class DSUtil {
    public static byte[] sign(byte[] document, PrivateKey privateKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initSign(privateKey);
        signature.update(document);
        return signature.sign();
    }

    public static boolean verify(byte[] document, byte[] digitalSignature, PublicKey publicKey) throws Exception {
        Signature signature = Signature.getInstance("SHA256withRSA");
        signature.initVerify(publicKey);
        signature.update(document);
        return signature.verify(digitalSignature);
    }
}
