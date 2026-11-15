package com.technicel.mapper;

import com.technicel.dto.WorkListResponse;
import com.technicel.entity.WorkList;
import com.technicel.entity.WorkListStatus;
import org.springframework.stereotype.Component;

@Component
public class WorkListMapper {

    public WorkListResponse toResponse(WorkList list, int totalCompleted) {
        int initial = list.getInitialDropCount();
        int problemDropCount = Math.max(list.getProblemDropCount(), 0);
        boolean finished = list.getStatus() == WorkListStatus.FINISHED || totalCompleted >= initial;
        int remaining = finished ? 0 : Math.max(initial - totalCompleted, 0);
        double progressPercentage = initial == 0
                ? 0
                : Math.min(100.0, Math.round((totalCompleted * 10000.0) / initial) / 100.0);
        WorkListStatus status = finished ? WorkListStatus.FINISHED : WorkListStatus.IN_PROGRESS;
        return new WorkListResponse(
                list.getId(),
                list.getTechnician().getId(),
                list.getListType(),
                initial,
                totalCompleted,
                remaining,
                progressPercentage,
                list.getReferenceDate(),
                status,
                list.getCompletionDate(),
                finished ? problemDropCount : 0,
                finished ? list.getProblemNote() : null
        );
    }
}
