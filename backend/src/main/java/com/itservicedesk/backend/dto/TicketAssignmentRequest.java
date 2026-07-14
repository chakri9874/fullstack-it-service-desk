package com.itservicedesk.backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketAssignmentRequest {

    @NotNull(message = "Assigned to user ID is required")
    private Long assignedToUserId;
}