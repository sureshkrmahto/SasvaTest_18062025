package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.analysis.AnalyzerService;
import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analysis")
public class AnalysisController {

    private final InMemoryStore store = InMemoryStore.getInstance();
    private final AnalyzerService analyzer = new AnalyzerService();

    @PostMapping("/start")
    public ResponseEntity<ApiResponse<Map<String, Object>>> start(@RequestBody Map<String, Object> req) throws Exception {
        String projectId = (String) req.get("project_id");
        Map<String, Object> configuration = (Map<String, Object>) req.getOrDefault("configuration", Map.of());
        ProjectMetadata pm = store.projects.get(projectId);
        if (pm == null) {
            ApiResponse<Map<String, Object>> res = new ApiResponse<>();
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Project not found";
            return ResponseEntity.status(404).body(res);
        }
        // override source/target if provided
        if (configuration.containsKey("source_folder")) pm.setSourceFolder((String) configuration.get("source_folder"));
        if (configuration.containsKey("target_folder")) pm.setTargetFolder((String) configuration.get("target_folder"));

        AnalysisResult ar = analyzer.analyze(pm);
        store.analyses.put(ar.getAnalysisId(), ar);

        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("analysis_id", ar.getAnalysisId());
        data.put("project_id", pm.getProjectId());
        data.put("status", "started");
        data.put("analysis_type", req.getOrDefault("analysis_type", "comprehensive"));
        data.put("estimated_duration", 300);
        data.put("started_at", Instant.now().toString());
        data.put("progress_url", "/api/v1/analysis/" + ar.getAnalysisId() + "/progress");
        res.success = true; res.data = data;
        return ResponseEntity.accepted().body(res);
    }

    @GetMapping("/{analysisId}/progress")
    public ResponseEntity<ApiResponse<Map<String, Object>>> progress(@PathVariable String analysisId) {
        AnalysisResult ar = store.analyses.get(analysisId);
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        if (ar == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Analysis not found";
            return ResponseEntity.status(404).body(res);
        }
        Map<String, Object> stages = Map.of(
                "percentage", 100,
                "current_stage", "completed",
                "stages", new Object[]{ Map.of("name","project_parsing","status","completed","duration",45,"items_processed", ar.getStatistics().get("file_count")) }
        );
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("analysis_id", analysisId);
        data.put("status", "completed");
        data.put("progress", stages);
        data.put("estimated_completion", Instant.now().toString());
        data.put("last_updated", Instant.now().toString());
        res.success = true; res.data = data;
        return ResponseEntity.ok(res);
    }
}
