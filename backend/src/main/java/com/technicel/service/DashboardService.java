package com.technicel.service;

import com.technicel.dto.AdminDashboardResponse;
import com.technicel.dto.DropRankingResponse;
import com.technicel.dto.DropRankingRowResponse;
import com.technicel.dto.PageResponse;
import com.technicel.dto.RankingPeriod;
import com.technicel.dto.TechnicianProgressRowResponse;
import com.technicel.entity.ListType;
import com.technicel.entity.WorkListStatus;
import com.technicel.repository.DashboardQueryRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DashboardService {

    private final DashboardQueryRepository dashboardQueryRepository;

    public DashboardService(DashboardQueryRepository dashboardQueryRepository) {
        this.dashboardQueryRepository = dashboardQueryRepository;
    }

    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard(
            String search,
            ListType listType,
            Boolean active,
            WorkListStatus listStatus,
            Pageable pageable
    ) {
        long total = dashboardQueryRepository.countTechnicians(search, listType, active, listStatus);
        List<TechnicianProgressRowResponse> rows = dashboardQueryRepository.findTechnicians(
                search, listType, active, listStatus, pageable
        );
        return new AdminDashboardResponse(
                dashboardQueryRepository.loadSummary(),
                new PageResponse<>(rows, pageable.getPageNumber(), pageable.getPageSize(), total)
        );
    }

    @Transactional(readOnly = true)
    public DropRankingResponse getDropRankings(RankingPeriod period, LocalDate date) {
        LocalDate selected = date == null ? LocalDate.now() : date;
        LocalDate fromDate = period == RankingPeriod.MONTH ? selected.withDayOfMonth(1) : selected;
        LocalDate toDate = period == RankingPeriod.MONTH
                ? selected.withDayOfMonth(selected.lengthOfMonth())
                : selected;
        List<DropRankingRowResponse> source = dashboardQueryRepository.findDropRankings(fromDate, toDate);
        List<DropRankingRowResponse> ranked = new ArrayList<>(source.size());
        long totalDrops = 0;
        int previousDrops = -1;
        int rank = 0;
        for (int index = 0; index < source.size(); index++) {
            DropRankingRowResponse row = source.get(index);
            totalDrops += row.getCompletedDrops();
            if (index == 0 || row.getCompletedDrops() != previousDrops) {
                rank = index + 1;
            }
            ranked.add(new DropRankingRowResponse(
                    rank,
                    row.getTechnicianId(),
                    row.getTechnicianCode(),
                    row.getFullName(),
                    row.getCompletedDrops()
            ));
            previousDrops = row.getCompletedDrops();
        }
        return new DropRankingResponse(period, fromDate, toDate, totalDrops, ranked);
    }
}
