package net.safety.io;

import com.github.windpapi4j.WinDPAPI;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public class KeySaver {
    private final KeyStore keyStore;
    private final byte[] pass = getPassword();
    public KeySaver(){
        try {
            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\" + "Password"))){
                Files.createDirectory(Path.of(FileManager.getSystemPath() + "\\" + "Password"));
            }

            if (!Files.exists(Path.of(getPathToPassword()))){
                Files.createFile(Path.of(getPathToPassword()));
            }// проверка, существует ли такая папка и файл

            if (!Files.exists(Path.of(getPathToKeyStore()))){
                Files.createFile(Path.of(getPathToKeyStore()));
            }

            if (doGenerate()) {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                KeyGenerator key = KeyGenerator.getInstance("AES");
                char[] secretKey = key.generateKey().toString().toCharArray();
                keyStore.load(null, secretKey); // создаю хранилище ключей
                this.keyStore = keyStore;

                savePassword(secretKey);
                saveKeyStore(keyStore, secretKey);
            } else {
                KeyStore keyStore = KeyStore.getInstance("PKCS12");
                getKeyStore(keyStore, new String(getPassword()).toCharArray());
                this.keyStore = keyStore;
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static boolean doGenerate(){
        try {
            return Files.size(Path.of(getPathToPassword())) == 0 || Files.size(Path.of(getPathToKeyStore())) == 0;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public static String getPathToPassword(){
        return FileManager.getSystemPath() + "\\" + "Password\\DoNotDelete!.txt";
    }

    public static String getPathToKeyStore(){
        return FileManager.getSystemPath() + "\\Password\\KeyStore.p12";
    }

    private void saveKeyStore(KeyStore keyStore, char[] pass){ // сохраняю сам KeyStore
        try(FileOutputStream output = new FileOutputStream(getPathToKeyStore())){
            keyStore.store(output, pass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void getKeyStore(KeyStore keyStore, char[] pass){ // получаю KeyStore
        try(FileInputStream input = new FileInputStream(getPathToKeyStore())){
            keyStore.load(input, pass);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    private void savePassword(char[] password){ // сохраняю пароль к KeyStore
        try {
            if (WinDPAPI.isPlatformSupported()) { // WinDPAPI - инструмент для шифрования пароля, приуроченного к учетной записи Windows
                WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
                byte[] savedPassword = win.protectData(new String(password).getBytes());

                FileOutputStream output = new FileOutputStream(getPathToPassword());
                output.write(savedPassword);
                output.close();
            }
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public byte[] getPassword(){ // получаю пароль к KeyStore из файла
        try {
            FileInputStream input = new FileInputStream(getPathToPassword());
            byte[] decoded = input.readAllBytes();
            input.close();
            WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            // WinDPAPI инструмент Windows для шифрования пароля и/или сообщения
            return win.unprotectData(decoded);
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void saveSharedKey(SecretKeySpec secretKey){ // сохраняю общий секрет в KeyStore
        try {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter parameter = new KeyStore.PasswordProtection(new String(pass).toCharArray());
            //KeyStore - встроенное хранилище ключей
            keyStore.setEntry("shared-key-alias", entry, parameter);
            // Shared-key-alias - использую просто для обращения. Сертификат здесь не требуется
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public SecretKey getSharedKey(){ // читаю из KeyStore
        try {
            return (SecretKey) keyStore.getKey("shared-key-alias", new String(pass).toCharArray());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public void savePrivateKey(PrivateKey key, X509Certificate certificate){ // сохраняю ключ для токена
        try {
            keyStore.setKeyEntry("private-alias", key, new String(pass).toCharArray(),
                    new X509Certificate[]{certificate});
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public PrivateKey getPrivateKey(){ // получаю приватный ключ токена
        try{
           return (PrivateKey) keyStore.getKey("private-alias", new String(pass).toCharArray());
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public PublicKey getPublicKey(){
        try {
            Certificate certificate = keyStore.getCertificate("private-alias");
            return certificate.getPublicKey();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
