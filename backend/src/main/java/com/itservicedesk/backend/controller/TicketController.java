package com.itservicedesk.backend.controller;

import com.itservicedesk.backend.dto.SupportWorkloadStatsResponse;
import com.itservicedesk.backend.dto.TicketAssignmentRequest;
import com.itservicedesk.backend.dto.TicketCreateRequest;
import com.itservicedesk.backend.dto.TicketResponse;
import com.itservicedesk.backend.dto.TicketStatusUpdateRequest;
import com.itservicedesk.backend.dto.TicketUpdateRequest;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketStatus;
import com.itservicedesk.backend.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody TicketCreateRequest request) {

        TicketResponse createdTicket = ticketService.createTicket(request);

        return new ResponseEntity<>(createdTicket, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponse>> getAllTickets() {

        return ResponseEntity.ok(ticketService.getAllTickets());
    }

    @GetMapping("/support-stats")
    public ResponseEntity<SupportWorkloadStatsResponse> getSupportStats(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getSupportWorkloadStats(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/support-queue")
    public ResponseEntity<List<TicketResponse>> getPendingTicketsForSupport(
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.getPendingTicketsForLoggedInSupport(
                        authentication.getName()
                )
        );
    }

    @GetMapping("/escalation-queue")
    public ResponseEntity<List<TicketResponse>> getEscalationQueue() {

        return ResponseEntity.ok(ticketService.getEscalationQueue());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<TicketResponse>> getTicketsByStatus(
            @PathVariable TicketStatus status) {

        return ResponseEntity.ok(ticketService.getTicketsByStatus(status));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<TicketResponse>> getTicketsByCategory(
            @PathVariable TicketCategory category) {

        return ResponseEntity.ok(ticketService.getTicketsByCategory(category));
    }

    @GetMapping("/created-by/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsCreatedByUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ticketService.getTicketsCreatedByUser(userId));
    }

    @GetMapping("/assigned-to/{userId}")
    public ResponseEntity<List<TicketResponse>> getTicketsAssignedToUser(
            @PathVariable Long userId) {

        return ResponseEntity.ok(ticketService.getTicketsAssignedToUser(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponse> getTicketById(
            @PathVariable Long id) {

        return ResponseEntity.ok(ticketService.getTicketById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TicketResponse> updateTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketUpdateRequest request) {

        return ResponseEntity.ok(ticketService.updateTicket(id, request));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TicketResponse> updateTicketStatus(
            @PathVariable Long id,
            @Valid @RequestBody TicketStatusUpdateRequest request,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.updateTicketStatus(
                        id,
                        request.getStatus(),
                        authentication.getName(),
                        isAdmin(authentication)
                )
        );
    }

    @PatchMapping("/{id}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long id,
            @Valid @RequestBody TicketAssignmentRequest request) {

        return ResponseEntity.ok(ticketService.assignTicket(id, request));
    }

    @PatchMapping("/{id}/accept")
    public ResponseEntity<TicketResponse> acceptTicket(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.acceptTicketAssignedToLoggedInSupport(
                        id,
                        authentication.getName()
                )
        );
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<TicketResponse> rejectTicket(
            @PathVariable Long id,
            Authentication authentication) {

        return ResponseEntity.ok(
                ticketService.rejectTicketAssignedToLoggedInSupport(
                        id,
                        authentication.getName()
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTicket(
            @PathVariable Long id) {

        ticketService.deleteTicket(id);

        return ResponseEntity.noContent().build();
    }

    private boolean isAdmin(Authentication authentication) {

        return authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority -> authority.equals("ROLE_ADMIN"));
    }
}
