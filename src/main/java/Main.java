import GUI.API;
import com.github.windpapi4j.WinDPAPI;
import net.safety.crypto.Encryptor;
import net.Person;
import net.safety.crypto.Token;
import net.safety.io.FileManager;
import net.safety.io.KeySaver;
import net.safety.io.MessageSaver;

import java.time.LocalTime;

public class Main {
    public static void main(String[] args) {
       /* API api = new API();
        api.start((ip) -> {
            Person person = new Person(ip);
            return person;
        });

        */
        try {
            MessageSaver saver = new MessageSaver();
            System.out.println(saver.getPathForConnection());
            LocalTime time = LocalTime.now();
            saver.setMessage("Привет! Тествое поле", time);
            System.out.println(saver.getMessage().get(2) + " " + saver.getTime().get(2));
            saver.close();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }
}
