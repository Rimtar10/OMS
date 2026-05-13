package com.tradeoms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A Trade is created when a BuyOrder successfully matches a SellOrder.
 * It is the record of an actual transaction that happened.
 *
 * Think of it this way:
 *   Order = "I want to buy 10 AAPL at $150"  (intent)
 *   Trade = "10 AAPL were bought at $150"     (execution)
 */
public class Trade {

    private long id;
    private Order buyOrder;
    private Order sellOrder;
    private Instrument instrument;
    private int quantity;           // how many units were exchanged
    private BigDecimal price;       // the price at which the trade executed
    private LocalDateTime executedAt;

    public Trade(Order buyOrder, Order sellOrder, Instrument instrument,
                 int quantity, BigDecimal price) {
        this.buyOrder   = buyOrder;
        this.sellOrder  = sellOrder;
        this.instrument = instrument;
        this.quantity   = quantity;
        this.price      = price;
        this.executedAt = LocalDateTime.now();
    }

    public long getId()                 { return id; }
    public Order getBuyOrder()          { return buyOrder; }
    public Order getSellOrder()         { return sellOrder; }
    public Instrument getInstrument()   { return instrument; }
    public int getQuantity()            { return quantity; }
    public BigDecimal getPrice()        { return price; }
    public LocalDateTime getExecutedAt(){ return executedAt; }

    public void setId(long id)          { this.id = id; }

    @Override
    public String toString() {
        return String.format("Trade[id=%d, instrument=%s, qty=%d, price=%s, buyer=%s, seller=%s, at=%s]",
                id, instrument.getSymbol(), quantity, price,
                buyOrder.getUserId(), sellOrder.getUserId(), executedAt);
    }
}