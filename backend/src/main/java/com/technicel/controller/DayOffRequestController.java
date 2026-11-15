package com.technicel.controller;

import com.technicel.dto.CreateDayOffRequest;
import com.technicel.dto.DayOffRequestResponse;
import com.technicel.service.DayOffRequestService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/day-off-requests")
public class DayOffRequestController {

    private final DayOffRequestService dayOffRequestService;

    public DayOffRequestController(DayOffRequestService dayOffRequestService) {
        this.dayOffRequestService = dayOffRequestService;
    }

    @PostMapping
    public ResponseEntity<DayOffRequestResponse> create(@Valid @RequestBody CreateDayOffRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dayOffRequestService.create(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<DayOffRequestResponse>> myRequests() {
        return ResponseEntity.ok(dayOffRequestService.listMine());
    }
}
