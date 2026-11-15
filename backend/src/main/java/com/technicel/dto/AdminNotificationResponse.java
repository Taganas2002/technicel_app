package com.technicel.dto;

import com.technicel.entity.ListType;
import com.technicel.entity.NotificationType;
import java.time.LocalDateTime;

public class AdminNotificationResponse {

    private Long id;
    private NotificationType type;
    private String title;
    private String message;
    private Long technicianId;
    private String technicianCode;
    private String technicianName;
    private Long workListId;
    private ListType listType;
    private boolean read;
    private LocalDateTime createdAt;

    public AdminNotificationResponse(
            Long id,
            NotificationType type,
            String title,
            String message,
            Long technicianId,
            String technicianCode,
            String technicianName,
            Long workListId,
            ListType listType,
            boolean read,
            LocalDateTime createdAt
    ) {
        this.id = id;
        this.type = type;
        this.title = title;
        this.message = message;
        this.technicianId = technicianId;
        this.technicianCode = technicianCode;
        this.technicianName = technicianName;
        this.workListId = workListId;
        this.listType = listType;
        this.read = read;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public NotificationType getType() {
        return type;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public String getTechnicianName() {
        return technicianName;
    }

    public Long getWorkListId() {
        return workListId;
    }

    public ListType getListType() {
        return listType;
    }

    public boolean isRead() {
        return read;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
