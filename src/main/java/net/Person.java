package net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import net.safety.crypto.Certificate;
import net.safety.crypto.Encryptor;
import net.safety.crypto.Token;
import net.safety.io.FileManager;
import net.safety.io.KeySaver;
import net.safety.io.MessageSaver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.time.LocalTime;

public class Person {
    private static final Logger logger = LogManager.getLogger(Person.class); // класс для логирования
    private String ID;
    public static final int PORT = 8001;
    private final String IP;
  private final String URL;
  private final FileManager fileManager = new FileManager();
  private final MessageSaver saver = new MessageSaver();
  private final LocalTime time = LocalTime.now();
  private final KeySaver keySaver = new KeySaver();
  private final Encryptor encryptor = new Encryptor();
  private final Token token = new Token();
  private SecretKey ShredKey;
  private final PrivateKey privateKey;
  private final PublicKey publicKey;

  private HttpClient client;

    public Person(String IP){
        this.IP = IP;
        URL = "http://" + IP + ":" + PORT + "/" + ID;
        if (KeySaver.doGenerate()) {
            encryptor.generate(); // сделал без конструктора, чтобы лишний раз не генерировались ключи
            token.generate();
            keySaver.savePrivateKey(token.getPrivateKey(), token.getCertificate(token.getPublicKey()));

            privateKey = token.getPrivateKey();
            publicKey = token.getPublicKey();
            start();
        } else {
            ShredKey = keySaver.getSharedKey();
            privateKey = keySaver.getPrivateKey();
            publicKey = keySaver.getPublicKey();
        }
    }

    public String getURL(){
        return URL;
    }

    public void client(Path path_to_file){ // здесь я обращаюсь к другому пользователю
        logger.info("Отправка сообщения");
        byte[] message = encryptor.encrypt(fileManager.read(path_to_file), ShredKey); // кодирую сообщение
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("FileName", path_to_file.getFileName().toString())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(message))
                    .build();

            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            logger.info("Статус: " + response.statusCode() +  " отправлено");

            saver.setMessage(path_to_file.toString(), time); // сохраняю путь к файлу

        } catch (Exception e){
            logger.error(e + " возникла ошибка при отправке или соединении");
            throw new RuntimeException(e);
        }
    }

    private void start(){
        ID = token.getFingerPrint(token.getPublicKey()); // мой уникальный ID по публичному ключу токена
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL))
                    .header("start", "token")
                    .header("PublicKey", new String(token.getPublicKey().getEncoded(), StandardCharsets.UTF_8))
                    .header("PublicKeyForSharedSecret", encryptor.getPublicKey().toString())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(token.generateJWT(privateKey, publicKey)))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            System.out.println("Статус: " + response.statusCode() + " отправлено ");
            logger.info("Статус: " + response.statusCode() +  " отправлено");
            this.client = client;

        } catch (Exception e){
            logger.error(e + " Возникла ошибка при старте");
            throw new RuntimeException(e);
        }
    }

    public void server(){ // обрабатывает данные от другого пользователя
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/" + ID, new Handler()); // создаем способ чтения данных
            // вместо MyPath использовать PeerId
            server.setExecutor(null);
            server.start();

            logger.info("Сервер запущен");
        } catch (Exception e){
            logger.error(e);
            throw new RuntimeException(e);
        }
    }
    class Handler implements HttpHandler{ // класс для чтения данных

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())){
                try(InputStream input = exchange.getRequestBody();
                    OutputStream outputStream = exchange.getResponseBody()){ // получаю и читаю данные

                    if (exchange.getRequestHeaders().getFirst("start").equals("token")){
                        byte[] publicKey = exchange.getRequestHeaders().getFirst("PublicKey").getBytes();
                        if (!Token.isValidToken(new String(input.readAllBytes()), Encryptor.byteToPublicKey(publicKey))){
                            client.close();
                            logger.error("Токен этого пользователя недействителен");
                            throw new RuntimeException("Токен этого пользователя недействителен");
                        }
                    }

                    if (KeySaver.doGenerate()) { // формирую и сохраняю общий пароль если это надо делать
                        ShredKey = encryptor.getSharedKey(exchange.getRequestHeaders().
                                getFirst("PublicKeyForSharedSecret").getBytes(), encryptor.getPrivateKey());
                        keySaver.saveSharedKey(ShredKey);
                    }

                    fileManager.loading(encryptor.decrypt(input.readAllBytes(), ShredKey),
                            exchange.getRequestHeaders().getFirst("FileName")); // расшифровываю и сохраняю
                    byte[] answer = "Ответ".getBytes();
                    exchange.sendResponseHeaders(200, answer.length);
                    outputStream.write(answer);
                    System.out.println("загружено");
                } catch (IOException e){
                    logger.error(e);
                    throw new RuntimeException(e);
                }
            }
        }
    }
}
