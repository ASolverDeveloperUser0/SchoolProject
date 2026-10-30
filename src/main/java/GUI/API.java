package GUI;

import net.Person;

import java.nio.file.Path;
import java.util.Scanner;

public class API {
    Person person;
    public void start(Action action){
        Scanner scanner = new Scanner(System.in);
        String line = "";
        while (!line.equals("stop")){
            line = scanner.next();
            if (line.equals("send")){
                System.out.print("Пожалуйста, введите путь к файлу: ");
                String path = scanner.next();
                person.client(Path.of(path));
            }

            if (line.equals("start")){
                System.out.print("Введите IP адресата: ");
                String IP = scanner.next();
                this.person = action.Do(IP);
                person.server();
            }
        }
    }
}
