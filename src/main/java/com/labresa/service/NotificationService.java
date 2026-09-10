package com.labresa.service;

import com.labresa.dao.NotificationDAO;
import com.labresa.model.Notification;

import java.util.List;

public class NotificationService {

    private final NotificationDAO notificationDAO;

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public List<Notification> forUser(int userId) {
        return notificationDAO.findByUser(userId);
    }
}
