package com.tradeoms.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Represents a user's holding in a specific instrument.
 * Tracks quantity owned and the average price paid (for P&L calculation).
 *
 * P&L (Profit & Loss) = (currentPrice - avgPrice) * quantity
 */
public class Portfolio {

    private long id;
    private String userId;
    private Instrument instrument;
    private int quantity;           // how many units the user currently holds
    private BigDecimal avgPrice;    // average cost per unit across all buys

    public Portfolio(String userId, Instrument instrument, int quantity, BigDecimal avgPrice) {
        this.userId     = userId;
        this.instrument = instrument;
        this.quantity   = quantity;
        this.avgPrice   = avgPrice;
    }

    /**
     * Updates the position when a new buy trade is executed.
     * Recalculates the average price using weighted average formula:
     *   newAvg = (oldQty * oldAvg + newQty * newPrice) / (oldQty + newQty)
     */
    public void addPosition(int tradeQty, BigDecimal tradePrice) {
        BigDecimal totalCost = avgPrice.multiply(BigDecimal.valueOf(quantity))
                .add(tradePrice.multiply(BigDecimal.valueOf(tradeQty)));
        this.quantity += tradeQty;
        this.avgPrice  = totalCost.divide(BigDecimal.valueOf(quantity), 2, RoundingMode.HALF_UP);
    }

    /**
     * Reduces position when a sell trade is executed.
     * Average price stays the same on sell — only quantity changes.
     */
    public void reducePosition(int tradeQty) {
        if (tradeQty > this.quantity) {
            throw new IllegalArgumentException("Cannot sell more than held quantity.");
        }
        this.quantity -= tradeQty;
    }

    /**
     * Calculates unrealized P&L given a current market price.
     */
    public BigDecimal calculatePnL(BigDecimal currentPrice) {
        return currentPrice.subtract(avgPrice).multiply(BigDecimal.valueOf(quantity));
    }

    public long getId()                { return id; }
    public String getUserId()          { return userId; }
    public Instrument getInstrument()  { return instrument; }
    public int getQuantity()           { return quantity; }
    public BigDecimal getAvgPrice()    { return avgPrice; }

    public void setId(long id)         { this.id = id; }
    public void setQuantity(int qty)   { this.quantity = qty; }
    public void setAvgPrice(BigDecimal p){ this.avgPrice = p; }

    @Override
    public String toString() {
        return String.format("Portfolio[user=%s, instrument=%s, qty=%d, avgPrice=%s]",
                userId, instrument.getSymbol(), quantity, avgPrice);
    }
}