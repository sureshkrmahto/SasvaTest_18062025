package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.migration.MigrationService;
import com.acme.javamigrator.engine.model.MigrationExecution;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/migration")
@Tag(name = "Migration")
public class MigrationController {

    private final InMemoryStore store = InMemoryStore.getInstance();
    private final MigrationService migrationService = new MigrationService();

    @PostMapping("/execute")
    public ResponseEntity<ApiResponse<Map<String, Object>>> execute(@RequestBody Map<String, Object> req) throws Exception {
        String planId = (String) req.get("plan_id");
        var plan = store.plans.get(planId);
        if (plan == null) {
            ApiResponse<Map<String, Object>> res = new ApiResponse<>();
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Plan not found";
            return ResponseEntity.status(404).body(res);
        }
        ProjectMetadata pm = store.projects.get(plan.getProjectId());
        Map<String, Object> configuration = (Map<String, Object>) req.getOrDefault("configuration", Map.of());
        if (configuration.containsKey("source_folder")) pm.setSourceFolder((String) configuration.get("source_folder"));
        if (configuration.containsKey("target_folder")) pm.setTargetFolder((String) configuration.get("target_folder"));
        if (configuration.containsKey("backup_folder")) pm.setBackupFolder((String) configuration.get("backup_folder"));

        boolean backup = ((Map<String, Object>) req.getOrDefault("options", Map.of())).getOrDefault("backup_before_execution", Boolean.TRUE).equals(Boolean.TRUE);

        var result = migrationService.execute(pm, backup);
        store.migrations.put(result.execution.getMigrationId(), result.execution);
        store.migrationLogs.put(result.execution.getMigrationId(), result.logs);

        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("migration_id", result.execution.getMigrationId());
        data.put("plan_id", plan.getPlanId());
        data.put("status", result.execution.getStatus().name().toLowerCase());
        data.put("execution_mode", req.getOrDefault("execution_mode", "step_by_step"));
        data.put("current_phase", Map.of("phase_id", 1, "name", "Preparation", "status", "completed"));
        data.put("progress_url", "/api/v1/migration/" + result.execution.getMigrationId() + "/progress");
        data.put("logs_url", "/api/v1/migration/" + result.execution.getMigrationId() + "/logs");
        res.success = true; res.data = data;
        return ResponseEntity.accepted().body(res);
    }

    @GetMapping("/{migrationId}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> progress(@PathVariable String migrationId) {
        var ex = store.migrations.get(migrationId);
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        if (ex == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Migration not found";
            return ResponseEntity.status(404).body(res);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("migration_id", migrationId);
        data.put("status", ex.getStatus().name().toLowerCase());
        data.put("progress", Map.of("percentage", 100, "current_stage", "completed"));
        res.success = true; res.data = data;
        return ResponseEntity.ok(res);
    }

    @GetMapping("/{migrationId}/logs")
    public ResponseEntity<ApiResponse<Map<String, Object>>> logs(@PathVariable String migrationId) {
        List<String> logs = store.migrationLogs.getOrDefault(migrationId, List.of());
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true; res.data = Map.of("logs", logs);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/{migrationId}/rollback")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rollback(@PathVariable String migrationId) {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true; res.data = Map.of("status", "rolled_back", "migration_id", migrationId);
        return ResponseEntity.ok(res);
    }
}
