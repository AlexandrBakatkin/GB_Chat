package server;

import java.sql.*;
import java.util.ArrayList;

public class AuthService {

    private static Connection connection;
    private static Statement stmt;

    public static void connect() throws SQLException {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DBUsers.db");
            stmt = connection.createStatement();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public static void disconnet(){
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass (String login, String pass){
        String sql = String.format("SELECT nickname FROM main WHERE login = '%s' AND password = '%s'", login, pass);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                return rs.getString(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String[] getBlackListbyNick (String nick){
        String sql = String.format("SELECT blacklist FROM main WHERE nickname = '%s'", nick);
        try {
            ResultSet rs = stmt.executeQuery(sql);
            if(rs.next()){
                String temp = rs.getString(1);
                if (temp != null){
                    String [] blackNick = temp.split(" ");
                    return blackNick;
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveBlackList (String nicks, String nick){
        String sql = String.format("UPDATE main SET blacklist = '%s' WHERE nickname = '%s'", nicks, nick);
        try {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //Метод записи сообщения в базу данных
    public static void saveMessageDB (String message, String nick){
        try {
            PreparedStatement ps = connection.prepareStatement("INSERT INTO history (nick, message) VALUES (?, ?)");
            ps.setString(1, nick);
            ps.setString(2, message);
            ps.addBatch();
            ps.executeBatch();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static ArrayList<String> loadHistoryDB (){

        ArrayList<String> history = new ArrayList<>();
        try {
            PreparedStatement ps = connection.prepareStatement("SELECT message FROM history");
            ResultSet rs = ps.executeQuery();
            while (rs.next()){
                history.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return history;
    }
}