package com.technicel.service;

import com.technicel.dto.CreateWorkListRequest;
import com.technicel.dto.DailyProgressRequest;
import com.technicel.dto.FinishWithProblemsRequest;
import com.technicel.dto.DailyProgressResponse;
import com.technicel.dto.DailyProgressSaveResponse;
import com.technicel.dto.UpdateInitialDropsRequest;
import com.technicel.dto.WorkListResponse;
import com.technicel.entity.DailyProgress;
import com.technicel.entity.Role;
import com.technicel.entity.User;
import com.technicel.entity.WorkList;
import com.technicel.entity.WorkListStatus;
import com.technicel.exception.BusinessRuleException;
import com.technicel.exception.DuplicateResourceException;
import com.technicel.mapper.DailyProgressMapper;
import com.technicel.mapper.WorkListMapper;
import com.technicel.repository.AdminNotificationRepository;
import com.technicel.repository.DailyProgressRepository;
import com.technicel.repository.UserRepository;
import com.technicel.repository.WorkListRepository;
import com.technicel.security.CurrentUserService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkListService {

    private final WorkListRepository workListRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final AdminNotificationRepository adminNotificationRepository;
    private final UserRepository userRepository;
    private final CurrentUserService currentUserService;
    private final WorkListMapper workListMapper;
    private final DailyProgressMapper dailyProgressMapper;
    private final AdminNotificationService adminNotificationService;

    public WorkListService(
            WorkListRepository workListRepository,
            DailyProgressRepository dailyProgressRepository,
            AdminNotificationRepository adminNotificationRepository,
            UserRepository userRepository,
            CurrentUserService currentUserService,
            WorkListMapper workListMapper,
            DailyProgressMapper dailyProgressMapper,
            AdminNotificationService adminNotificationService
    ) {
        this.workListRepository = workListRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        this.adminNotificationRepository = adminNotificationRepository;
        this.userRepository = userRepository;
        this.currentUserService = currentUserService;
        this.workListMapper = workListMapper;
        this.dailyProgressMapper = dailyProgressMapper;
        this.adminNotificationService = adminNotificationService;
    }

    @Transactional
    public WorkListResponse create(CreateWorkListRequest request) {
        User technician = currentUserService.requireTechnician();
        if (workListRepository.existsByTechnicianIdAndStatus(technician.getId(), WorkListStatus.IN_PROGRESS)) {
            throw new DuplicateResourceException("An in-progress work list already exists");
        }

        WorkList list = new WorkList();
        list.setTechnician(technician);
        list.setListType(request.getListType());
        list.setInitialDropCount(request.getInitialDropCount());
        list.setReferenceDate(request.getReferenceDate());
        list.setStatus(WorkListStatus.IN_PROGRESS);
        list.setCompletionDate(null);
        WorkList saved = workListRepository.save(list);
        adminNotificationService.notifyListCreated(technician, saved);
        return toResponse(saved);
    }

    @Transactional
    public WorkListResponse finishWithProblems(Long listId, FinishWithProblemsRequest request) {
        currentUserService.requireTechnician();
        WorkList list = requireList(listId);
        currentUserService.assertCanAccessWorkList(list);
        if (list.getStatus() != WorkListStatus.IN_PROGRESS) {
            throw new BusinessRuleException("Only an in-progress list can be finished with problems");
        }
        int completed = completedDrops(list.getId());
        int problem = list.getInitialDropCount() - completed;
        if (problem <= 0) {
            throw new BusinessRuleException("This list is already complete");
        }
        String note = request == null || request.getNote() == null ? "" : request.getNote().trim();
        list.setProblemDropCount(problem);
        list.setProblemNote(note.isEmpty() ? null : note);
        list.setStatus(WorkListStatus.FINISHED);
        list.setCompletionDate(LocalDate.now());
        return toResponse(workListRepository.save(list));
    }

    @Transactional(readOnly = true)
    public List<WorkListResponse> listMine() {
        User technician = currentUserService.requireTechnician();
        return workListRepository.findByTechnicianIdOrderByCreatedAtDesc(technician.getId()).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkListResponse getMineById(Long id) {
        WorkList list = requireList(id);
        currentUserService.assertCanAccessWorkList(list);
        return toResponse(list);
    }

    @Transactional(readOnly = true)
    public WorkListResponse getActive() {
        User technician = currentUserService.requireTechnician();
        WorkList list = workListRepository
                .findByTechnicianIdAndStatus(technician.getId(), WorkListStatus.IN_PROGRESS)
                .orElseThrow(() -> new EntityNotFoundException("No in-progress work list was found"));
        return toResponse(list);
    }

    @Transactional(readOnly = true)
    public List<WorkListResponse> history() {
        User technician = currentUserService.requireTechnician();
        return workListRepository
                .findByTechnicianIdAndStatusOrderByCompletionDateDesc(technician.getId(), WorkListStatus.FINISHED)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<WorkListResponse> listForTechnician(Long technicianId) {
        requireExistingTechnician(technicianId);
        return workListRepository.findByTechnicianIdOrderByCreatedAtDesc(technicianId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional
    public void delete(Long listId) {
        WorkList list = requireList(listId);
        adminNotificationRepository.detachWorkList(list.getId());
        workListRepository.delete(list);
    }

    @Transactional
    public WorkListResponse updateInitialDrops(Long listId, UpdateInitialDropsRequest request) {
        WorkList list = requireList(listId);
        list.setInitialDropCount(request.getInitialDropCount());
        refreshStatus(list);
        return toResponse(workListRepository.save(list));
    }

    @Transactional
    public DailyProgressSaveResponse addProgress(Long listId, DailyProgressRequest request) {
        User technician = currentUserService.requireTechnician();
        WorkList list = requireList(listId);
        currentUserService.assertCanAccessWorkList(list);

        DailyProgress existing = dailyProgressRepository
                .findByWorkListAndWorkDate(list, request.getWorkDate())
                .orElse(null);

        if (list.getStatus() == WorkListStatus.FINISHED && existing == null) {
            throw new BusinessRuleException("Finished work lists cannot receive more progress");
        }
        int previousForDay = existing == null ? 0 : existing.getCompletedDrops();
        int currentTotal = completedDrops(list.getId());
        int nextTotal = currentTotal - previousForDay + request.getCompletedDrops();
        if (nextTotal > list.getInitialDropCount()) {
            throw new BusinessRuleException("Completed drops cannot exceed the initial drop count");
        }

        DailyProgress progress = existing;
        if (progress == null) {
            progress = new DailyProgress();
            progress.setWorkList(list);
            progress.setTechnician(technician);
            progress.setWorkDate(request.getWorkDate());
        }
        progress.setCompletedDrops(request.getCompletedDrops());
        progress = dailyProgressRepository.saveAndFlush(progress);

        refreshStatus(list);
        workListRepository.save(list);

        return new DailyProgressSaveResponse(dailyProgressMapper.toResponse(progress), toResponse(list));
    }

    @Transactional(readOnly = true)
    public List<DailyProgressResponse> listProgress(Long listId) {
        WorkList list = requireList(listId);
        currentUserService.assertCanAccessWorkList(list);
        return dailyProgressRepository.findByWorkListIdOrderByWorkDateAsc(listId).stream()
                .map(dailyProgressMapper::toResponse)
                .toList();
    }

    private void refreshStatus(WorkList list) {
        int totalCompleted = completedDrops(list.getId());
        int initial = list.getInitialDropCount();
        if (totalCompleted >= initial) {
            list.setStatus(WorkListStatus.FINISHED);
            list.setProblemDropCount(0);
            list.setProblemNote(null);
            if (list.getCompletionDate() == null) {
                list.setCompletionDate(LocalDate.now());
            }
            return;
        }
        if (list.getProblemDropCount() > 0) {
            list.setStatus(WorkListStatus.FINISHED);
            list.setProblemDropCount(initial - totalCompleted);
            if (list.getCompletionDate() == null) {
                list.setCompletionDate(LocalDate.now());
            }
            return;
        }
        list.setStatus(WorkListStatus.IN_PROGRESS);
        list.setCompletionDate(null);
    }

    private WorkListResponse toResponse(WorkList list) {
        int totalCompleted = completedDrops(list.getId());
        return workListMapper.toResponse(list, totalCompleted);
    }

    private int completedDrops(Long workListId) {
        Long total = dailyProgressRepository.sumCompletedByWorkListId(workListId);
        return total == null ? 0 : total.intValue();
    }

    private WorkList requireList(Long id) {
        return workListRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Work list was not found"));
    }

    private void requireExistingTechnician(Long technicianId) {
        User user = userRepository.findById(technicianId)
                .orElseThrow(() -> new EntityNotFoundException("Technician was not found"));
        if (user.getRole() != Role.TECHNICIAN) {
            throw new EntityNotFoundException("Technician was not found");
        }
    }
}
