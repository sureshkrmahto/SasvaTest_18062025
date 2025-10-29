package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analysis")
@Tag(name = "Analysis")
public class AnalysisResultsController {
    private final InMemoryStore store = InMemoryStore.getInstance();

    @GetMapping("/{analysisId}/results")
    public ResponseEntity<ApiResponse<AnalysisResult>> results(@PathVariable String analysisId) {
        AnalysisResult ar = store.analyses.get(analysisId);
        ApiResponse<AnalysisResult> res = new ApiResponse<>();
        if (ar == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Analysis not found";
            return ResponseEntity.status(404).body(res);
        }
        res.success = true; res.data = ar;
        return ResponseEntity.ok(res);
    }
}
