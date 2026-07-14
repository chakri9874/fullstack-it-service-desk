package com.itservicedesk.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class TicketEscalationScheduler {

    private final TicketService ticketService;

    @Value("${app.ticket.acceptance-timeout-minutes:120}")
    private long acceptanceTimeoutMinutes;

    public TicketEscalationScheduler(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @Scheduled(fixedRate = 60000)
    public void escalateStalePendingAssignments() {
        ticketService.escalateStalePendingAssignments(
                acceptanceTimeoutMinutes
        );
    }
}
