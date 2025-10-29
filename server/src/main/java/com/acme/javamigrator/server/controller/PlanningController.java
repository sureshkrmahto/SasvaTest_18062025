package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.MigrationPlan;
import com.acme.javamigrator.engine.planning.PlanService;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/planning")
@Tag(name = "Planning")
public class PlanningController {

    private final InMemoryStore store = InMemoryStore.getInstance();
    private final PlanService planner = new PlanService();

    @PostMapping("/generate")
    public ResponseEntity<ApiResponse<Map<String, Object>>> generate(@RequestBody Map<String, Object> req) {
        String analysisId = (String) req.get("analysis_id");
        AnalysisResult ar = store.analyses.get(analysisId);
        if (ar == null) {
            ApiResponse<Map<String, Object>> res = new ApiResponse<>();
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Analysis not found";
            return ResponseEntity.status(404).body(res);
        }
        MigrationPlan plan = planner.generatePlan(ar.getProjectId(), ar, "incremental");
        store.plans.put(plan.getPlanId(), plan);

        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("plan_id", plan.getPlanId());
        data.put("analysis_id", analysisId);
        data.put("status", "generated");
        data.put("generated_at", Instant.now().toString());
        data.put("migration_strategy", plan.getMigrationStrategy());
        data.put("preview_url", "/api/v1/planning/" + plan.getPlanId());
        data.put("execution_url", "/api/v1/migration/execute");
        res.success = true; res.data = data;
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping("/{planId}")
    public ResponseEntity<ApiResponse<MigrationPlan>> get(@PathVariable String planId) {
        ApiResponse<MigrationPlan> res = new ApiResponse<>();
        MigrationPlan plan = store.plans.get(planId);
        if (plan == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Plan not found";
            return ResponseEntity.status(404).body(res);
        }
        res.success = true; res.data = plan;
        return ResponseEntity.ok(res);
    }
}
