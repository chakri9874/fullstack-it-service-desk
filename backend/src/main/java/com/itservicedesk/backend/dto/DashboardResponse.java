package com.itservicedesk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class DashboardResponse {

    private DashboardSummaryResponse summary;

    private List<DashboardChartItemResponse> ticketsByStatus;

    private List<DashboardChartItemResponse> ticketsByCategory;
}
