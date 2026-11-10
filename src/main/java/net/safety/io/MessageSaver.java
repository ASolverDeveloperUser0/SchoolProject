package net.safety.io;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;

public class MessageSaver {
    String pathToDB = "";
    Connection connection;

    public MessageSaver(){
        try(Connection connection = DriverManager.getConnection(pathToDB);
            Statement statement = connection.createStatement()){
            this.connection = connection;
            String createSQLTable = "CREATE TABLE IF NOT EXISTS user (" +"\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + "\n" +
                    "name TEXT NOT NULL, " + "\n" +
                     "time TEXT NOT NULL);";

            statement.execute(createSQLTable);

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public void setTableValue(){
        String method = "INSERT INTO users(name) VALUES(?)";
        try(PreparedStatement statement = connection.prepareStatement(method)){
            statement.setString(1, "Hi!");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
