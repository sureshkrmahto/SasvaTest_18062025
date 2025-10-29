package com.acme.javamigrator.engine.migration;

import com.acme.javamigrator.engine.model.MigrationExecution;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import com.acme.javamigrator.engine.rules.RuleEngine;
import com.acme.javamigrator.engine.util.FileUtils;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MigrationService {

    private final BackupService backupService = new BackupService();

    public static class MigrationResult {
        public final MigrationExecution execution;
        public final List<String> logs;
        public MigrationResult(MigrationExecution execution, List<String> logs) {
            this.execution = execution; this.logs = logs;
        }
    }

    public MigrationResult execute(ProjectMetadata metadata, boolean backupBeforeExecution) throws Exception {
        Path source = Paths.get(metadata.getSourceFolder());
        Path target = Paths.get(metadata.getTargetFolder());
        FileUtils.ensureDirectory(target);

        if (backupBeforeExecution) {
            Path backupZip = metadata.getBackupFolder() != null
                    ? Paths.get(metadata.getBackupFolder()).resolve("project_backup.zip")
                    : target.resolve("backup").resolve("project_backup.zip");
            backupService.backupProject(source, backupZip);
        }

        // Copy source -> target
        FileUtils.copyDirectory(source, target);

        MigrationExecution ex = new MigrationExecution();
        ex.setMigrationId("migration_" + UUID.randomUUID().toString().replace("-", ""));
        ex.setProjectId(metadata.getProjectId());
        ex.setStatus(MigrationExecution.Status.STARTED);
        ex.setStartedAt(Instant.now());

        List<String> logs = new ArrayList<>();
        RuleEngine engine = new RuleEngine();
        Map<String, Object> params = new HashMap<>();
        params.put("targetJava", metadata.getTargetJavaVersion());
        engine.applyAll(target, params, logs);

        ex.setStatus(MigrationExecution.Status.COMPLETED);
        ex.setCompletedAt(Instant.now());
        return new MigrationResult(ex, logs);
    }
}
