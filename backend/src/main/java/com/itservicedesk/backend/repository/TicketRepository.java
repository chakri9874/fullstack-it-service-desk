package com.itservicedesk.backend.repository;

import com.itservicedesk.backend.entity.Ticket;
import com.itservicedesk.backend.enums.TicketAssignmentStatus;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    long countByStatus(TicketStatus status);

    long countByCategory(TicketCategory category);

    long countByAssignmentStatus(TicketAssignmentStatus assignmentStatus);

    List<Ticket> findAllByOrderByCreatedAtDesc();

    List<Ticket> findByStatusOrderByCreatedAtDesc(TicketStatus status);

    List<Ticket> findByCategoryOrderByCreatedAtDesc(TicketCategory category);

    List<Ticket> findByCreatedBy_IdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByAssignedTo_IdOrderByCreatedAtDesc(Long userId);

    List<Ticket> findByAssignmentStatusAndAssignedTo_IdOrderByCreatedAtDesc(
            TicketAssignmentStatus assignmentStatus,
            Long assignedToUserId
    );

    List<Ticket> findByAssignmentStatusOrderByCreatedAtDesc(
            TicketAssignmentStatus assignmentStatus
    );

    List<Ticket> findByAssignmentStatusAndLastAssignedAtBeforeOrderByLastAssignedAtAsc(
            TicketAssignmentStatus assignmentStatus,
            LocalDateTime cutoffTime
    );

    @Query("""
            SELECT COUNT(t)
            FROM Ticket t
            WHERE t.assignedTo.id = :supportUserId
              AND t.assignmentStatus IN :assignmentStatuses
              AND t.status NOT IN :terminalStatuses
            """)
    long countActiveTicketsForSupport(
            @Param("supportUserId") Long supportUserId,
            @Param("assignmentStatuses") List<TicketAssignmentStatus> assignmentStatuses,
            @Param("terminalStatuses") List<TicketStatus> terminalStatuses
    );
}