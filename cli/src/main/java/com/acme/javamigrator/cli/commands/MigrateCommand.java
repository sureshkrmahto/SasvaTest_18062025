package com.acme.javamigrator.cli.commands;

import com.acme.javamigrator.engine.migration.MigrationService;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import picocli.CommandLine;

@CommandLine.Command(name = "migrate", description = "Execute migration")
public class MigrateCommand implements Runnable {
    @CommandLine.Option(names = {"--source","--source-path"}, required = true)
    String source;
    @CommandLine.Option(names = {"--target","--target-path"}, required = true)
    String target;
    @CommandLine.Option(names = "--backup", defaultValue = "true")
    boolean backup;
    @CommandLine.Option(names = "--current-java", defaultValue = "8")
    String currentJava;
    @CommandLine.Option(names = "--target-java", required = true)
    String targetJava;

    @Override
    public void run() {
        try {
            ProjectMetadata pm = new ProjectMetadata();
            pm.setProjectId("cli-project");
            pm.setSourceFolder(source);
            pm.setTargetFolder(target);
            pm.setCurrentJavaVersion(currentJava);
            pm.setTargetJavaVersion(targetJava);
            MigrationService ms = new MigrationService();
            var result = ms.execute(pm, backup);
            System.out.println("Migration ID: " + result.execution.getMigrationId());
            System.out.println("Status: " + result.execution.getStatus());
            result.logs.forEach(System.out::println);
        } catch (Exception e) {
            System.err.println("Migration failed: " + e.getMessage());
            System.exit(2);
        }
    }
}
