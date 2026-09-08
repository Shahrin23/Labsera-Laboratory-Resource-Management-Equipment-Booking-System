package com.labresa.patterns;

import com.labresa.dao.ResourceDAO;
import com.labresa.model.Reservation;
import com.labresa.model.Resource;
import com.labresa.model.UsageLog;
import com.labresa.patterns.approval.ApprovalResult;
import com.labresa.service.ApprovalService;
import com.labresa.service.ReservationService;
import com.labresa.service.UsageService;

public class LabResaFacade {

    private final ResourceDAO resourceDAO;
    private final ReservationService reservationService;
    private final ApprovalService approvalService;
    private final UsageService usageService;

    public LabResaFacade(ResourceDAO resourceDAO, ReservationService reservationService,
                         ApprovalService approvalService, UsageService usageService) {
        this.resourceDAO = resourceDAO;
        this.reservationService = reservationService;
        this.approvalService = approvalService;
        this.usageService = usageService;
    }

    /** One call for the whole "request slot -> route through approval chain" workflow. */
    public ApprovalResult requestAndApprove(Reservation reservation) {
        Resource resource = resourceDAO.findById(reservation.getResourceId());
        if (resource == null) {
            throw new IllegalArgumentException("No such resource: " + reservation.getResourceId());
        }
        reservationService.request(reservation);          // conflict check + save as PENDING
        return approvalService.evaluate(reservation, resource); // chain of responsibility
    }

    public UsageLog checkIn(Reservation reservation) {
        Resource resource = resourceDAO.findById(reservation.getResourceId());
        return usageService.checkIn(reservation, resource);
    }

    public void checkOut(UsageLog log, int resourceId, String conditionNotes) {
        Resource resource = resourceDAO.findById(resourceId);
        usageService.checkOut(log, resource, conditionNotes);
    }
}
