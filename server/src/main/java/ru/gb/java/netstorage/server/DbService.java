package ru.gb.java.netstorage.server;

import java.sql.*;

public class DbService {
    private static final DbService INSTANCE = new DbService();
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement psInsert;
    private static PreparedStatement psGetLogin;
    private static PreparedStatement psCheckLogin;
    private static PreparedStatement psGetMaxStorageSize;

    private DbService() {
        try {
            connect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DbService getInstance () {
        return INSTANCE;
    }

    public static void connect() throws Exception {
        Class.forName("org.sqlite.JDBC");
        connection = DriverManager.getConnection("jdbc:sqlite:DB/Users.db");
        stmt = connection.createStatement();
        System.out.println("Database 'Users' - connected");
    }

    public void disconnect() {
        try {
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void prepareInsert() throws SQLException {
        psInsert = connection.prepareStatement("INSERT INTO users (login, password) VALUES ( ? , ? );");
    }

    public static void prepareGetLoginByPass() throws SQLException {
        psGetLogin = connection.prepareStatement("SELECT login FROM users WHERE login = ? and password = ?;");
    }

    public static void prepareCheckLogin() throws SQLException {
        psCheckLogin = connection.prepareStatement("SELECT login FROM users WHERE login = ?;");
    }

    public static void prepareGetMaxStorageSizeByLogin() throws SQLException {
        psGetMaxStorageSize = connection.prepareStatement("SELECT max_storage_size FROM users WHERE login = ?;");
    }

    public String getLoginByPass(String login, Integer password) {
        if (isInDb(login)) {
            try {
                prepareGetLoginByPass();
                psGetLogin.setString(1, login);
                psGetLogin.setInt(2, password.hashCode());
                ResultSet rs = psGetLogin.executeQuery();
                String rss = rs.getString("login");
                rs.close();
                return rss;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return null;
        }
        return null;
    }

    public boolean isInDb(String login) {
        try {
            prepareCheckLogin();
            psCheckLogin.setString(1, login);
            ResultSet rs =  psCheckLogin.executeQuery();
            if (rs.next()) {
                rs.close();
                return true;
            } else {
                rs.close();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public long getMaxStorageSizeByLogin (String login) {
        if (isInDb(login)) {
            try {
                prepareGetMaxStorageSizeByLogin();
                psGetMaxStorageSize.setString(1, login);
                ResultSet rs = psGetMaxStorageSize.executeQuery();
                long rss = rs.getLong("max_storage_size");
                rs.close();
                return rss;
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return 0;
        }
        return 0;
    }

    public boolean registration(String login, String password) {
        try {
            prepareInsert();
            psInsert.setString(1, login);
            psInsert.setInt(2, password.hashCode());
            psInsert.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

}
