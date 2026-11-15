package com.technicel.dto;

import com.technicel.entity.DayOffStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class DayOffRequestResponse {

    private Long id;
    private Long technicianId;
    private String technicianCode;
    private String technicianFirstName;
    private String technicianLastName;
    private LocalDate startDate;
    private LocalDate endDate;
    private int numberOfDays;
    private String reason;
    private DayOffStatus status;
    private String adminComment;
    private Long processedById;
    private String processedByName;
    private LocalDateTime createdAt;
    private LocalDate decisionDate;

    public DayOffRequestResponse(
            Long id,
            Long technicianId,
            String technicianCode,
            String technicianFirstName,
            String technicianLastName,
            LocalDate startDate,
            LocalDate endDate,
            int numberOfDays,
            String reason,
            DayOffStatus status,
            String adminComment,
            Long processedById,
            String processedByName,
            LocalDateTime createdAt,
            LocalDate decisionDate
    ) {
        this.id = id;
        this.technicianId = technicianId;
        this.technicianCode = technicianCode;
        this.technicianFirstName = technicianFirstName;
        this.technicianLastName = technicianLastName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.numberOfDays = numberOfDays;
        this.reason = reason;
        this.status = status;
        this.adminComment = adminComment;
        this.processedById = processedById;
        this.processedByName = processedByName;
        this.createdAt = createdAt;
        this.decisionDate = decisionDate;
    }

    public Long getId() {
        return id;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public String getTechnicianFirstName() {
        return technicianFirstName;
    }

    public String getTechnicianLastName() {
        return technicianLastName;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public int getNumberOfDays() {
        return numberOfDays;
    }

    public String getReason() {
        return reason;
    }

    public DayOffStatus getStatus() {
        return status;
    }

    public String getAdminComment() {
        return adminComment;
    }

    public Long getProcessedById() {
        return processedById;
    }

    public String getProcessedByName() {
        return processedByName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDate getDecisionDate() {
        return decisionDate;
    }
}
