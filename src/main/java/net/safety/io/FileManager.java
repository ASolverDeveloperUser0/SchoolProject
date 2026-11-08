package net.safety.io;

import com.github.windpapi4j.WinDPAPI;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyGenerator;
import javax.crypto.spec.SecretKeySpec;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;

public class FileManager {
    private static final Logger logger = LogManager.getLogger(FileManager.class);
    private final KeyStore keyStore;
    private final char[] secretKey;

    public FileManager(){
        try {
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            KeyGenerator key = KeyGenerator.getInstance("EC");
            char[] secretKey = key.generateKey().toString().toCharArray();
            this.secretKey = secretKey;
            keyStore.load(null, secretKey);
            this.keyStore = keyStore;

            savePassword(secretKey);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private String getPathForLoading(){
        return Paths.get("").toAbsolutePath() + "\\" + "Загрузки";
    }

    private String getSystemPath(){
        return Paths.get("").toAbsolutePath().toString();
    }

    private void loading(byte[] bytes, String fullName){ // скачиваю файл от другого пользователя в загрузки
        try {
            Path path = Path.of(getPathForLoading());
            if (!Files.exists(path)) { // Проверяю, есть ли папка загрузки. В противном случае создаю эту папку
                Files.createDirectory(path);
            }
        } catch (IOException e){
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        try (FileOutputStream output = new FileOutputStream(getPathForLoading() + "/" + fullName)) {
            if (bytes.length > 0) {
                output.write(bytes);
            }

            System.out.println("Сохранено в загрузки");
            logger.info("Успешно сохранено");
        } catch (IOException e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
    }

    public void saveKeys(){

    }

    public void saveSharedKey(SecretKeySpec secretKey){
        KeyStore.SecretKeyEntry entry = new KeyStore.SecretKeyEntry(secretKey);
        KeyStore.ProtectionParameter parameter = new KeyStore.PasswordProtection(this.secretKey);
    }

    public void savePassword(char[] password){
        try {
            if (!Files.exists(Path.of(getSystemPath() + "\\" + "Password"))){
                Files.createDirectory(Path.of(getSystemPath() + "\\" + "Password"));
            }

            if (!Files.exists(Path.of(getSystemPath() + "\\" + "Password\\DoNotDelete!.txt"))){
                Files.createFile(Path.of(getSystemPath() + "\\" + "Password\\DoNotDelete!.txt"));
            }

            if (WinDPAPI.isPlatformSupported()) {
                WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
                byte[] savedPassword = win.protectData(new String(password).getBytes());

                FileOutputStream output = new FileOutputStream(getSystemPath() + "\\" + "Password\\DoNotDelete!.txt");
                output.write(savedPassword);
                output.close();
            }
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public byte[] getPassword(){
        try {
            FileInputStream input = new FileInputStream(getSystemPath() + "\\" + "Password\\DoNotDelete!.txt");
            byte[] decoded = input.readAllBytes();
            input.close();
            WinDPAPI win = WinDPAPI.newInstance(WinDPAPI.CryptProtectFlag.CRYPTPROTECT_UI_FORBIDDEN);
            return win.unprotectData(decoded);
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private byte[] read(Path path_to_file){ // читаю для client
        try(FileInputStream input = new FileInputStream(path_to_file.toString())){
            return input.readAllBytes();
        } catch (IOException e){
            logger.error(e.getMessage() + " (Возникла ошибка при чтении для Client)");
            throw new RuntimeException(e.getMessage());
        }
    }
}
