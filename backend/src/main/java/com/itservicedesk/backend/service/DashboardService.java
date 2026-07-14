package com.itservicedesk.backend.service;

import com.itservicedesk.backend.dto.DashboardChartItemResponse;
import com.itservicedesk.backend.dto.DashboardResponse;
import com.itservicedesk.backend.dto.DashboardSummaryResponse;
import com.itservicedesk.backend.enums.TicketAssignmentStatus;
import com.itservicedesk.backend.enums.TicketCategory;
import com.itservicedesk.backend.enums.TicketStatus;
import com.itservicedesk.backend.repository.TicketRepository;
import com.itservicedesk.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

@Service
public class DashboardService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;

    public DashboardService(
            TicketRepository ticketRepository,
            UserRepository userRepository) {

        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
    }

    public DashboardResponse getDashboardSummary() {

        DashboardSummaryResponse summary = new DashboardSummaryResponse(
                ticketRepository.count(),
                ticketRepository.countByStatus(TicketStatus.OPEN),
                ticketRepository.countByStatus(TicketStatus.IN_PROGRESS),
                ticketRepository.countByStatus(TicketStatus.RESOLVED),
                ticketRepository.countByAssignmentStatus(TicketAssignmentStatus.ESCALATED),
                userRepository.count(),
                userRepository.countByActiveTrue()
        );

        List<DashboardChartItemResponse> ticketsByStatus =
                Arrays.stream(TicketStatus.values())
                        .map(status ->
                                new DashboardChartItemResponse(
                                        status.name(),
                                        ticketRepository.countByStatus(status)
                                )
                        )
                        .toList();

        List<DashboardChartItemResponse> ticketsByCategory =
                Arrays.stream(TicketCategory.values())
                        .map(category ->
                                new DashboardChartItemResponse(
                                        category.name(),
                                        ticketRepository.countByCategory(category)
                                )
                        )
                        .toList();

        return new DashboardResponse(
                summary,
                ticketsByStatus,
                ticketsByCategory
        );
    }
}
