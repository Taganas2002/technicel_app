package com.technicel.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

@Entity
@Table(
        name = "daily_progress",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_daily_progress_list_date",
                columnNames = {"work_list_id", "work_date"}
        )
)
public class DailyProgress extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "work_list_id", nullable = false)
    private WorkList workList;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @NotNull
    @Column(name = "work_date", nullable = false)
    private LocalDate workDate;

    @Positive
    @Column(name = "completed_drops", nullable = false)
    private int completedDrops;

    public Long getId() {
        return id;
    }

    public WorkList getWorkList() {
        return workList;
    }

    public void setWorkList(WorkList workList) {
        this.workList = workList;
    }

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

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
