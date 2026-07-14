package com.itservicedesk.backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardChartItemResponse {

    private String label;
    private long count;
}