package com.technicel.mapper;

import com.technicel.dto.DailyProgressResponse;
import com.technicel.entity.DailyProgress;
import org.springframework.stereotype.Component;

@Component
public class DailyProgressMapper {

    public DailyProgressResponse toResponse(DailyProgress progress) {
        return new DailyProgressResponse(
                progress.getId(),
                progress.getWorkList().getId(),
                progress.getWorkDate(),
                progress.getCompletedDrops(),
                progress.getCreatedAt(),
                progress.getUpdatedAt()
        );
    }
}
