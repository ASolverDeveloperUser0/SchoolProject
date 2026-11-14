package net.safety.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.util.Calendar;

public class MessageSaver {
   private final Connection connection;
   private final Calendar calendar = Calendar.getInstance();

    public MessageSaver(){
        try {
            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\DataBase"))) {
                Files.createDirectory(Path.of(FileManager.getSystemPath() + "\\DataBase"));
            }

            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR)))){
                Files.createDirectory(Path.of(FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR)));
            }

            if (!Files.exists(Path.of(FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR)
            + "\\" + calendar.get(Calendar.MONTH)))){
                Files.createDirectory(Path.of(FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR)
                        + "\\" + calendar.get(Calendar.MONTH)));
            }
            if (!Files.exists(Path.of(getPath()))){
                Files.createFile(Path.of(getPath()));
            }
        } catch (IOException e){
            throw new RuntimeException(e.getMessage());
        }
//Проблема заключается в том, что connection закрывает свой ресурс. То есть он должен продолжать работу и закрыться когда-нибудь потом
        try {
            Connection connection = DriverManager.getConnection(getPathForConnection());
            Statement statement = connection.createStatement();
            this.connection = connection;
            String createSQLTable = "CREATE TABLE IF NOT EXISTS user (" +"\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + "\n" +
                    "name TEXT NOT NULL);";

            statement.execute(createSQLTable);

        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }
    public String getPath(){
        return FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR) +
                "\\" + calendar.get(Calendar.MONTH) + "\\" + calendar.get(Calendar.DAY_OF_MONTH) + ".db";
    }

    public String getPathForConnection(){
        return "jdbc:sqlite:" + FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR) +
                "\\" + calendar.get(Calendar.MONTH) + "\\" + calendar.get(Calendar.DAY_OF_MONTH) + ".db";
    }

    public String getPath(Calendar calendar){ // для получения пути в конкретную дату, если есть
        return FileManager.getSystemPath() + "\\DataBase\\" + calendar.get(Calendar.YEAR) +
                "\\" + calendar.get(Calendar.MONTH) + "\\" + calendar.get(Calendar.DAY_OF_MONTH) + ".db";
    }
    public void setTableValue(){
        String method = "INSERT INTO user(name) VALUES(?)";
        try(PreparedStatement statement = connection.prepareStatement(method)){
            statement.setString(1, "Hi!");
            statement.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getTableValue(){ // здесь надо будет создать цикл для того, чтобы прочесть все строки
        String method = "SELECT * FROM user";
        try(Statement statement = connection.createStatement()){
            ResultSet resultSet = statement.executeQuery(method);
            return resultSet.getString("name");
        } catch (Exception e){
            throw new RuntimeException(e.getMessage());
        }
    }

}
