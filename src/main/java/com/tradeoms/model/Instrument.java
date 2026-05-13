package com.tradeoms.model;


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