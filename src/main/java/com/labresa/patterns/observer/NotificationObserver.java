package com.labresa.patterns.observer;

public interface NotificationObserver {
    void onNotify(String eventType, String message);
}

