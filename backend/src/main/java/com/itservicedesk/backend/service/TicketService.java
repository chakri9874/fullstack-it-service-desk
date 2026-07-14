package com.itservicedesk.backend.service;

import com.itservicedesk.backend.dto.SupportWorkloadStatsResponse;
import com.itservicedesk.backend.dto.TicketAssignmentRequest;
import com.itservicedesk.backend.dto.TicketCreateRequest;
import com.itservicedesk.backend.dto.TicketResponse;
import com.itservicedesk.backend.dto.TicketUpdateRequest;
import com.itservicedesk.backend.entity.Ticket;
import com.itservicedesk.backend.entity.TicketRejection;
import com.itservicedesk.backend.entity.User;
import com.itservicedesk.backend.enums.TicketAssignmentStatus;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketPriority;
import com.itservicedesk.backend.enums.TicketStatus;
import com.itservicedesk.backend.enums.UserRole;
import com.itservicedesk.backend.exception.InvalidTicketOperationException;
import com.itservicedesk.backend.exception.ResourceNotFoundException;
import com.itservicedesk.backend.exception.UnauthorizedException;
import com.itservicedesk.backend.repository.TicketRejectionRepository;
import com.itservicedesk.backend.repository.TicketRepository;
import com.itservicedesk.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TicketService {

    private final TicketRepository ticketRepository;
    private final TicketRejectionRepository ticketRejectionRepository;
    private final UserService userService;
    private final UserRepository userRepository;

    @Value("${app.ticket.weekly-rejection-limit:2}")
    private int weeklyRejectionLimit;

    public TicketService(
            TicketRepository ticketRepository,
            TicketRejectionRepository ticketRejectionRepository,
            UserService userService,
            UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.ticketRejectionRepository = ticketRejectionRepository;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public TicketResponse createTicket(TicketCreateRequest request) {

        User createdByUser = userService.findUserEntityById(
                request.getCreatedByUserId()
        );

        Ticket ticket = new Ticket();

        ticket.setTitle(request.getTitle());
        ticket.setDescription(request.getDescription());
        ticket.setCategory(request.getCategory());
        ticket.setPriority(TicketPriority.MEDIUM);
        ticket.setDueAt(request.getDueAt());
        ticket.setCreatedBy(createdByUser);
        ticket.setStatus(TicketStatus.OPEN);

        assignTicketUsingWorkloadRules(ticket, null);

        Ticket savedTicket = ticketRepository.save(ticket);

        return mapToTicketResponse(savedTicket);
    }

    public List<TicketResponse> getAllTickets() {

        return ticketRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public TicketResponse getTicketById(Long id) {

        Ticket ticket = findTicketEntityById(id);

        return mapToTicketResponse(ticket);
    }

    public List<TicketResponse> getTicketsByStatus(TicketStatus status) {

        return ticketRepository.findByStatusOrderByCreatedAtDesc(status)
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public List<TicketResponse> getTicketsByCategory(
            TicketCategory category) {

        return ticketRepository.findByCategoryOrderByCreatedAtDesc(category)
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public List<TicketResponse> getTicketsCreatedByUser(Long userId) {

        userService.findUserEntityById(userId);

        return ticketRepository.findByCreatedBy_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public List<TicketResponse> getTicketsAssignedToUser(Long userId) {

        userService.findUserEntityById(userId);

        return ticketRepository.findByAssignedTo_IdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public List<TicketResponse> getPendingTicketsForLoggedInSupport(
            String supportUserEmail) {

        User supportUser = findActiveSupportUserByEmail(supportUserEmail);

        movePendingAssignmentsToAcceptedIfRejectionLimitReached(supportUser);

        return ticketRepository
                .findByAssignmentStatusAndAssignedTo_IdOrderByCreatedAtDesc(
                        TicketAssignmentStatus.PENDING_ACCEPTANCE,
                        supportUser.getId()
                )
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public SupportWorkloadStatsResponse getSupportWorkloadStats(
            String supportUserEmail) {

        User supportUser = findActiveSupportUserByEmail(supportUserEmail);

        long rejectionsUsedThisWeek =
                getCurrentWeekRejectionCount(supportUser.getId());

        long remainingRejections =
                Math.max(weeklyRejectionLimit - rejectionsUsedThisWeek, 0);

        long pendingAssignments =
                ticketRepository
                        .findByAssignmentStatusAndAssignedTo_IdOrderByCreatedAtDesc(
                                TicketAssignmentStatus.PENDING_ACCEPTANCE,
                                supportUser.getId()
                        )
                        .size();

        long acceptedTickets =
                ticketRepository
                        .findByAssignmentStatusAndAssignedTo_IdOrderByCreatedAtDesc(
                                TicketAssignmentStatus.ACCEPTED,
                                supportUser.getId()
                        )
                        .size();

        return new SupportWorkloadStatsResponse(
                supportUser.getId(),
                supportUser.getFullName(),
                weeklyRejectionLimit,
                rejectionsUsedThisWeek,
                remainingRejections,
                remainingRejections > 0,
                pendingAssignments,
                acceptedTickets
        );
    }

    public List<TicketResponse> getEscalationQueue() {

        return ticketRepository
                .findByAssignmentStatusOrderByCreatedAtDesc(
                        TicketAssignmentStatus.ESCALATED
                )
                .stream()
                .map(this::mapToTicketResponse)
                .toList();
    }

    public TicketResponse updateTicket(
            Long id,
            TicketUpdateRequest request) {

        Ticket existingTicket = findTicketEntityById(id);

        existingTicket.setTitle(request.getTitle());
        existingTicket.setDescription(request.getDescription());
        existingTicket.setCategory(request.getCategory());
        existingTicket.setStatus(request.getStatus());
        existingTicket.setDueAt(request.getDueAt());

        updateResolvedTimestamp(existingTicket, request.getStatus());

        Ticket updatedTicket = ticketRepository.save(existingTicket);

        return mapToTicketResponse(updatedTicket);
    }

    public TicketResponse updateTicketStatus(
            Long id,
            TicketStatus newStatus,
            String actorEmail,
            boolean adminAction) {

        Ticket existingTicket = findTicketEntityById(id);

        if (!adminAction) {
            validateAssignedSupportCanModifyTicket(
                    existingTicket,
                    actorEmail
            );

            if (newStatus == TicketStatus.CLOSED
                    || newStatus == TicketStatus.CANCELLED) {

                throw new InvalidTicketOperationException(
                        "IT support can only move tickets to in progress, on hold, or resolved."
                );
            }
        }

        existingTicket.setStatus(newStatus);

        updateResolvedTimestamp(existingTicket, newStatus);

        Ticket updatedTicket = ticketRepository.save(existingTicket);

        return mapToTicketResponse(updatedTicket);
    }

    public TicketResponse assignTicket(
            Long id,
            TicketAssignmentRequest request) {

        Ticket existingTicket = findTicketEntityById(id);

        User assignedUser = userService.findUserEntityById(
                request.getAssignedToUserId()
        );

        validateAssignableSupportUser(assignedUser);

        existingTicket.setAssignedTo(assignedUser);
        existingTicket.setAssignmentStatus(TicketAssignmentStatus.ACCEPTED);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        existingTicket.setLastAssignedAt(LocalDateTime.now());
        existingTicket.setEscalatedAt(null);
        existingTicket.setEscalationReason(null);
        existingTicket.setResolvedAt(null);

        Ticket updatedTicket = ticketRepository.save(existingTicket);

        return mapToTicketResponse(updatedTicket);
    }

    public TicketResponse acceptTicketAssignedToLoggedInSupport(
            Long id,
            String supportUserEmail) {

        Ticket existingTicket = findTicketEntityById(id);

        User supportUser = findActiveSupportUserByEmail(supportUserEmail);

        if (isTerminalStatus(existingTicket.getStatus())) {
            throw new InvalidTicketOperationException(
                    "Resolved, closed, or cancelled tickets cannot be accepted."
            );
        }

        if (existingTicket.getAssignedTo() == null
                || !existingTicket.getAssignedTo().getId()
                .equals(supportUser.getId())) {

            throw new InvalidTicketOperationException(
                    "This ticket is not assigned to your support account."
            );
        }

        existingTicket.setAssignmentStatus(TicketAssignmentStatus.ACCEPTED);
        existingTicket.setStatus(TicketStatus.IN_PROGRESS);
        existingTicket.setEscalatedAt(null);
        existingTicket.setEscalationReason(null);

        Ticket updatedTicket = ticketRepository.save(existingTicket);

        return mapToTicketResponse(updatedTicket);
    }

    public TicketResponse rejectTicketAssignedToLoggedInSupport(
            Long id,
            String supportUserEmail) {

        Ticket existingTicket = findTicketEntityById(id);

        User supportUser = findActiveSupportUserByEmail(supportUserEmail);

        if (!hasWeeklyRejectionAllowance(supportUser)) {
            existingTicket.setAssignmentStatus(TicketAssignmentStatus.ACCEPTED);
            existingTicket.setStatus(TicketStatus.IN_PROGRESS);
            existingTicket.setEscalatedAt(null);
            existingTicket.setEscalationReason(null);

            Ticket updatedTicket = ticketRepository.save(existingTicket);

            return mapToTicketResponse(updatedTicket);
        }

        if (isTerminalStatus(existingTicket.getStatus())) {
            throw new InvalidTicketOperationException(
                    "Resolved, closed, or cancelled tickets cannot be rejected."
            );
        }

        if (existingTicket.getAssignmentStatus()
                != TicketAssignmentStatus.PENDING_ACCEPTANCE) {

            throw new InvalidTicketOperationException(
                    "Only tickets waiting for support review can be rejected."
            );
        }

        if (existingTicket.getAssignedTo() == null
                || !existingTicket.getAssignedTo().getId()
                .equals(supportUser.getId())) {

            throw new InvalidTicketOperationException(
                    "You can reject only tickets assigned to your support account."
            );
        }

        saveRejectionIfMissing(existingTicket, supportUser);

        assignTicketUsingWorkloadRules(
                existingTicket,
                supportUser.getId()
        );

        Ticket updatedTicket = ticketRepository.save(existingTicket);

        return mapToTicketResponse(updatedTicket);
    }

    public void escalateStalePendingAssignments(
            long acceptanceTimeoutMinutes) {

        LocalDateTime cutoffTime = LocalDateTime.now()
                .minusMinutes(acceptanceTimeoutMinutes);

        List<Ticket> staleTickets =
                ticketRepository
                        .findByAssignmentStatusAndLastAssignedAtBeforeOrderByLastAssignedAtAsc(
                                TicketAssignmentStatus.PENDING_ACCEPTANCE,
                                cutoffTime
                        );

        for (Ticket ticket : staleTickets) {
            escalateTicket(
                    ticket,
                    "Ticket was not accepted or rejected within "
                            + acceptanceTimeoutMinutes
                            + " minutes."
            );

            ticketRepository.save(ticket);
        }
    }

    public void deleteTicket(Long id) {

        Ticket existingTicket = findTicketEntityById(id);

        ticketRepository.delete(existingTicket);
    }

    public Ticket findTicketEntityById(Long id) {

        return ticketRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Ticket not found with ID: " + id
                        )
                );
    }

    private void assignTicketUsingWorkloadRules(
            Ticket ticket,
            Long rejectedBySupportUserId) {

        Optional<User> selectedSupportUser =
                findEligibleSupportUserWithLowestWorkload(
                        ticket,
                        rejectedBySupportUserId
                );

        if (selectedSupportUser.isEmpty()) {
            escalateTicket(
                    ticket,
                    "No eligible IT support user is available. Admin review is required."
            );

            return;
        }

        User supportUser = selectedSupportUser.get();

        ticket.setAssignedTo(supportUser);
        ticket.setLastAssignedAt(LocalDateTime.now());
        ticket.setEscalatedAt(null);
        ticket.setEscalationReason(null);
        ticket.setResolvedAt(null);

        if (hasWeeklyRejectionAllowance(supportUser)) {
            ticket.setAssignmentStatus(TicketAssignmentStatus.PENDING_ACCEPTANCE);
            ticket.setStatus(TicketStatus.OPEN);
        } else {
            ticket.setAssignmentStatus(TicketAssignmentStatus.ACCEPTED);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
        }
    }

    private Optional<User> findEligibleSupportUserWithLowestWorkload(
            Ticket ticket,
            Long rejectedBySupportUserId) {

        List<User> activeSupportUsers =
                userRepository.findByRoleAndActiveTrue(UserRole.IT_SUPPORT);

        Comparator<User> supportUserComparator =
                Comparator
                        .comparingLong(
                                (User user) ->
                                        getActiveWorkloadForSupport(
                                                user.getId()
                                        )
                        )
                        .thenComparingLong(User::getId);

        return activeSupportUsers
                .stream()
                .filter(user -> rejectedBySupportUserId == null
                        || !user.getId().equals(rejectedBySupportUserId))
                .filter(user -> ticket.getId() == null
                        || !ticketRejectionRepository
                        .existsByTicket_IdAndSupportUser_Id(
                                ticket.getId(),
                                user.getId()
                        ))
                .min(supportUserComparator);
    }

    private long getActiveWorkloadForSupport(Long supportUserId) {

        return ticketRepository.countActiveTicketsForSupport(
                supportUserId,
                List.of(
                        TicketAssignmentStatus.PENDING_ACCEPTANCE,
                        TicketAssignmentStatus.ACCEPTED
                ),
                List.of(
                        TicketStatus.RESOLVED,
                        TicketStatus.CLOSED,
                        TicketStatus.CANCELLED
                )
        );
    }

    private void movePendingAssignmentsToAcceptedIfRejectionLimitReached(
            User supportUser) {

        if (hasWeeklyRejectionAllowance(supportUser)) {
            return;
        }

        List<Ticket> pendingTickets =
                ticketRepository
                        .findByAssignmentStatusAndAssignedTo_IdOrderByCreatedAtDesc(
                                TicketAssignmentStatus.PENDING_ACCEPTANCE,
                                supportUser.getId()
                        );

        for (Ticket ticket : pendingTickets) {
            ticket.setAssignmentStatus(TicketAssignmentStatus.ACCEPTED);
            ticket.setStatus(TicketStatus.IN_PROGRESS);
            ticket.setEscalatedAt(null);
            ticket.setEscalationReason(null);
            ticketRepository.save(ticket);
        }
    }

    private void saveRejectionIfMissing(
            Ticket ticket,
            User supportUser) {

        if (!ticketRejectionRepository.existsByTicket_IdAndSupportUser_Id(
                ticket.getId(),
                supportUser.getId()
        )) {
            TicketRejection ticketRejection = new TicketRejection();

            ticketRejection.setTicket(ticket);
            ticketRejection.setSupportUser(supportUser);

            ticketRejectionRepository.save(ticketRejection);
        }
    }

    private boolean hasWeeklyRejectionAllowance(User supportUser) {
        return getCurrentWeekRejectionCount(supportUser.getId())
                < weeklyRejectionLimit;
    }

    private long getCurrentWeekRejectionCount(Long supportUserId) {

        LocalDate today = LocalDate.now();

        LocalDate weekStartDate = today.with(DayOfWeek.MONDAY);
        LocalDateTime weekStart = weekStartDate.atStartOfDay();
        LocalDateTime nextWeekStart = weekStart.plusWeeks(1);

        return ticketRejectionRepository
                .countBySupportUser_IdAndRejectedAtGreaterThanEqualAndRejectedAtLessThan(
                        supportUserId,
                        weekStart,
                        nextWeekStart
                );
    }

    private void escalateTicket(
            Ticket ticket,
            String reason) {

        ticket.setAssignedTo(null);
        ticket.setAssignmentStatus(TicketAssignmentStatus.ESCALATED);
        ticket.setStatus(TicketStatus.OPEN);
        ticket.setResolvedAt(null);
        ticket.setEscalatedAt(LocalDateTime.now());
        ticket.setEscalationReason(reason);
    }

    private User findActiveSupportUserByEmail(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UnauthorizedException(
                                "Logged-in support user was not found."
                        )
                );

        if (!user.isActive()) {
            throw new UnauthorizedException(
                    "Logged-in support user account is inactive."
            );
        }

        if (user.getRole() != UserRole.IT_SUPPORT) {
            throw new UnauthorizedException(
                    "Only IT support users can perform this action."
            );
        }

        return user;
    }

    private void validateAssignableSupportUser(User user) {

        if (!user.isActive()) {
            throw new InvalidTicketOperationException(
                    "Ticket can be assigned only to an active IT support user."
            );
        }

        if (user.getRole() != UserRole.IT_SUPPORT) {
            throw new InvalidTicketOperationException(
                    "Ticket can be assigned only to an IT support user."
            );
        }
    }

    private void validateAssignedSupportCanModifyTicket(
            Ticket ticket,
            String supportUserEmail) {

        User supportUser = findActiveSupportUserByEmail(supportUserEmail);

        if (ticket.getAssignedTo() == null
                || !ticket.getAssignedTo().getId()
                .equals(supportUser.getId())) {

            throw new InvalidTicketOperationException(
                    "You can update only tickets assigned to your support account."
            );
        }

        if (ticket.getAssignmentStatus() != TicketAssignmentStatus.ACCEPTED) {
            throw new InvalidTicketOperationException(
                    "You must have an active assigned ticket before updating it."
            );
        }
    }

    private boolean isTerminalStatus(TicketStatus status) {
        return status == TicketStatus.RESOLVED
                || status == TicketStatus.CLOSED
                || status == TicketStatus.CANCELLED;
    }

    private void updateResolvedTimestamp(
            Ticket ticket,
            TicketStatus status) {

        if (status == TicketStatus.RESOLVED
                || status == TicketStatus.CLOSED) {

            if (ticket.getResolvedAt() == null) {
                ticket.setResolvedAt(LocalDateTime.now());
            }

            return;
        }

        ticket.setResolvedAt(null);
    }

    private TicketResponse mapToTicketResponse(Ticket ticket) {

        Long createdByUserId = null;
        String createdByName = null;

        if (ticket.getCreatedBy() != null) {
            createdByUserId = ticket.getCreatedBy().getId();
            createdByName = ticket.getCreatedBy().getFullName();
        }

        Long assignedToUserId = null;
        String assignedToName = null;

        if (ticket.getAssignedTo() != null) {
            assignedToUserId = ticket.getAssignedTo().getId();
            assignedToName = ticket.getAssignedTo().getFullName();
        }

        return new TicketResponse(
                ticket.getId(),
                ticket.getTicketNumber(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getCategory(),
                ticket.getStatus(),
                ticket.getAssignmentStatus(),
                createdByUserId,
                createdByName,
                assignedToUserId,
                assignedToName,
                ticket.getCreatedAt(),
                ticket.getUpdatedAt(),
                ticket.getDueAt(),
                ticket.getResolvedAt(),
                ticket.getLastAssignedAt(),
                ticket.getEscalatedAt(),
                ticket.getEscalationReason()
        );
    }
}