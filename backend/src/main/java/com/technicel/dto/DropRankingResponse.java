package com.technicel.dto;

import java.time.LocalDate;
import java.util.List;

public class DropRankingResponse {

    private RankingPeriod period;
    private LocalDate fromDate;
    private LocalDate toDate;
    private long totalDrops;
    private List<DropRankingRowResponse> rows;

    public DropRankingResponse(
            RankingPeriod period,
            LocalDate fromDate,
            LocalDate toDate,
            long totalDrops,
            List<DropRankingRowResponse> rows
    ) {
        this.period = period;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.totalDrops = totalDrops;
        this.rows = rows;
    }

    public RankingPeriod getPeriod() {
        return period;
    }

    public LocalDate getFromDate() {
        return fromDate;
    }

    public LocalDate getToDate() {
        return toDate;
    }

    public long getTotalDrops() {
        return totalDrops;
    }

    public List<DropRankingRowResponse> getRows() {
        return rows;
    }
}
