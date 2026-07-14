package com.itservicedesk.backend.dto;

import com.itservicedesk.backend.enums.TicketAssignmentStatus;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private TicketCategory category;

    private TicketStatus status;
    private TicketAssignmentStatus assignmentStatus;

    private Long createdByUserId;
    private String createdByName;

    private Long assignedToUserId;
    private String assignedToName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime dueAt;
    private LocalDateTime resolvedAt;

    private LocalDateTime lastAssignedAt;
    private LocalDateTime escalatedAt;
    private String escalationReason;
}