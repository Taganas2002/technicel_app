package com.technicel.controller;

import com.technicel.dto.DayOffDecisionRequest;
import com.technicel.dto.DayOffRequestResponse;
import com.technicel.dto.PageResponse;
import com.technicel.entity.DayOffStatus;
import com.technicel.service.DayOffRequestService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/day-off-requests")
public class AdminDayOffRequestController {

    private final DayOffRequestService dayOffRequestService;

    public AdminDayOffRequestController(DayOffRequestService dayOffRequestService) {
        this.dayOffRequestService = dayOffRequestService;
    }

    @GetMapping
    public ResponseEntity<PageResponse<DayOffRequestResponse>> list(
            @RequestParam(required = false) DayOffStatus status,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(dayOffRequestService.listAll(status, pageable));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<DayOffRequestResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DayOffDecisionRequest request
    ) {
        return ResponseEntity.ok(dayOffRequestService.approve(id, request == null ? new DayOffDecisionRequest() : request));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<DayOffRequestResponse> reject(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) DayOffDecisionRequest request
    ) {
        return ResponseEntity.ok(dayOffRequestService.reject(id, request == null ? new DayOffDecisionRequest() : request));
    }
}
