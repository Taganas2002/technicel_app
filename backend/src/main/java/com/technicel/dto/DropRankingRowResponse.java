package com.technicel.dto;

public class DropRankingRowResponse {

    private int rank;
    private Long technicianId;
    private String technicianCode;
    private String fullName;
    private int completedDrops;

    public DropRankingRowResponse(
            int rank,
            Long technicianId,
            String technicianCode,
            String fullName,
            int completedDrops
    ) {
        this.rank = rank;
        this.technicianId = technicianId;
        this.technicianCode = technicianCode;
        this.fullName = fullName;
        this.completedDrops = completedDrops;
    }

    public int getRank() {
        return rank;
    }

    public Long getTechnicianId() {
        return technicianId;
    }

    public String getTechnicianCode() {
        return technicianCode;
    }

    public String getFullName() {
        return fullName;
    }

    public int getCompletedDrops() {
        return completedDrops;
    }
}
