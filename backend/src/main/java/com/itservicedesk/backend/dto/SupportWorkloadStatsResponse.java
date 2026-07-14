package com.itservicedesk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SupportWorkloadStatsResponse {

    private Long supportUserId;
    private String supportUserName;

    private int weeklyRejectionLimit;
    private long rejectionsUsedThisWeek;
    private long remainingRejectionsThisWeek;
    private boolean rejectionAvailable;

    private long pendingAssignments;
    private long acceptedTickets;
}