package com.labresa;

import com.labresa.model.User;

/** Holds the currently logged-in user for the duration of the app session. */
public class Session {
    private static User currentUser;

    public static void setCurrentUser(User user) { currentUser = user; }
    public static User getCurrentUser() { return currentUser; }
}
