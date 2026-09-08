package com.labresa.patterns.observer;

import java.util.ArrayList;
import java.util.List;


public class NotificationDispatcher {

    private final List<NotificationObserver> observers = new ArrayList<>();

    public void subscribe(NotificationObserver observer) {
        observers.add(observer);
    }

    public void unsubscribe(NotificationObserver observer) {
        observers.remove(observer);
    }

    public void dispatch(String eventType, String message) {
        for (NotificationObserver o : observers) {
            o.onNotify(eventType, message);
        }
    }
}
