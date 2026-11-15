package com.technicel.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "work_lists")
public class WorkList extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "technician_id", nullable = false)
    private User technician;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "list_type", nullable = false, length = 10)
    private ListType listType;

    @Min(1)
    @Column(name = "initial_drop_count", nullable = false)
    private int initialDropCount;

    @NotNull
    @Column(name = "reference_date", nullable = false)
    private LocalDate referenceDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WorkListStatus status = WorkListStatus.IN_PROGRESS;

    @Column(name = "completion_date")
    private LocalDate completionDate;

    @Min(0)
    @Column(name = "problem_drop_count", nullable = false)
    private int problemDropCount;

    @Column(name = "problem_note", length = 500)
    private String problemNote;

    @OneToMany(mappedBy = "workList", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DailyProgress> dailyProgress = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public User getTechnician() {
        return technician;
    }

    public void setTechnician(User technician) {
        this.technician = technician;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public int getInitialDropCount() {
        return initialDropCount;
    }

    public void setInitialDropCount(int initialDropCount) {
        this.initialDropCount = initialDropCount;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }

    public WorkListStatus getStatus() {
        return status;
    }

    public void setStatus(WorkListStatus status) {
        this.status = status;
    }

    public LocalDate getCompletionDate() {
        return completionDate;
    }

    public void setCompletionDate(LocalDate completionDate) {
        this.completionDate = completionDate;
    }

    public int getProblemDropCount() {
        return problemDropCount;
    }

    public void setProblemDropCount(int problemDropCount) {
        this.problemDropCount = problemDropCount;
    }

    public String getProblemNote() {
        return problemNote;
    }

    public void setProblemNote(String problemNote) {
        this.problemNote = problemNote;
    }

    public List<DailyProgress> getDailyProgress() {
        return dailyProgress;
    }
}
