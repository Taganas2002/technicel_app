package com.technicel.dto;

public class AdminDashboardResponse {

    private DashboardSummaryResponse summary;
    private PageResponse<TechnicianProgressRowResponse> technicians;

    public AdminDashboardResponse(
            DashboardSummaryResponse summary,
            PageResponse<TechnicianProgressRowResponse> technicians
    ) {
        this.summary = summary;
        this.technicians = technicians;
    }

    public DashboardSummaryResponse getSummary() {
        return summary;
    }

    public PageResponse<TechnicianProgressRowResponse> getTechnicians() {
        return technicians;
    }
}
