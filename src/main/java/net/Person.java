package net;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Person {
    public static String PATH; // путь файла, который хочу отправить
    private static final String PATH_FOR_LOADING = "D:\\Вадим\\Java\\Project\\P2P\\Загрузки"; // путь к загрузкам
    private final String PersonalPath = "128";
    private final String MyPath = "127";
    public static final int PORT = 8001;
    private final String IP;
  private final String URL_FROM_FRIENDS;

    public Person(String IP){
        this.IP = IP;
        URL_FROM_FRIENDS = "http://" + IP + ":" + PORT + "/" + 127;
        System.out.println("Загрузки: " + getPathForLoading());
    }

    public void client(Path path_to_file){ // здесь я обращаюсь к другому пользователю
        System.out.println("Отправляется к: " + URL_FROM_FRIENDS);
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL_FROM_FRIENDS))
                    .header("FileName", path_to_file.getFileName().toString())
                    .POST(HttpRequest.BodyPublishers.ofByteArray(read(path_to_file)))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<byte[]> response = client.send(request, HttpResponse.BodyHandlers.ofByteArray());
            System.out.println("Статус: " + response.statusCode() + " отправлено ");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private byte[] read(Path path_to_file){ // читаю для client
        try(FileInputStream input = new FileInputStream(path_to_file.toString())){
            return input.readAllBytes();
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
    }

    private void loading(byte[] bytes, String fullName){ // скачиваю файл от другого пользователя в загрузки
        try {
            Path path = Path.of(getPathForLoading());
            if (!Files.exists(path)) { // Проверяю, есть ли папка загрузки. В противном случае создаю эту папку
                Files.createDirectory(path);
            }
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
        try (FileOutputStream output = new FileOutputStream(getPathForLoading() + "/" + fullName)) {
            if (bytes.length > 0) {
                output.write(bytes);
            }

            System.out.println("Сохранено в загрузки");
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public void server(){ // обрабатывает данные от другого пользователя
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", PORT), 0);
            server.createContext("/" + MyPath, new Handler()); // создаем способ чтения данных
            server.setExecutor(null);
            server.start();
            System.out.println("Сервер запущен!");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    class Handler implements HttpHandler{ // класс для чтения данных

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())){
                try(InputStream input = exchange.getRequestBody();
                    OutputStream outputStream = exchange.getResponseBody()){ // получаю и читаю данные
                    loading(input.readAllBytes(), exchange.getRequestHeaders().getFirst("FileName"));
                    byte[] answer = "Ответ".getBytes();
                    exchange.sendResponseHeaders(200, answer.length);
                    outputStream.write(answer);
                    System.out.println("загружено");
                } catch (IOException e){
                    throw new RuntimeException(e.getMessage());
                }
            }
        }
    }

    private String getPathForLoading(){
        return Paths.get("").toAbsolutePath().toString() + "\\" + "Загрузки";
    }
}
