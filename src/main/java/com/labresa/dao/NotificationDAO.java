package com.labresa.dao;

import com.labresa.model.Notification;

import java.util.List;

public interface NotificationDAO {
    void save(Notification notification);
    List<Notification> findByUser(int userId);
}