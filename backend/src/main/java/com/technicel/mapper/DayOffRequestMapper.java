package com.technicel.mapper;

import com.technicel.dto.DayOffRequestResponse;
import com.technicel.entity.DayOffRequest;
import com.technicel.entity.User;
import org.springframework.stereotype.Component;

@Component
public class DayOffRequestMapper {

    public DayOffRequestResponse toResponse(DayOffRequest request) {
        User technician = request.getTechnician();
        User processedBy = request.getApprovedBy();
        return new DayOffRequestResponse(
                request.getId(),
                technician.getId(),
                technician.getTechnicianCode(),
                technician.getFirstName(),
                technician.getLastName(),
                request.getStartDate(),
                request.getEndDate(),
                request.getNumberOfDays(),
                request.getReason(),
                request.getStatus(),
                request.getAdminComment(),
                processedBy == null ? null : processedBy.getId(),
                processedBy == null ? null : processedBy.getFirstName() + " " + processedBy.getLastName(),
                request.getCreatedAt(),
                request.getDecisionDate()
        );
    }
}
