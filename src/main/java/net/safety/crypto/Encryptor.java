package net.safety.crypto;

import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.*;
import java.security.spec.X509EncodedKeySpec;


public class Encryptor {
   private final byte[] publicKey;
   private final KeyPair pair;
   private byte[] key_second_person;

    public Encryptor(){
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("DH");
            generator.initialize(2048);
            KeyPair pair = generator.generateKeyPair();
            this.pair = pair;
            publicKey = pair.getPublic().getEncoded();
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setPublicKey(byte[] key_second_person){
        this.key_second_person = key_second_person;
    }

    public byte[] getPublicKey(){
        return publicKey;
    }

    public KeyPair getPair() {
        return pair;
    }

    private PublicKey generateKey(byte[] key_second_person){
        if (key_second_person == null){
            return null;
        }
        try {
            X509EncodedKeySpec spec = new X509EncodedKeySpec(key_second_person);
            KeyFactory factory = KeyFactory.getInstance("DH");
            PublicKey key = factory.generatePublic(spec);

            return key;
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public SecretKeySpec getSharedKey(){
        try {
            KeyAgreement agreement = KeyAgreement.getInstance("DH");
            agreement.init(pair.getPrivate());
            agreement.doPhase(generateKey(key_second_person), true);
            byte[] secret = agreement.generateSecret();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            return new SecretKeySpec(digest.digest(secret), "AES");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] solveMac(byte[] message){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(getSharedKey());
            return mac.doFinal(message);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public boolean isValidMac(SecretKeySpec sharedKey, byte[] encryptedMessage, byte[] sentMac){
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(sharedKey);
            byte[] mac_from_me = mac.doFinal(decrypt(encryptedMessage, sharedKey));
            return MessageDigest.isEqual(mac_from_me, sentMac);

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] encrypt(byte[] message, SecretKeySpec sharedKey){
        try {
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, sharedKey);
            return cipher.doFinal(message);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + " проблемы при шифровании");
        }
    }

    public byte[] decrypt(byte[] encryptedMessage, SecretKeySpec sharedKey){
        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, sharedKey);
            return cipher.doFinal(encryptedMessage);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage() + " проблема при расшифровке");
        }
    }

}
