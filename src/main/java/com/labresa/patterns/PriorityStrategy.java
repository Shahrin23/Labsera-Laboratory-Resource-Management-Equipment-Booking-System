package com.labresa.patterns;

import com.labresa.model.User;

/**
 * Strategy pattern: decides which of two competing users should get a
 * freed-up reservation slot. Kept independent of ReservationService so the
 * priority rule can change (e.g. per semester) without touching booking logic.
 */
public interface PriorityStrategy {
    /** Returns a positive number if a has higher priority than b, negative if lower, 0 if equal. */
    int compare(User a, User b);
}
