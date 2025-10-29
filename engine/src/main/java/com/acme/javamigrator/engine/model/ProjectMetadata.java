package com.acme.javamigrator.engine.model;

import java.util.List;
import java.util.Objects;

public class ProjectMetadata {
    private String projectId;
    private String projectName;
    private String description;
    private String sourceFolder;
    private String targetFolder;
    private String backupFolder;
    private String currentJavaVersion; // "8","9","10","11"
    private String targetJavaVersion;  // "9".."17"
    private String currentSpringVersion;
    private String targetSpringVersion;
    private List<String> tags;

    public String getProjectId() { return projectId; }
    public void setProjectId(String projectId) { this.projectId = projectId; }

    public String getProjectName() { return projectName; }
    public void setProjectName(String projectName) { this.projectName = projectName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getSourceFolder() { return sourceFolder; }
    public void setSourceFolder(String sourceFolder) { this.sourceFolder = sourceFolder; }

    public String getTargetFolder() { return targetFolder; }
    public void setTargetFolder(String targetFolder) { this.targetFolder = targetFolder; }

    public String getBackupFolder() { return backupFolder; }
    public void setBackupFolder(String backupFolder) { this.backupFolder = backupFolder; }

    public String getCurrentJavaVersion() { return currentJavaVersion; }
    public void setCurrentJavaVersion(String currentJavaVersion) { this.currentJavaVersion = currentJavaVersion; }

    public String getTargetJavaVersion() { return targetJavaVersion; }
    public void setTargetJavaVersion(String targetJavaVersion) { this.targetJavaVersion = targetJavaVersion; }

    public String getCurrentSpringVersion() { return currentSpringVersion; }
    public void setCurrentSpringVersion(String currentSpringVersion) { this.currentSpringVersion = currentSpringVersion; }

    public String getTargetSpringVersion() { return targetSpringVersion; }
    public void setTargetSpringVersion(String targetSpringVersion) { this.targetSpringVersion = targetSpringVersion; }

    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }

    @Override
    public String toString() {
        return "ProjectMetadata{" +
                "projectId='" + projectId + '\'' +
                ", sourceFolder='" + sourceFolder + '\'' +
                ", targetFolder='" + targetFolder + '\'' +
                ", currentJavaVersion='" + currentJavaVersion + '\'' +
                ", targetJavaVersion='" + targetJavaVersion + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ProjectMetadata)) return false;
        ProjectMetadata that = (ProjectMetadata) o;
        return Objects.equals(projectId, that.projectId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(projectId);
    }
}
