package com.labresa.patterns;

import java.util.EnumMap;
import java.util.Map;

import com.labresa.model.User;

/** Faculty > Grad Student > Technician > Undergrad, for resolving freed-slot priority. */
public class RolePriorityStrategy implements PriorityStrategy {

    private static final Map<User.Role, Integer> RANK = new EnumMap<>(User.Role.class);
    static {
        RANK.put(User.Role.FACULTY, 4);
        RANK.put(User.Role.GRAD, 3);
        RANK.put(User.Role.TECHNICIAN, 2);
        RANK.put(User.Role.UNDERGRAD, 1);
    }

    @Override
    public int compare(User a, User b) {
        return RANK.get(a.getRole()) - RANK.get(b.getRole());
    }
}
