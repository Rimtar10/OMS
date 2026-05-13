package com.tradeoms.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;


public abstract class Order {

    private long id;                    // set by DB after save
    private String userId;             // who placed the order
    private Instrument instrument;     // what asset is being traded
    private int quantity;              // how many units
    private BigDecimal price;          // price per unit
    private OrderStatus status;        // current lifecycle state
    private LocalDateTime createdAt;   // when it was submitted

    public Order(String userId, Instrument instrument, int quantity, BigDecimal price) {
        this.userId     = userId;
        this.instrument = instrument;
        this.quantity   = quantity;
        this.price      = price;
        this.status     = OrderStatus.PENDING;   // always starts PENDING
        this.createdAt  = LocalDateTime.now();
    }

    // Every subclass must declare its type
    public abstract OrderType getType();

    // Getters
    public long getId()                { return id; }
    public String getUserId()          { return userId; }
    public Instrument getInstrument()  { return instrument; }
    public int getQuantity()           { return quantity; }
    public BigDecimal getPrice()       { return price; }
    public OrderStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    // Setters for fields that change during lifecycle
    public void setId(long id)               { this.id = id; }
    public void setStatus(OrderStatus status){ this.status = status; }
    public void setQuantity(int quantity)    { this.quantity = quantity; }

    @Override
    public String toString() {
        return String.format("Order[id=%d, type=%s, user=%s, instrument=%s, qty=%d, price=%s, status=%s]",
                id, getType(), userId, instrument.getSymbol(), quantity, price, status);
    }
}