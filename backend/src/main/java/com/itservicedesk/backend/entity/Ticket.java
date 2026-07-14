package com.itservicedesk.backend.entity;

import com.itservicedesk.backend.enums.TicketAssignmentStatus;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketPriority;
import com.itservicedesk.backend.enums.TicketStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, updatable = false, length = 50)
    private String ticketNumber;

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title cannot exceed 150 characters")
    @Column(nullable = false, length = 150)
    private String title;

    @NotBlank(message = "Description is required")
    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketCategory category;

    /*
     * Kept internally only because the existing MySQL table already has this column.
     * It is no longer shown or used in the professional workflow.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketAssignmentStatus assignmentStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id")
    private User createdBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assigned_to_user_id")
    private User assignedTo;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime dueAt;

    private LocalDateTime resolvedAt;

    private LocalDateTime lastAssignedAt;

    private LocalDateTime escalatedAt;

    @Column(length = 300)
    private String escalationReason;

    @PrePersist
    protected void beforeInsert() {
        LocalDateTime now = LocalDateTime.now();

        createdAt = now;
        updatedAt = now;

        if (ticketNumber == null) {
            String datePart = now.format(
                    DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
            );

            String randomPart = UUID.randomUUID()
                    .toString()
                    .substring(0, 6)
                    .toUpperCase();

            ticketNumber = "TKT-" + datePart + "-" + randomPart;
        }

        if (priority == null) {
            priority = TicketPriority.MEDIUM;
        }

        if (status == null) {
            status = TicketStatus.OPEN;
        }

        if (assignmentStatus == null) {
            assignmentStatus = TicketAssignmentStatus.UNASSIGNED;
        }
    }

    @PreUpdate
    protected void beforeUpdate() {
        updatedAt = LocalDateTime.now();
    }
}