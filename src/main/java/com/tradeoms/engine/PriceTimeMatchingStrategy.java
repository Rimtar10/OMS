package com.tradeoms.engine;

import com.tradeoms.model.*;

import java.util.ArrayList;
import java.util.List;

public class PriceTimeMatchingStrategy implements MatchingStrategy {

    @Override
    public List<Trade> match(OrderBook orderBook) {
        List<Trade> executedTrades = new ArrayList<>();

        while (!orderBook.getBuyOrders().isEmpty() && !orderBook.getSellOrders().isEmpty()) {

            // Peek at the best buy and best sell without removing them yet
            Order bestBuy  = orderBook.getBuyOrders().peek();
            Order bestSell = orderBook.getSellOrders().peek();

            // No match possible if best buy price < best sell price
            if (bestBuy.getPrice().compareTo(bestSell.getPrice()) < 0) {
                break;
            }

            // Determine how many units to trade
            int tradedQty = Math.min(bestBuy.getQuantity(), bestSell.getQuantity());

            // Trade executes at the sell order's price
            Trade trade = new Trade(bestBuy, bestSell,
                    bestBuy.getInstrument(), tradedQty, bestSell.getPrice());
            executedTrades.add(trade);

            System.out.printf("[MATCH FOUND] %s: %d units @ %s (Buyer: %s | Seller: %s)%n",
                    bestBuy.getInstrument().getSymbol(),
                    tradedQty,
                    bestSell.getPrice(),
                    bestBuy.getUserId(),
                    bestSell.getUserId());

            // Update quantities and statuses
            if (bestBuy.getQuantity() == bestSell.getQuantity()) {
                // Perfect fill — both orders completely matched
                bestBuy.setStatus(OrderStatus.FILLED);
                bestSell.setStatus(OrderStatus.FILLED);
                orderBook.getBuyOrders().poll();
                orderBook.getSellOrders().poll();

            } else if (bestBuy.getQuantity() > bestSell.getQuantity()) {
                // Buy order has more — sell is done, buy is partially filled
                bestBuy.setQuantity(bestBuy.getQuantity() - tradedQty);
                bestBuy.setStatus(OrderStatus.PARTIAL);
                bestSell.setStatus(OrderStatus.FILLED);
                orderBook.getSellOrders().poll();

                // Re-add the buy order so the priority queue re-sorts it
                orderBook.getBuyOrders().poll();
                orderBook.getBuyOrders().add(bestBuy);

            } else {
                // Sell order has more — buy is done, sell is partially filled
                bestSell.setQuantity(bestSell.getQuantity() - tradedQty);
                bestSell.setStatus(OrderStatus.PARTIAL);
                bestBuy.setStatus(OrderStatus.FILLED);
                orderBook.getBuyOrders().poll();

                // Re-add the sell order so the priority queue re-sorts it
                orderBook.getSellOrders().poll();
                orderBook.getSellOrders().add(bestSell);
            }
        }

        return executedTrades;
    }
}