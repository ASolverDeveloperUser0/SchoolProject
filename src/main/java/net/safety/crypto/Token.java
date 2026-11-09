package net.safety.crypto;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;

import java.security.*;
import java.security.cert.X509Certificate;
import java.util.HexFormat;

public class Token {
    private final PublicKey publicKey;
    private final PrivateKey privateKey;
    private final KeyPair pair;
    public Token(){
        try {
            KeyPairGenerator pairGenerator = KeyPairGenerator.getInstance("RSA");
            // RSA - метод, который требуется для генерации сертификата (SHA256withRSA)
            pairGenerator.initialize(2048); // если меньше, то кидает исключение
            KeyPair pair = pairGenerator.generateKeyPair();
            this.pair = pair;
            privateKey = pair.getPrivate();
            publicKey = pair.getPublic();
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public X509Certificate getCertificate(){ //генерирую сертификат
        Certificate certificate = new Certificate();
        return certificate.generateSelfCertificate(pair, "123456789");
    }

    public String getFingerPrint(PublicKey publicKey){
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] fingerprint = digest.digest(publicKey.getEncoded());
            return HexFormat.of().formatHex(fingerprint);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] generateJWT(){
        String jwt = Jwts.builder()
                .subject("")
                .claim("", "")
                .signWith(privateKey, Jwts.SIG.ES256)
                .compact();
        return jwt.getBytes();
    }



    public boolean isValidToken(String token, PublicKey key){
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
