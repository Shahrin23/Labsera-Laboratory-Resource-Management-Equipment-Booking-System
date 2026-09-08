package com.labresa.dao.memory;

import com.labresa.dao.ReservationDAO;
import com.labresa.model.Reservation;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class InMemoryReservationDAO implements ReservationDAO {

    private final Map<Integer, Reservation> store = new LinkedHashMap<>();
    private int nextId = 1;

    @Override
    public Reservation findById(int id) {
        return store.get(id);
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public List<Reservation> findOverlapping(int resourceId, LocalDateTime start, LocalDateTime end) {
        return store.values().stream()
                .filter(r -> r.getResourceId() == resourceId)
                .filter(r -> r.getStatus() != Reservation.Status.CANCELLED
                        && r.getStatus() != Reservation.Status.REJECTED)
                .filter(r -> r.overlaps(start, end))
                .collect(Collectors.toList());
    }

    @Override
    public void save(Reservation reservation) {
        if (reservation.getId() == 0) {
            reservation.setId(nextId++);
        }
        store.put(reservation.getId(), reservation);
    }

    @Override
    public void updateStatus(int reservationId, Reservation.Status status) {
        Reservation r = store.get(reservationId);
        if (r != null) {
            r.setStatus(status);
        }
    }
}

