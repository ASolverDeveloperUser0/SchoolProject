import GUI.API;
import com.github.windpapi4j.WinDPAPI;
import net.safety.crypto.Encryptor;
import net.Person;
import net.safety.crypto.Token;
import net.safety.io.FileManager;
import net.safety.io.KeySaver;
import net.safety.io.MessageSaver;

import java.time.LocalTime;
import java.util.Arrays;

public class Main {
    public static void main(String[] args) {
        API api = new API();
        api.start((ip) -> {
            Person person = new Person(ip);
            return person;
        });
    }
}
