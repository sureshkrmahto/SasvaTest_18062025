package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/reports")
@Tag(name = "Reports")
public class ReportExportController {

    @PostMapping("/{migrationId}/export")
    public ResponseEntity<ApiResponse<Map<String, Object>>> export(@PathVariable String migrationId) {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        res.success = true; res.data = Map.of("exported", true, "format", "html", "path", "/tmp/report-" + migrationId + ".html");
        return ResponseEntity.ok(res);
    }
}
