package com.tradeoms.model;

/**
 * Represents a tradeable financial instrument (e.g. a stock or bond).
 * This is what orders are placed against — e.g. "Buy 10 shares of AAPL".
 */
public class Instrument {

    private String id;      // e.g. "AAPL"
    private String symbol;  // same as id for stocks, different for some bonds
    private String name;    // full name e.g. "Apple Inc."

    public Instrument(String id, String symbol, String name) {
        this.id = id;
        this.symbol = symbol;
        this.name = name;
    }

    public String getId()     { return id; }
    public String getSymbol() { return symbol; }
    public String getName()   { return name; }

    @Override
    public String toString() {
        return String.format("Instrument[%s | %s]", symbol, name);
    }
}