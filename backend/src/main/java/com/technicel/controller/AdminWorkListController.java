package com.technicel.controller;

import com.technicel.dto.UpdateInitialDropsRequest;
import com.technicel.dto.WorkListResponse;
import com.technicel.service.WorkListService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminWorkListController {

    private final WorkListService workListService;

    public AdminWorkListController(WorkListService workListService) {
        this.workListService = workListService;
    }

    @GetMapping("/api/admin/technicians/{technicianId}/lists")
    public ResponseEntity<List<WorkListResponse>> listForTechnician(@PathVariable Long technicianId) {
        return ResponseEntity.ok(workListService.listForTechnician(technicianId));
    }

    @DeleteMapping("/api/admin/lists/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        workListService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/api/admin/lists/{id}/initial-drops")
    public ResponseEntity<WorkListResponse> updateInitialDrops(
            @PathVariable Long id,
            @Valid @RequestBody UpdateInitialDropsRequest request
    ) {
        return ResponseEntity.ok(workListService.updateInitialDrops(id, request));
    }
}
