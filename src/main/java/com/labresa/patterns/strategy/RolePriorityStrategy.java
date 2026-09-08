package com.labresa.patterns.strategy;

import com.labresa.model.Reservation;
import com.labresa.model.User;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class RolePriorityStrategy implements PriorityStrategy {

    private static final Map<User.Role, Integer> RANK = Map.of(
            User.Role.FACULTY, 3,
            User.Role.GRAD, 2,
            User.Role.UNDERGRAD, 1,
            User.Role.TECHNICIAN, 0
    );

    @Override
    public Reservation resolve(List<Reservation> competitors) {
        if (competitors == null || competitors.isEmpty()) {
            throw new IllegalArgumentException("No competitors to resolve");
        }
        return competitors.stream()
                .max(Comparator
                        .comparingInt((Reservation r) -> RANK.getOrDefault(r.getRequesterRole(), 0))
                        .thenComparing(Comparator.comparingInt(Reservation::getId).reversed()))
                .orElseThrow();
    }
}

