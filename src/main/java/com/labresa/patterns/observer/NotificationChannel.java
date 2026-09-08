package com.labresa.patterns.observer;

import com.labresa.model.Notification;

/**
 * Observer pattern: an "observer" that reacts whenever the system dispatches
 * a Notification. New delivery channels (email, SMS, push) implement this
 * without any change to NotificationDispatcher or the services that trigger it.
 */
public interface NotificationChannel {
    void send(Notification notification);
}
