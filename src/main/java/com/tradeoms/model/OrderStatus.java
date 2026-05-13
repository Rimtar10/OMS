package com.tradeoms.model;

/**
 * Represents the lifecycle state of an order.
 * PENDING   → submitted, waiting to be matched
 * FILLED    → fully matched with a counterpart order
 * PARTIAL   → partially matched, remainder still pending
 * CANCELLED → cancelled before full execution
 */
public enum OrderStatus {
    PENDING,
    FILLED,
    PARTIAL,
    CANCELLED
}