package com.itservicedesk.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ticket_rejections",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ticket_rejection_ticket_support",
                        columnNames = {"ticket_id", "support_user_id"}
                )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TicketRejection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "support_user_id", nullable = false)
    private User supportUser;

    @Column(nullable = false, updatable = false)
    private LocalDateTime rejectedAt;

    @PrePersist
    protected void beforeInsert() {
        if (rejectedAt == null) {
            rejectedAt = LocalDateTime.now();
        }
    }
}