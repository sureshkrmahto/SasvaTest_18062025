package com.acme.javamigrator.engine.model;

import java.util.ArrayList;
import java.util.List;

public class MigrationProgress {
    private int percentage;
    private String currentStage;
    private final List<Stage> stages = new ArrayList<>();

    public static class Stage {
        private String name;
        private String status; // pending|in_progress|completed
        private long duration;
        private Integer itemsProcessed;
        private Integer totalItems;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        public long getDuration() { return duration; }
        public void setDuration(long duration) { this.duration = duration; }
        public Integer getItemsProcessed() { return itemsProcessed; }
        public void setItemsProcessed(Integer itemsProcessed) { this.itemsProcessed = itemsProcessed; }
        public Integer getTotalItems() { return totalItems; }
        public void setTotalItems(Integer totalItems) { this.totalItems = totalItems; }
    }

    public int getPercentage() { return percentage; }
    public void setPercentage(int percentage) { this.percentage = percentage; }
    public String getCurrentStage() { return currentStage; }
    public void setCurrentStage(String currentStage) { this.currentStage = currentStage; }
    public List<Stage> getStages() { return stages; }
}
