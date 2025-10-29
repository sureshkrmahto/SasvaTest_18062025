package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.model.MigrationExecution;
import com.acme.javamigrator.engine.reporting.ReportService;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
public class ReportController {

    private final InMemoryStore store = InMemoryStore.getInstance();
    private final ReportService reportService = new ReportService();

    @GetMapping("/{migrationId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> report(@PathVariable String migrationId) {
        var ex = store.migrations.get(migrationId);
        var logs = store.migrationLogs.getOrDefault(migrationId, List.of());
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        if (ex == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Migration not found";
            return ResponseEntity.status(404).body(res);
        }
        res.success = true; res.data = reportService.generateDetailed(ex, logs);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{migrationId}/summary")
    public ResponseEntity<ApiResponse<Map<String, Object>>> summary(@PathVariable String migrationId) {
        var ex = store.migrations.get(migrationId);
        var logs = store.migrationLogs.getOrDefault(migrationId, List.of());
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        if (ex == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Migration not found";
            return ResponseEntity.status(404).body(res);
        }
        res.success = true; res.data = reportService.generateSummary(ex, logs);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{migrationId}/detailed")
    public ResponseEntity<ApiResponse<Map<String, Object>>> detailed(@PathVariable String migrationId) {
        return report(migrationId);
    }
}
