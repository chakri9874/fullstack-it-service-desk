package com.itservicedesk.backend.dto;

import com.itservicedesk.backend.enums.TicketPriority;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketPriorityUpdateRequest {

    @NotNull(message = "Priority is required")
    private TicketPriority priority;
}