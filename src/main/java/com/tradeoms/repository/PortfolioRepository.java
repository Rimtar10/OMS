package com.tradeoms.repository;

import com.tradeoms.db.DatabaseConnection;
import com.tradeoms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles all database operations for Portfolio positions.
 * Each row represents one user's holding in one instrument.
 */
public class PortfolioRepository {

    /**
     * Finds a user's position in a specific instrument.
     * Returns null if the user has no position yet.
     */
    public Portfolio findByUserAndInstrument(String userId, Instrument instrument) throws SQLException {
        String sql = "SELECT * FROM portfolio WHERE user_id = ? AND instrument_id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            stmt.setString(2, instrument.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Portfolio p = new Portfolio(userId, instrument,
                        rs.getInt("quantity"),
                        rs.getBigDecimal("avg_price"));
                p.setId(rs.getLong("id"));
                return p;
            }
        }
        return null;
    }

    /**
     * Returns all positions for a given user.
     * Used by PortfolioService to show the user's full holdings.
     */
    public List<Portfolio> findByUser(String userId, List<Instrument> instruments) throws SQLException {
        String sql = "SELECT * FROM portfolio WHERE user_id = ?";
        List<Portfolio> positions = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, userId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                String instrId = rs.getString("instrument_id");
                Instrument instrument = instruments.stream()
                        .filter(i -> i.getId().equals(instrId))
                        .findFirst()
                        .orElseThrow(() -> new SQLException("Instrument not found: " + instrId));

                Portfolio p = new Portfolio(userId, instrument,
                        rs.getInt("quantity"),
                        rs.getBigDecimal("avg_price"));
                p.setId(rs.getLong("id"));
                positions.add(p);
            }
        }
        return positions;
    }

    /**
     * Inserts a new portfolio row (first time a user buys an instrument).
     */
    public void save(Portfolio portfolio) throws SQLException {
        String sql = "INSERT INTO portfolio (user_id, instrument_id, quantity, avg_price) " +
                "VALUES (?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, portfolio.getUserId());
            stmt.setString(2, portfolio.getInstrument().getId());
            stmt.setInt(3, portfolio.getQuantity());
            stmt.setBigDecimal(4, portfolio.getAvgPrice());
            stmt.executeUpdate();

            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                portfolio.setId(keys.getLong(1));
            }
        }
    }

    /**
     * Updates an existing portfolio position (quantity and average price).
     * Called after every trade execution.
     */
    public void update(Portfolio portfolio) throws SQLException {
        String sql = "UPDATE portfolio SET quantity = ?, avg_price = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, portfolio.getQuantity());
            stmt.setBigDecimal(2, portfolio.getAvgPrice());
            stmt.setLong(3, portfolio.getId());
            stmt.executeUpdate();
        }
    }
}