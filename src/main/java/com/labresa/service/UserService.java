package com.labresa.service;

import com.labresa.dao.UserDAO;
import com.labresa.model.User;
import org.mindrot.jbcrypt.BCrypt;

public class UserService {

    public static final int MIN_PASSWORD_LENGTH = 8;

    private final UserDAO userDAO;

    public UserService(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public boolean emailExists(String email) {
        return userDAO.emailExists(email.trim().toLowerCase());
    }

    public boolean isPasswordValid(String password) {
        return password != null && password.length() >= MIN_PASSWORD_LENGTH;
    }

    /** Throws IllegalArgumentException with a user-facing message if validation fails. */
    public User register(String firstName, String lastName, String session, String classRoll,
                          String email, String password, User.Role role) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (firstName == null || firstName.isBlank()) {
            throw new IllegalArgumentException("First name is required.");
        }
        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required.");
        }
        if (classRoll == null || classRoll.isBlank()) {
            throw new IllegalArgumentException("Class roll is required.");
        }
        if (!normalizedEmail.matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
        if (emailExists(normalizedEmail)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        if (!isPasswordValid(password)) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }

        User user = new User(0, firstName.trim(), lastName.trim(), session == null ? "" : session.trim(), classRoll.trim(), role);
        String hash = BCrypt.hashpw(password, BCrypt.gensalt());
        userDAO.save(user, normalizedEmail, hash);
        return userDAO.findByEmail(normalizedEmail);
    }

    /** Throws IllegalArgumentException if the email isn't registered or the new password is too weak. */
    public void resetPassword(String email, String newPassword) {
        String normalizedEmail = email == null ? "" : email.trim().toLowerCase();
        if (!emailExists(normalizedEmail)) {
            throw new IllegalArgumentException("No account found with that email.");
        }
        if (!isPasswordValid(newPassword)) {
            throw new IllegalArgumentException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters.");
        }
        userDAO.updatePassword(normalizedEmail, BCrypt.hashpw(newPassword, BCrypt.gensalt()));
    }
}
