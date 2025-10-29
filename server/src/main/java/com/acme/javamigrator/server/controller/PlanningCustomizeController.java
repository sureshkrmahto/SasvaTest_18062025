package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.model.MigrationPlan;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/planning")
@Tag(name = "Planning")
public class PlanningCustomizeController {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @PutMapping("/{planId}/customize")
    public ResponseEntity<ApiResponse<MigrationPlan>> customize(@PathVariable String planId, @RequestBody Map<String, Object> body) {
        MigrationPlan plan = store.plans.get(planId);
        ApiResponse<MigrationPlan> res = new ApiResponse<>();
        if (plan == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Plan not found";
            return ResponseEntity.status(404).body(res);
        }
        // MVP: accept but do not persist complex customization beyond strategy
        Object strategy = body.get("migration_strategy");
        if (strategy instanceof String s && !s.isBlank()) {
            plan.setMigrationStrategy(s);
        }
        res.success = true; res.data = plan;
        return ResponseEntity.ok(res);
    }
}
