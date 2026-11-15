package com.technicel.service;

import com.technicel.dto.CreateDayOffRequest;
import com.technicel.dto.DayOffDecisionRequest;
import com.technicel.dto.DayOffRequestResponse;
import com.technicel.dto.PageResponse;
import com.technicel.entity.DayOffRequest;
import com.technicel.entity.DayOffStatus;
import com.technicel.entity.User;
import com.technicel.exception.BusinessRuleException;
import com.technicel.exception.InvalidRequestException;
import com.technicel.mapper.DayOffRequestMapper;
import com.technicel.repository.DayOffRequestRepository;
import com.technicel.security.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DayOffRequestService {

    private final DayOffRequestRepository dayOffRequestRepository;
    private final CurrentUserService currentUserService;
    private final DayOffRequestMapper dayOffRequestMapper;

    public DayOffRequestService(
            DayOffRequestRepository dayOffRequestRepository,
            CurrentUserService currentUserService,
            DayOffRequestMapper dayOffRequestMapper
    ) {
        this.dayOffRequestRepository = dayOffRequestRepository;
        this.currentUserService = currentUserService;
        this.dayOffRequestMapper = dayOffRequestMapper;
    }

    @Transactional
    public DayOffRequestResponse create(CreateDayOffRequest request) {
        User technician = currentUserService.requireTechnician();
        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new InvalidRequestException("Start date must be on or before end date");
        }

        DayOffRequest entity = new DayOffRequest();
        entity.setTechnician(technician);
        entity.setStartDate(request.getStartDate());
        entity.setEndDate(request.getEndDate());
        entity.setNumberOfDays(daysInclusive(request.getStartDate(), request.getEndDate()));
        entity.setReason(request.getReason());
        entity.setStatus(DayOffStatus.PENDING);
        return dayOffRequestMapper.toResponse(dayOffRequestRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public List<DayOffRequestResponse> listMine() {
        User technician = currentUserService.requireTechnician();
        return dayOffRequestRepository.findByTechnicianIdOrderByCreatedAtDesc(technician.getId()).stream()
                .map(dayOffRequestMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<DayOffRequestResponse> listAll(DayOffStatus status, Pageable pageable) {
        Page<DayOffRequest> page = status == null
                ? dayOffRequestRepository.findAllByOrderByCreatedAtDesc(pageable)
                : dayOffRequestRepository.findByStatusOrderByCreatedAtDesc(status, pageable);
        return new PageResponse<>(page.map(dayOffRequestMapper::toResponse));
    }

    @Transactional
    public DayOffRequestResponse approve(Long id, DayOffDecisionRequest request) {
        return decide(id, DayOffStatus.APPROVED, request);
    }

    @Transactional
    public DayOffRequestResponse reject(Long id, DayOffDecisionRequest request) {
        return decide(id, DayOffStatus.REJECTED, request);
    }

    private DayOffRequestResponse decide(Long id, DayOffStatus newStatus, DayOffDecisionRequest request) {
        User admin = currentUserService.requireCurrentUser();
        DayOffRequest entity = dayOffRequestRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Day-off request was not found"));
        if (entity.getStatus() != DayOffStatus.PENDING) {
            throw new BusinessRuleException("This day-off request has already been processed");
        }
        entity.setStatus(newStatus);
        entity.setAdminComment(request == null ? null : request.getAdminComment());
        entity.setApprovedBy(admin);
        entity.setDecisionDate(LocalDate.now());
        return dayOffRequestMapper.toResponse(dayOffRequestRepository.save(entity));
    }

    private int daysInclusive(LocalDate start, LocalDate end) {
        return (int) ChronoUnit.DAYS.between(start, end) + 1;
    }
}
