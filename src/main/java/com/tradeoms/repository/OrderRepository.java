package com.tradeoms.repository;

import com.tradeoms.db.DatabaseConnection;
import com.tradeoms.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class OrderRepository {

    public void save(Order order) throws SQLException {
        String sql = "INSERT INTO orders (instrument_id, type, status, quantity, price, user_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        // RETURN_GENERATED_KEYS tells JDBC to give us back the auto-increment ID
        try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, order.getInstrument().getId());
            stmt.setString(2, order.getType().name());
            stmt.setString(3, order.getStatus().name());
            stmt.setInt(4, order.getQuantity());
            stmt.setBigDecimal(5, order.getPrice());
            stmt.setString(6, order.getUserId());
            stmt.executeUpdate();

            // Read the generated ID and set it back on the order
            ResultSet keys = stmt.getGeneratedKeys();
            if (keys.next()) {
                order.setId(keys.getLong(1));
            }
        }
    }

    public void update(Order order) throws SQLException {
        String sql = "UPDATE orders SET status = ?, quantity = ? WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, order.getStatus().name());
            stmt.setInt(2, order.getQuantity());
            stmt.setLong(3, order.getId());
            stmt.executeUpdate();
        }
    }


    public List<Order> findByStatus(OrderStatus status, List<Instrument> instruments) throws SQLException {
        String sql = "SELECT * FROM orders WHERE status = ?";
        List<Order> orders = new ArrayList<>();

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status.name());
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                orders.add(mapRow(rs, instruments));
            }
        }
        return orders;
    }

    public Order findById(long id, List<Instrument> instruments) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id = ?";

        Connection conn = DatabaseConnection.getInstance().getConnection();

        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapRow(rs, instruments);
            }
        }
        return null;
    }

    private Order mapRow(ResultSet rs, List<Instrument> instruments) throws SQLException {
        String instrumentId = rs.getString("instrument_id");

        // Find the matching instrument from the provided list
        Instrument instrument = instruments.stream()
                .filter(i -> i.getId().equals(instrumentId))
                .findFirst()
                .orElseThrow(() -> new SQLException("Instrument not found: " + instrumentId));

        OrderType type     = OrderType.valueOf(rs.getString("type"));
        OrderStatus status = OrderStatus.valueOf(rs.getString("status"));

        Order order = switch (type) {
            case BUY  -> new BuyOrder(rs.getString("user_id"), instrument,
                    rs.getInt("quantity"), rs.getBigDecimal("price"));
            case SELL -> new SellOrder(rs.getString("user_id"), instrument,
                    rs.getInt("quantity"), rs.getBigDecimal("price"));
        };

        order.setId(rs.getLong("id"));
        order.setStatus(status);
        return order;
    }
}