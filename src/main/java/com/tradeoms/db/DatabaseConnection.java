package com.tradeoms.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL      = "jdbc:mysql://localhost:3306/trade_oms";
    private static final String USER     = "root";
    private static final String PASSWORD = "Reemi100";

    private static DatabaseConnection instance;
    private Connection connection;

    // Private constructor, nobody can call "new DatabaseConnection()" from outside
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