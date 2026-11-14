import GUI.API;
import com.github.windpapi4j.WinDPAPI;
import net.safety.crypto.Encryptor;
import net.Person;
import net.safety.crypto.Token;
import net.safety.io.FileManager;
import net.safety.io.KeySaver;
import net.safety.io.MessageSaver;

public class Main {
    public static void main(String[] args) {
       /* API api = new API();
        api.start((ip) -> {
            Person person = new Person(ip);
            return person;
        });

        */
        MessageSaver saver = new MessageSaver();
        System.out.println(saver.getPathForConnection());
        saver.setTableValue();
        System.out.println(saver.getTableValue());
    }
}
