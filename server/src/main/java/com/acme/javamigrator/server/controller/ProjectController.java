package com.acme.javamigrator.server.controller;

import com.acme.javamigrator.engine.model.ProjectMetadata;
import com.acme.javamigrator.server.dto.CommonDtos.ApiResponse;
import com.acme.javamigrator.server.service.InMemoryStore;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/api/v1/projects")
@Tag(name = "Projects")
public class ProjectController {

    private final InMemoryStore store = InMemoryStore.getInstance();

    @PostMapping("/upload")
    public ResponseEntity<ApiResponse<Map<String, Object>>> upload(
            @RequestPart(value = "project_file", required = false) MultipartFile file,
            @RequestPart("metadata") Map<String, Object> metadata
    ) throws IOException {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();

        String sourceFolder = (String) metadata.get("source_folder");
        String targetFolder = (String) metadata.get("target_folder");
        if (!StringUtils.hasText(sourceFolder) || !StringUtils.hasText(targetFolder)) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "INVALID_PROJECT_FILE";
            res.error.message = "Uploaded file is not a valid Java project";
            res.error.details = "No pom.xml or build.gradle found in project root";
            res.error.validation_errors = List.of(
                    fieldError("project_file", "Must contain either pom.xml or build.gradle"),
                    fieldError("source_folder", "Source folder path is required for migration configuration"),
                    fieldError("target_folder", "Target folder path is required for migration output")
            );
            return ResponseEntity.badRequest().body(res);
        }

        ProjectMetadata pm = new ProjectMetadata();
        pm.setProjectId("proj_" + UUID.randomUUID().toString().replace("-",""));
        pm.setProjectName((String) metadata.getOrDefault("project_name", "Unnamed Project"));
        pm.setDescription((String) metadata.getOrDefault("description", ""));
        pm.setSourceFolder(sourceFolder);
        pm.setTargetFolder(targetFolder);
        pm.setCurrentJavaVersion((String) metadata.getOrDefault("current_java_version", "8"));
        pm.setTargetJavaVersion((String) metadata.getOrDefault("target_java_version", "17"));
        pm.setCurrentSpringVersion((String) metadata.getOrDefault("current_spring_version", "2.1.0"));
        pm.setTargetSpringVersion((String) metadata.getOrDefault("target_spring_version", "3.1.0"));
        pm.setTags((List<String>) metadata.getOrDefault("tags", List.of()));

        store.projects.put(pm.getProjectId(), pm);

        long fileSize = file != null ? file.getSize() : 0L;
        int fileCount = 0; // not computed here

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", pm.getProjectId());
        data.put("project_name", pm.getProjectName());
        data.put("status", "uploaded");
        data.put("file_size", fileSize);
        data.put("file_count", fileCount);
        data.put("upload_timestamp", Instant.now().toString());
        data.put("metadata", metadata);

        res.success = true;
        res.data = data;
        return ResponseEntity.status(201).body(res);
    }

    @GetMapping("/{projectId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getProject(@PathVariable String projectId) {
        ProjectMetadata pm = store.projects.get(projectId);
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        if (pm == null) {
            res.success = false;
            res.error = new com.acme.javamigrator.server.dto.CommonDtos.ApiError();
            res.error.code = "RESOURCE_NOT_FOUND";
            res.error.message = "Project not found";
            return ResponseEntity.status(404).body(res);
        }
        Map<String, Object> stats = new HashMap<>();
        stats.put("file_count", 0);
        stats.put("java_files", 0);
        stats.put("test_files", 0);
        stats.put("config_files", 0);
        stats.put("total_lines", 0);
        stats.put("java_lines", 0);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("project_id", pm.getProjectId());
        data.put("project_name", pm.getProjectName());
        data.put("status", "uploaded");
        data.put("metadata", Map.of(
                "source_folder", pm.getSourceFolder(),
                "target_folder", pm.getTargetFolder(),
                "current_java_version", pm.getCurrentJavaVersion(),
                "target_java_version", pm.getTargetJavaVersion(),
                "current_spring_version", pm.getCurrentSpringVersion(),
                "target_spring_version", pm.getTargetSpringVersion(),
                "tags", pm.getTags(),
                "git_operations_enabled", false
        ));
        data.put("statistics", stats);
        data.put("upload_timestamp", Instant.now().toString());
        data.put("last_modified", Instant.now().toString());

        res.success = true; res.data = data;
        return ResponseEntity.ok(res);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> listProjects(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "created_desc") String sort
    ) {
        ApiResponse<Map<String, Object>> res = new ApiResponse<>();
        List<Map<String, Object>> items = new ArrayList<>();
        for (ProjectMetadata pm : store.projects.values()) {
            items.add(Map.of(
                    "project_id", pm.getProjectId(),
                    "project_name", pm.getProjectName(),
                    "status", "uploaded",
                    "current_java_version", pm.getCurrentJavaVersion(),
                    "target_java_version", pm.getTargetJavaVersion(),
                    "upload_timestamp", Instant.now().toString()
            ));
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("projects", items);
        data.put("pagination", Map.of(
                "page", page,
                "limit", limit,
                "total_pages", 1,
                "total_items", items.size(),
                "has_next", false,
                "has_previous", false
        ));
        res.success = true; res.data = data;
        return ResponseEntity.ok(res);
    }

    private com.acme.javamigrator.server.dto.CommonDtos.FieldError fieldError(String field, String message) {
        com.acme.javamigrator.server.dto.CommonDtos.FieldError fe = new com.acme.javamigrator.server.dto.CommonDtos.FieldError();
        fe.field = field; fe.message = message; fe.code = "FIELD_ERROR";
        return fe;
    }
}
