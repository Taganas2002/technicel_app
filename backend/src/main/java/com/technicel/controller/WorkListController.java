package com.technicel.controller;

import com.technicel.dto.CreateWorkListRequest;
import com.technicel.dto.FinishWithProblemsRequest;
import com.technicel.dto.DailyProgressRequest;
import com.technicel.dto.DailyProgressResponse;
import com.technicel.dto.DailyProgressSaveResponse;
import com.technicel.dto.WorkListResponse;
import com.technicel.service.WorkListService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/lists")
public class WorkListController {

    private final WorkListService workListService;

    public WorkListController(WorkListService workListService) {
        this.workListService = workListService;
    }

    @PostMapping
    public ResponseEntity<WorkListResponse> create(@Valid @RequestBody CreateWorkListRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workListService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<WorkListResponse>> listMine() {
        return ResponseEntity.ok(workListService.listMine());
    }

    @GetMapping("/active")
    public ResponseEntity<WorkListResponse> active() {
        return ResponseEntity.ok(workListService.getActive());
    }

    @GetMapping("/history")
    public ResponseEntity<List<WorkListResponse>> history() {
        return ResponseEntity.ok(workListService.history());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkListResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(workListService.getMineById(id));
    }

    @PostMapping("/{id}/finish-with-problems")
    public ResponseEntity<WorkListResponse> finishWithProblems(
            @PathVariable Long id,
            @Valid @RequestBody(required = false) FinishWithProblemsRequest request
    ) {
        return ResponseEntity.ok(workListService.finishWithProblems(id, request));
    }

    @PostMapping("/{id}/progress")
    public ResponseEntity<DailyProgressSaveResponse> addProgress(
            @PathVariable Long id,
            @Valid @RequestBody DailyProgressRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workListService.addProgress(id, request));
    }

    @GetMapping("/{id}/progress")
    public ResponseEntity<List<DailyProgressResponse>> listProgress(@PathVariable Long id) {
        return ResponseEntity.ok(workListService.listProgress(id));
    }
}
