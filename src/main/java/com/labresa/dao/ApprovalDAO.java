package com.labresa.dao;

import com.labresa.model.Approval;

import java.util.List;

public interface ApprovalDAO {
    void save(Approval approval);
    List<Approval> findByReservation(int reservationId);
}
