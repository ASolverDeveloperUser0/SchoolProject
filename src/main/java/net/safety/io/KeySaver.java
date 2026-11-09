package net.safety.io;

import com.github.windpapi4j.WinDPAPI;
import net.safety.crypto.Certificate;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

public class KeySaver {
    private final KeyStore keyStore;
    private final byte[] pass = getPassword();
    public KeySaver(){
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            KeyGenerator key = KeyGenerator.getInstance("AES");
            char[] secretKey = key.generateKey().toString().toCharArray();
            keyStore.load(null, secretKey); // создаю хранилище ключей
            this.keyStore = keyStore;

            savePassword(secretKey);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void savePassword(char[] password){
        try {
            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\" + "Password"))){
                Files.createDirectory(Path.of(FileManager.getSystemPath() + "\\" + "Password"));
            }

            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\" + "Password\\DoNotDelete!.txt"))){
                Files.createFile(Path.of(FileManager.getSystemPath() + "\\" + "Password\\DoNotDelete!.txt"));
            }

            if (WinDPAPI.isPlatformSupported()) { // WinDPAPI - инструмент для шифрования пароля, приуроченного к учетной записи Windows
                WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
                byte[] savedPassword = win.protectData(new String(password).getBytes());

                FileOutputStream output = new FileOutputStream(FileManager.getSystemPath() + "\\" + "Password\\DoNotDelete!.txt");
                output.write(savedPassword);
                output.close();
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getPassword(){
        try {
            FileInputStream input = new FileInputStream(FileManager.getSystemPath() + "\\" + "Password\\DoNotDelete!.txt");
            byte[] decoded = input.readAllBytes();
            input.close();
            WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            return win.unprotectData(decoded);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveSharedKey(SecretKeySpec secretKey){
        try {
            KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
            KeyStore.ProtectionParameter parameter = new KeyStore.PasswordProtection(new String(pass).toCharArray());
            keyStore.setEntry("shared-key-alias", entry, parameter);
            // Shared-key-alias - использую просто для обращения. Сертификат здесь не требуется
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public SecretKey getSharedKey(){
        try {
            return (SecretKey) keyStore.getKey("shared-key-alias", new String(pass).toCharArray());
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveKeys(PrivateKey key, X509Certificate certificate){
        try {
            keyStore.setKeyEntry("private-alias", key, new String(pass).toCharArray(),
                    new X509Certificate[]{certificate});
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
}
