package net.safety.io;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Calendar;

public class MessageSaver implements AutoCloseable{
   private final Connection connection;
   private Calendar calendar = Calendar.getInstance();

    public MessageSaver(){ // для текущего дня
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
        try {
            Connection connection = DriverManager.getConnection(getPathForConnection());
            Statement statement = connection.createStatement();
            this.connection = connection;
            String createSQLTable = "CREATE TABLE IF NOT EXISTS user (" +"\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + "\n" +
                    "message TEXT NOT NULL, time TEXT NOT NULL);";

            statement.execute(createSQLTable);
            statement.close();

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public MessageSaver(Calendar calendar){ // для конкретной даты
        this.calendar = calendar;
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
        try {
            Connection connection = DriverManager.getConnection(getPathForConnection());
            Statement statement = connection.createStatement();
            this.connection = connection;
            String createSQLTable = "CREATE TABLE IF NOT EXISTS user (" +"\n" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," + "\n" +
                    "message TEXT NOT NULL, time TEXT NOT NULL);";

            statement.execute(createSQLTable);
            statement.close();

        } catch (Exception e){
            throw new RuntimeException(e);
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
    public void setMessage(String text, LocalTime time){
        String method = "INSERT INTO user(message, time) VALUES(?, ?)";
        try(PreparedStatement statement = connection.prepareStatement(method)){
            statement.setString(1, text);
            statement.setString(2, time.getHour() + ":" + time.getMinute());
            statement.executeUpdate();
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> getMessage(){
        String method = "SELECT message FROM user";
        try(Statement statement = connection.createStatement()){
            ResultSet set = statement.executeQuery(method);
            ArrayList<String> result = new ArrayList<>();
            while (set.next()){
                result.add(set.getString("message"));
            }
            return result;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    public ArrayList<String> getTime(){
        String method = "SELECT time FROM user";
        try(Statement statement = connection.createStatement()){
            ResultSet set = statement.executeQuery(method);
            ArrayList<String> result = new ArrayList<>();
            while (set.next()){
                result.add(set.getString("time"));
            }
            return result;
        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
