package com.technicel.controller;

import com.technicel.dto.AdminDashboardResponse;
import com.technicel.dto.DropRankingResponse;
import com.technicel.dto.RankingPeriod;
import com.technicel.entity.ListType;
import com.technicel.entity.WorkListStatus;
import com.technicel.service.DashboardService;
import java.time.LocalDate;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final DashboardService dashboardService;

    public AdminDashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<AdminDashboardResponse> getDashboard(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ListType listType,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) WorkListStatus listStatus,
            @PageableDefault(size = 20, sort = "technicianCode") Pageable pageable
    ) {
        return ResponseEntity.ok(dashboardService.getDashboard(search, listType, active, listStatus, pageable));
    }

    @GetMapping("/rankings")
    public ResponseEntity<DropRankingResponse> getRankings(
            @RequestParam(defaultValue = "DAY") RankingPeriod period,
            @RequestParam(required = false) LocalDate date
    ) {
        return ResponseEntity.ok(dashboardService.getDropRankings(period, date));
    }
}
