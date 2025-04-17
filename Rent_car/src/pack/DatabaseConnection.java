package pack;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/projetgr01";
    private static final String USER = "root";
    private static final String PASSWORD = "Madagascar2022";        // a changer selon la BD

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}

