package com.labresa.dao;

import java.util.List;

import com.labresa.model.Notification;

public interface NotificationDAO {
    void save(Notification notification);
    List<Notification> findByUser(int userId);
}