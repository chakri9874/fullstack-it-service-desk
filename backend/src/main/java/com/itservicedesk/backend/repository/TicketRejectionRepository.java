package com.itservicedesk.backend.repository;

import com.itservicedesk.backend.entity.TicketRejection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TicketRejectionRepository extends JpaRepository<TicketRejection, Long> {

    boolean existsByTicket_IdAndSupportUser_Id(
            Long ticketId,
            Long supportUserId
    );

    long countByTicket_Id(Long ticketId);

    long countBySupportUser_IdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
            Long supportUserId,
            LocalDateTime startTime,
            LocalDateTime endTime
    );
}