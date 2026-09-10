package com.labresa.patterns.observer;

import com.labresa.dao.NotificationDAO;
import com.labresa.model.Notification;

import java.util.ArrayList;
import java.util.List;

/**
 * Observer pattern (subject side): persists every notification once, then
 * broadcasts it to every registered NotificationChannel. Services that trigger
 * a notification (approval results, maintenance alerts) only depend on this
 * class - never on individual channels - so adding a new channel later
 * requires no change to ApprovalService, MaintenanceService, etc.
 */
public class NotificationDispatcher {

    private final NotificationDAO notificationDAO;
    private final List<NotificationChannel> channels = new ArrayList<>();

    public NotificationDispatcher(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public void subscribe(NotificationChannel channel) {
        channels.add(channel);
    }

    public void dispatch(Notification notification) {
        notificationDAO.save(notification);
        for (NotificationChannel channel : channels) {
            channel.send(notification);
        }
    }
}