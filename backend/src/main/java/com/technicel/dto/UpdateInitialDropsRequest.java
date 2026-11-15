package com.technicel.dto;

import jakarta.validation.constraints.Min;

public class UpdateInitialDropsRequest {

    @Min(1)
    private int initialDropCount;

    public int getInitialDropCount() {
        return initialDropCount;
    }

    public void setInitialDropCount(int initialDropCount) {
        this.initialDropCount = initialDropCount;
    }
}
