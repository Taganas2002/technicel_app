package com.technicel.controller;

import com.technicel.dto.AdminNotificationListResponse;
import com.technicel.dto.AdminNotificationResponse;
import com.technicel.service.AdminNotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/notifications")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    public AdminNotificationController(AdminNotificationService adminNotificationService) {
        this.adminNotificationService = adminNotificationService;
    }

    @GetMapping
    public ResponseEntity<AdminNotificationListResponse> list() {
        return ResponseEntity.ok(adminNotificationService.listRecent());
    }

    @PostMapping("/{id}/read")
    public ResponseEntity<AdminNotificationResponse> markRead(@PathVariable Long id) {
        return ResponseEntity.ok(adminNotificationService.markRead(id));
    }

    @PostMapping("/read-all")
    public ResponseEntity<AdminNotificationListResponse> markAllRead() {
        return ResponseEntity.ok(adminNotificationService.markAllRead());
    }
}
