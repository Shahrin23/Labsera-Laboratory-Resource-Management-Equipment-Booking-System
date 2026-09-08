package com.labresa.patterns.observer;

import com.labresa.model.Notification;

/**
 * Concrete Observer: delivers notifications as an in-app banner/log line.
 * The UI layer can poll NotificationDAO.findByUser() to show unread items;
 * this channel additionally logs immediately for visibility during the demo.
 */
public class InAppNotificationChannel implements NotificationChannel {
    @Override
    public void send(Notification notification) {
        System.out.printf("[Notification -> user %d] %s%n", notification.getUserId(), notification.getMessage());
    }
}