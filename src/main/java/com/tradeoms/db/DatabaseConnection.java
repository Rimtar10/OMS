package com.tradeoms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Manages a single shared JDBC connection to the MySQL database.
 *
 * WHY SINGLETON?
 * Creating a new DB connection for every operation is expensive.
 * A singleton ensures the connection is created once and reused everywhere.
 *
 * HOW TO USE:
 *   Connection conn = DatabaseConnection.getInstance().getConnection();
 */
public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/trade_oms";
    private static final String USER     = "root";       // change to your MySQL username
    private static final String PASSWORD = "yourpassword"; // change to your MySQL password

    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor — nobody can call "new DatabaseConnection()" from outside
    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL, USER, PASSWORD);
        System.out.println("[DB] Connected to trade_oms database.");
    }

    /**
     * Returns the single instance, creating it on first call.
     * synchronized ensures thread safety — only one thread creates it.
     */
    public static synchronized DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.getConnection().isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}