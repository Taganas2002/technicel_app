package com.technicel.dto;

import com.technicel.entity.ListType;
import com.technicel.entity.WorkListStatus;
import java.time.LocalDate;

public class WorkListResponse {

    private Long id;
    private Long technicianId;
    private ListType listType;
    private int initialDropCount;
    private int totalCompleted;
    private int remaining;
    private double progressPercentage;
    private LocalDate referenceDate;
    private WorkListStatus status;
    private LocalDate completionDate;
    private int problemDropCount;
    private String problemNote;

    public WorkListResponse(
            Long id,
            Long technicianId,
            ListType listType,
            int initialDropCount,
            int totalCompleted,
            int remaining,
            double progressPercentage,
            LocalDate referenceDate,
            WorkListStatus status,
            LocalDate completionDate,
            int problemDropCount,
            String problemNote
    ) {
        this.id = id;
        this.technicianId = technicianId;
        this.listType = listType;
        this.initialDropCount = initialDropCount;
        this.totalCompleted = totalCompleted;
        this.remaining = remaining;
        this.progressPercentage = progressPercentage;
        this.referenceDate = referenceDate;
        this.status = status;
        this.completionDate = completionDate;
        this.problemDropCount = problemDropCount;
        this.problemNote = problemNote;
    }

    public Long getId() {
        return id;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public ListType getListType() {
        return listType;
    }

    public int getInitialDropCount() {
        return initialDropCount;
    }

    public int getTotalCompleted() {
        return totalCompleted;
    }

    public int getRemaining() {
        return remaining;
    }

    public double getProgressPercentage() {
        return progressPercentage;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public WorkListStatus getStatus() {
        return status;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public int getProblemDropCount() {
        return problemDropCount;
    }

    public String getProblemNote() {
        return problemNote;
    }
}
