package com.labresa.service;

import com.labresa.dao.ReservationDAO;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.patterns.approval.ApprovalHandler;
import com.labresa.patterns.approval.ApprovalResult;
import com.labresa.patterns.strategy.PriorityStrategy;

import java.util.List;

public class ApprovalService {

    private final ApprovalHandler approvalChain;
    private final ReservationDAO reservationDAO;
    private final PriorityStrategy priorityStrategy;

    public ApprovalService(ApprovalHandler approvalChain, ReservationDAO reservationDAO,
                           PriorityStrategy priorityStrategy) {
        this.approvalChain = approvalChain;
        this.reservationDAO = reservationDAO;
        this.priorityStrategy = priorityStrategy;
    }

    public ApprovalResult evaluate(Reservation reservation, Resource resource) {
        ApprovalResult result = approvalChain.handle(reservation, resource);
        reservationDAO.updateStatus(reservation.getId(),
                result.isApproved() ? Reservation.Status.CONFIRMED : Reservation.Status.REJECTED);
        return result;
    }


    public Reservation resolveCompetingRequests(List<Reservation> competitors) {
        Reservation winner = priorityStrategy.resolve(competitors);
        for (Reservation r : competitors) {
            if (r.getId() != winner.getId()) {
                reservationDAO.updateStatus(r.getId(), Reservation.Status.REJECTED);
            }
        }
        return winner;
    }
}
