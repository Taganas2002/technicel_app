package com.technicel.dto;

import com.technicel.entity.ListType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class CreateWorkListRequest {

    @NotNull
    private ListType listType;

    @Min(1)
    private int initialDropCount;

    @NotNull
    private LocalDate referenceDate;

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
}
