package com.tradeoms;

import com.tradeoms.db.DatabaseConnection;
import com.tradeoms.engine.OrderMatchingEngine;
import com.tradeoms.engine.PriceTimeMatchingStrategy;
import com.tradeoms.model.*;
import com.tradeoms.observer.OrderLogger;
import com.tradeoms.repository.OrderRepository;
import com.tradeoms.repository.PortfolioRepository;
import com.tradeoms.repository.TradeRepository;
import com.tradeoms.service.OrderService;
import com.tradeoms.service.PortfolioService;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public class Main {

    public static void main(String[] args) {
        System.out.println("   Trade Order Management System (OMS)  ");

        try {
            DatabaseConnection.getInstance();

            java.sql.Connection conn = DatabaseConnection.getInstance().getConnection();
            conn.createStatement().executeUpdate("DELETE FROM trades WHERE id > 0");
            conn.createStatement().executeUpdate("DELETE FROM portfolio WHERE id > 0");
            conn.createStatement().executeUpdate("DELETE FROM orders WHERE id > 0");
            conn.createStatement().executeUpdate("ALTER TABLE trades AUTO_INCREMENT = 1");
            conn.createStatement().executeUpdate("ALTER TABLE portfolio AUTO_INCREMENT = 1");
            conn.createStatement().executeUpdate("ALTER TABLE orders AUTO_INCREMENT = 1");
            System.out.println("[DB] Tables cleared for fresh demo run.\n");

            Instrument AAPL = new Instrument("AAPL", "AAPL", "Apple Inc.");
            Instrument TSLA = new Instrument("TSLA", "TSLA", "Tesla Inc.");
            List<Instrument> instruments = List.of(AAPL, TSLA);

            OrderRepository     orderRepo     = new OrderRepository();
            TradeRepository     tradeRepo     = new TradeRepository();
            PortfolioRepository portfolioRepo = new PortfolioRepository();

            PortfolioService portfolioService = new PortfolioService(portfolioRepo);

            OrderMatchingEngine engine = new OrderMatchingEngine(
                    new PriceTimeMatchingStrategy(), orderRepo, tradeRepo);

            engine.addObserver(new OrderLogger());

            OrderService orderService = new OrderService(engine, orderRepo, portfolioService);

            // Bob and Carol already own AAPL before they place sell orders.
            System.out.println(">>> SEEDING: Bob owns 20 AAPL @ avg $145.00");
            Portfolio bobHolding = new Portfolio("bob", AAPL, 20, new BigDecimal("145.00"));
            portfolioRepo.save(bobHolding);

            System.out.println(">>> SEEDING: Carol owns 10 AAPL @ avg $148.00\n");
            Portfolio carolHolding = new Portfolio("carol", AAPL, 10, new BigDecimal("148.00"));
            portfolioRepo.save(carolHolding);

            System.out.println(">>> STEP 1: Alice places BUY 10 AAPL @ $152.00");
            orderService.placeOrder(
                    OrderType.BUY, "alice", AAPL, 10, new BigDecimal("152.00"));

            System.out.println("\n>>> STEP 2: Bob places SELL 7 AAPL @ $150.00");
            System.out.println("    Expected: 7 units match with Alice (PARTIAL fill on Alice)");
            orderService.placeOrder(
                    OrderType.SELL, "bob", AAPL, 7, new BigDecimal("150.00"));

            System.out.println("\n>>> STEP 3: Carol places SELL 5 AAPL @ $151.00");
            System.out.println("    Expected: Alice's remaining 3 match with Carol");
            orderService.placeOrder(
                    OrderType.SELL, "carol", AAPL, 5, new BigDecimal("151.00"));

            System.out.println("\n>>> STEP 4: Current AAPL Order Book");
            orderService.printOrderBook("AAPL");

            // Simulate current market prices
            Map<String, BigDecimal> marketPrices = Map.of(
                    "AAPL", new BigDecimal("155.00"),
                    "TSLA", new BigDecimal("220.00")
            );

            System.out.println(">>> STEP 5: Portfolio Summaries");
            portfolioService.printPortfolioSummary("alice", instruments, marketPrices);
            portfolioService.printPortfolioSummary("bob",   instruments, marketPrices);
            portfolioService.printPortfolioSummary("carol", instruments, marketPrices);

            System.out.println("   OMS Demo completed successfully!     ");

        } catch (SQLException e) {
            System.err.println("[ERROR] Database error: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("[ERROR] Unexpected error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}