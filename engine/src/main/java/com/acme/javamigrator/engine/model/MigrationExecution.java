package com.acme.javamigrator.engine.model;

import java.time.Instant;

public class MigrationExecution {
    public enum Status { STARTED, IN_PROGRESS, COMPLETED, FAILED, ROLLED_BACK }

    private String migrationId;
    private String planId;
    private String projectId;
    private Status status;
    private Instant startedAt;
    private Instant completedAt;
    private MigrationProgress progress = new MigrationProgress();

    public String getMigrationId() { return migrationId; }
    public void setMigrationId(String migrationId) { this.migrationId = migrationId; }

    public String getPlanId() { return planId; }
    public void setPlanId(String planId) { this.planId = planId; }

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Instant getStartedAt() { return startedAt; }
    public void setStartedAt(Instant startedAt) { this.startedAt = startedAt; }

    public Instant getCompletedAt() { return completedAt; }
    public void setCompletedAt(Instant completedAt) { this.completedAt = completedAt; }

    public MigrationProgress getProgress() { return progress; }
}
