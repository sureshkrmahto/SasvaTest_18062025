package com.acme.javamigrator.engine.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class MigrationPlan {
    private String planId;
    private String analysisId;
    private String projectId;
    private String migrationStrategy; // incremental | big_bang
    private Instant generatedAt;
    private final List<MigrationStep> steps = new ArrayList<>();

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public String getAnalysisId() { return analysisId; }
    public void setAnalysisId(String analysisId) { this.analysisId = analysisId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getMigrationStrategy() { return migrationStrategy; }
    public void setMigrationStrategy(String migrationStrategy) { this.migrationStrategy = migrationStrategy; }

    public Instant getGeneratedAt() { return generatedAt; }
    public void setGeneratedAt(Instant generatedAt) { this.generatedAt = generatedAt; }

    public List<MigrationStep> getSteps() { return steps; }
}
