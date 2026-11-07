import GUI.API;
import net.crypto.Encryptor;
import net.Person;

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

       byte[] hello = encryptor.encrypt("Привет Vddvsdvskdmlac amsdjasmdaj aivjkalmcaisjv maiosjiamoscjasoijcasvavdasv!".getBytes());
        String s = new String(encryptor1.decrypt(hello));
        System.out.println(s);
    }
}
