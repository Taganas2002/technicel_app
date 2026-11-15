package com.technicel.dto;

public class DailyProgressSaveResponse {

    private DailyProgressResponse progress;
    private WorkListResponse workList;

    public DailyProgressSaveResponse(DailyProgressResponse progress, WorkListResponse workList) {
        this.progress = progress;
        this.workList = workList;
    }

    public DailyProgressResponse getProgress() {
        return progress;
    }

    public WorkListResponse getWorkList() {
        return workList;
    }
}
