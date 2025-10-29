package com.acme.javamigrator.engine.model;

public class MigrationStep {
    public enum StepType { JAVA_VERSION_UPGRADE, SPRING_BOOT_UPGRADE, DEPENDENCY_UPGRADE, CODE_TRANSFORMATION }

    private StepType type;
    private String description;

    public MigrationStep() {}
    public MigrationStep(StepType type, String description) {
        this.type = type;
        this.description = description;
    }

    public StepType getType() { return type; }
    public void setType(StepType type) { this.type = type; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
