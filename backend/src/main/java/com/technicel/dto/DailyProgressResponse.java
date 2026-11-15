package com.technicel.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class DailyProgressResponse {

    private Long id;
    private Long workListId;
    private LocalDate workDate;
    private int completedDrops;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public DailyProgressResponse(
            Long id,
            Long workListId,
            LocalDate workDate,
            int completedDrops,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {
        this.id = id;
        this.workListId = workListId;
        this.workDate = workDate;
        this.completedDrops = completedDrops;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getWorkListId() {
        return workListId;
    }

    public LocalDate getWorkDate() {
        return workDate;
    }

    public int getCompletedDrops() {
        return completedDrops;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
