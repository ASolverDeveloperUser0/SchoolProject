package net.safety.io;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileManager {
    private static final Logger logger = LogManager.getLogger(FileManager.class);

    public FileManager(){

    }

    private String getPathForLoading(){
        return Paths.get("").toAbsolutePath() + "\\" + "Загрузки";
    }

    public static String getSystemPath(){
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

    private byte[] read(Path path_to_file){ // читаю для client
        try(FileInputStream input = new FileInputStream(path_to_file.toString())){
            return input.readAllBytes();
        } catch (IOException e){
            logger.error(e.getMessage() + " (Возникла ошибка при чтении для Client)");
            throw new RuntimeException(e.getMessage());
        }
    }
}
