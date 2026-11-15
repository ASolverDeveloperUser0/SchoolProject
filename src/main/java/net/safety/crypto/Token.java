package net.safety.crypto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HexFormat;

public class Token {
    private PublicKey publicKey;
    private PrivateKey privateKey;
    private KeyPair pair;
    public void generate(){
        try {
            KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            // RSA - метод, который требуется для генерации сертификата (SHA256withRSA)
            pairGenerator.initialize(2048); // если меньше, то кидает исключение
            KeyPair pair = pairGenerator.generateKeyPair();
            this.pair = pair;
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate getCertificate(PublicKey publicKey){ //генерирую сертификат
        Certificate certificate = new Certificate();
        return certificate.generateSelfCertificate(pair, getFingerPrint(publicKey));
        // в качестве ID я использую просто отпечаток публичного ключа
    }

    public String getFingerPrint(PublicKey publicKey){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fingerprint = digest.digest(publicKey.getEncoded());
            return HexFormat.of().formatHex(fingerprint);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public byte[] generateJWT(PrivateKey privateKey, PublicKey publicKey){
        String jwt = Jwts.builder()
                .subject(getFingerPrint(publicKey))
                .claim("purpose", "auth")
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
        return jwt.getBytes();
    }



    public static boolean isValidToken(String token, PublicKey key){
        try {
            Jws<Claims> jws = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e){
            return false;
        }
    }
}
