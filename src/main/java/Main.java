import GUI.API;
import net.Person;

public class Main {
    public static void main(String[] args) {
        API api = new API();
        api.start((ip) -> {
            Person person = new Person(ip);
            return person;
        });
    }
}
