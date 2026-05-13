package com.tradeoms.service;

import com.tradeoms.model.*;
import com.tradeoms.repository.PortfolioRepository;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    public PortfolioService(PortfolioRepository portfolioRepository) {
        this.portfolioRepository = portfolioRepository;
    }

    public void updatePosition(String userId, Instrument instrument,
                               OrderType type, int tradedQty,
                               BigDecimal tradePrice) throws SQLException {

        Portfolio existing = portfolioRepository.findByUserAndInstrument(userId, instrument);

        if (type == OrderType.BUY) {
            if (existing == null) {
                // First time this user buys this instrument — create a new position
                Portfolio newPosition = new Portfolio(userId, instrument, tradedQty, tradePrice);
                portfolioRepository.save(newPosition);
                System.out.printf("[PORTFOLIO] New position: %s owns %d %s @ avg $%s%n",
                        userId, tradedQty, instrument.getSymbol(), tradePrice);
            } else {
                // Already holds this instrument — update quantity and average price
                existing.addPosition(tradedQty, tradePrice);
                portfolioRepository.update(existing);
                System.out.printf("[PORTFOLIO] Updated: %s now holds %d %s @ avg $%s%n",
                        userId, existing.getQuantity(), instrument.getSymbol(), existing.getAvgPrice());
            }

        } else { // SELL
            if (existing == null || existing.getQuantity() < tradedQty) {
                throw new IllegalStateException(
                        userId + " does not hold enough " + instrument.getSymbol() + " to sell.");
            }
            existing.reducePosition(tradedQty);
            portfolioRepository.update(existing);
            System.out.printf("[PORTFOLIO] Sold: %s sold %d %s | Remaining: %d%n",
                    userId, tradedQty, instrument.getSymbol(), existing.getQuantity());
        }
    }

    public List<Portfolio> getUserPortfolio(String userId,
                                            List<Instrument> instruments) throws SQLException {
        return portfolioRepository.findByUser(userId, instruments);
    }

    public void printPortfolioSummary(String userId, List<Instrument> instruments,
                                      java.util.Map<String, BigDecimal> marketPrices) throws SQLException {

        List<Portfolio> positions = getUserPortfolio(userId, instruments);

        System.out.println("\nPORTFOLIO: " + userId );
        if (positions.isEmpty()) {
            System.out.println("  No positions held.");
        } else {
            System.out.printf("  %-8s %-8s %-12s %-12s %-12s%n",
                    "Symbol", "Qty", "Avg Price", "Market", "P&L");
            System.out.println("  " + "-".repeat(55));

            BigDecimal totalPnL = BigDecimal.ZERO;

            for (Portfolio p : positions) {
                BigDecimal market = marketPrices.getOrDefault(
                        p.getInstrument().getId(), p.getAvgPrice());
                BigDecimal pnl = p.calculatePnL(market);
                totalPnL = totalPnL.add(pnl);

                System.out.printf("  %-8s %-8d $%-11s $%-11s $%s%n",
                        p.getInstrument().getSymbol(),
                        p.getQuantity(),
                        p.getAvgPrice(),
                        market,
                        pnl);
            }

            System.out.println("  " + "-".repeat(55));
            System.out.printf("  %-30s TOTAL P&L: $%s%n", "", totalPnL);
        }

    }
}