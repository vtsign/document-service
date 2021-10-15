package tech.vtsign.documentservice.utils;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyReaderUtil {
    public static PublicKey getPublicKey(byte[] keyBytes) throws Exception {

        if (keyBytes != null) {
            X509EncodedKeySpec spec =
                    new X509EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePublic(spec);
        }
        return null;
    }

    public static PrivateKey getPrivateKey(byte[] keyBytes) throws Exception {
        if (keyBytes != null) {
            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        }
        return null;
    }
}
