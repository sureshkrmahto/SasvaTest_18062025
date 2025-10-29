package com.acme.javamigrator.engine.model;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class AnalysisResult {
    private String analysisId;
    private String projectId;
    private Instant startedAt;
    private Instant completedAt;
    private Map<String, Object> statistics = new HashMap<>();
    private Map<String, Object> findings = new HashMap<>();

    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public Map<String, Object> getStatistics() { return statistics; }
    public Map<String, Object> getFindings() { return findings; }
}
