package com.acme.javamigrator.engine.reporting;

import com.acme.javamigrator.engine.model.MigrationExecution;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ReportService {
    public Map<String, Object> generateSummary(MigrationExecution execution, List<String> logs) {
        Map<String, Object> summary = new HashMap<>();
        summary.put("migration_id", execution.getMigrationId());
        summary.put("status", execution.getStatus().name());
        summary.put("started_at", execution.getStartedAt());
        summary.put("completed_at", execution.getCompletedAt());
        summary.put("log_count", logs.size());
        return summary;
    }

    public Map<String, Object> generateDetailed(MigrationExecution execution, List<String> logs) {
        Map<String, Object> detailed = new HashMap<>(generateSummary(execution, logs));
        detailed.put("logs", logs);
        return detailed;
    }
}
