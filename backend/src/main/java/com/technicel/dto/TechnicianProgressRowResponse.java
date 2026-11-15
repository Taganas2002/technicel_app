package com.technicel.dto;

import com.technicel.entity.ListType;
import com.technicel.entity.WorkListStatus;
import java.time.LocalDate;

public class TechnicianProgressRowResponse {

    private Long technicianId;
    private String technicianCode;
    private String fullName;
    private String nip;
    private boolean active;
    private Long currentListId;
    private ListType listType;
    private Integer initialDrops;
    private Integer completedDrops;
    private Integer remainingDrops;
    private Double progressPercentage;
    private WorkListStatus status;
    private int dropsCompletedToday;
    private int problemDropCount;
    private LocalDate referenceDate;

    public Long getTechnicianId() {
        return technicianId;
    }

    public void setTechnicianId(Long technicianId) {
        this.technicianId = technicianId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public void setTechnicianCode(String technicianCode) {
        this.technicianCode = technicianCode;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getNip() {
        return nip;
    }

    public void setNip(String nip) {
        this.nip = nip;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Long getCurrentListId() {
        return currentListId;
    }

    public void setCurrentListId(Long currentListId) {
        this.currentListId = currentListId;
    }

    public ListType getListType() {
        return listType;
    }

    public void setListType(ListType listType) {
        this.listType = listType;
    }

    public Integer getInitialDrops() {
        return initialDrops;
    }

    public void setInitialDrops(Integer initialDrops) {
        this.initialDrops = initialDrops;
    }

    public Integer getCompletedDrops() {
        return completedDrops;
    }

    public void setCompletedDrops(Integer completedDrops) {
        this.completedDrops = completedDrops;
    }

    public Integer getRemainingDrops() {
        return remainingDrops;
    }

    public void setRemainingDrops(Integer remainingDrops) {
        this.remainingDrops = remainingDrops;
    }

    public Double getProgressPercentage() {
        return progressPercentage;
    }

    public void setProgressPercentage(Double progressPercentage) {
        this.progressPercentage = progressPercentage;
    }

    public WorkListStatus getStatus() {
        return status;
    }

    public void setStatus(WorkListStatus status) {
        this.status = status;
    }

    public int getDropsCompletedToday() {
        return dropsCompletedToday;
    }

    public void setDropsCompletedToday(int dropsCompletedToday) {
        this.dropsCompletedToday = dropsCompletedToday;
    }

    public int getProblemDropCount() {
        return problemDropCount;
    }

    public void setProblemDropCount(int problemDropCount) {
        this.problemDropCount = problemDropCount;
    }

    public LocalDate getReferenceDate() {
        return referenceDate;
    }

    public void setReferenceDate(LocalDate referenceDate) {
        this.referenceDate = referenceDate;
    }
}
