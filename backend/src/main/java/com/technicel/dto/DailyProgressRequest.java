package com.technicel.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public class DailyProgressRequest {

    @NotNull
    private LocalDate workDate;

    @Positive
    private int completedDrops;

    public LocalDate getWorkDate() {
        return workDate;
    }

    public void setWorkDate(LocalDate workDate) {
        this.workDate = workDate;
    }

    public int getCompletedDrops() {
        return completedDrops;
    }

    public void setCompletedDrops(int completedDrops) {
        this.completedDrops = completedDrops;
    }
}
