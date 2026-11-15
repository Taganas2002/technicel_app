package com.technicel.dto;

import java.util.List;

public class AdminNotificationListResponse {

    private long unreadCount;
    private List<AdminNotificationResponse> notifications;

    public AdminNotificationListResponse(long unreadCount, List<AdminNotificationResponse> notifications) {
        this.unreadCount = unreadCount;
        this.notifications = notifications;
    }

    public long getUnreadCount() {
        return unreadCount;
    }

    public List<AdminNotificationResponse> getNotifications() {
        return notifications;
    }
}
