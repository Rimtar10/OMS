package com.tradeoms.observer;

import com.tradeoms.model.Order;


public interface OrderObserver {

    void onOrderUpdated(Order order);
}