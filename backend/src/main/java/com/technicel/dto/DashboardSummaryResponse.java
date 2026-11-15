package com.technicel.dto;

public class DashboardSummaryResponse {

    private long totalTechnicians;
    private long activeTechnicians;
    private long techniciansWorkingToday;
    private long activeLists;
    private long finishedLists;
    private long dropsCompletedToday;
    private long pendingDayOffRequests;

    public DashboardSummaryResponse(
            long totalTechnicians,
            long activeTechnicians,
            long techniciansWorkingToday,
            long activeLists,
            long finishedLists,
            long dropsCompletedToday,
            long pendingDayOffRequests
    ) {
        this.totalTechnicians = totalTechnicians;
        this.activeTechnicians = activeTechnicians;
        this.techniciansWorkingToday = techniciansWorkingToday;
        this.activeLists = activeLists;
        this.finishedLists = finishedLists;
        this.dropsCompletedToday = dropsCompletedToday;
        this.pendingDayOffRequests = pendingDayOffRequests;
    }

    public long getTotalTechnicians() {
        return totalTechnicians;
    }

    public long getActiveTechnicians() {
        return activeTechnicians;
    }

    public long getTechniciansWorkingToday() {
        return techniciansWorkingToday;
    }

    public long getActiveLists() {
        return activeLists;
    }

    public long getFinishedLists() {
        return finishedLists;
    }

    public long getDropsCompletedToday() {
        return dropsCompletedToday;
    }

    public long getPendingDayOffRequests() {
        return pendingDayOffRequests;
    }
}
