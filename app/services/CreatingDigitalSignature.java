package services;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Date;

public class CreatingDigitalSignature {
    public static void main(String args[]) throws Exception {

        String privateKeyString = "MD4CAQAwEAYHKoZIzj0CAQYFK4EEAAoEJzAlAgEBBCAua+T0a8jQQy9EdYYEkv5zCrDzILd8uEB9bif8Dn99Lg==";
        String timestampMessage = Long.toString(new Date().getTime());
        System.out.println(timestampMessage);

        String signatureECDSA = "SHA256withECDSA";

        PKCS8EncodedKeySpec privKeySpec = new PKCS8EncodedKeySpec(Base64.
                getDecoder().decode(privateKeyString));
        KeyFactory kf = KeyFactory.getInstance("EC");
        PrivateKey privateKey = kf.generatePrivate(privKeySpec);

        Signature signatureInstance = Signature.getInstance
                (signatureECDSA);

        signatureInstance.initSign(privateKey);

        signatureInstance.update(timestampMessage.getBytes("UTF-8"));

        byte[] signature = signatureInstance.sign();

        String signatureString = Base64.getEncoder().encodeToString
                (signature);

        System.out.println("Digital signature: " + signatureString);
    }
}
