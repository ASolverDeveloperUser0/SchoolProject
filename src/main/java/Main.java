import GUI.API;
import com.github.windpapi4j.WinDPAPI;
import net.safety.crypto.Encryptor;
import net.Person;
import net.safety.crypto.Token;
import net.safety.io.FileManager;
import net.safety.io.KeySaver;

public class Main {
    public static void main(String[] args) {
       /* API api = new API();
        api.start((ip) -> {
            Person person = new Person(ip);
            return person;
        });

        */
        Encryptor encryptor = new Encryptor();
        Encryptor encryptor1 = new Encryptor();
        encryptor.setPublicKey(encryptor1.getPublicKey());
        encryptor1.setPublicKey(encryptor.getPublicKey());

       byte[] hello = encryptor.encrypt("Привет!".getBytes(), encryptor.getSharedKey());
        String s = new String(encryptor1.decrypt(hello, encryptor1.getSharedKey()));
        System.out.println(s);
    }
}
