package com.labresa.dao;

import com.labresa.model.User;

import java.util.List;

public interface UserDAO {
    User findById(int id);
    User findByEmail(String email);
    List<User> findAll();
    void save(User user, String email, String passwordHash);
    boolean verifyPassword(String email, String rawPassword);
    boolean emailExists(String email);
    void updatePassword(String email, String newPasswordHash);
}
