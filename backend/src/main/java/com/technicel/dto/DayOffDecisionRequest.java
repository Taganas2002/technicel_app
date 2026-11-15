package com.technicel.dto;

import jakarta.validation.constraints.Size;

public class DayOffDecisionRequest {

    @Size(max = 500)
    private String adminComment;

    public String getAdminComment() {
        return adminComment;
    }

    public void setAdminComment(String adminComment) {
        this.adminComment = adminComment;
    }
}
