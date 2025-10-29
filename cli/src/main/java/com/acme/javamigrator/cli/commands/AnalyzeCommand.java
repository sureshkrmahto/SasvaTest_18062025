package com.acme.javamigrator.cli.commands;

import com.acme.javamigrator.engine.analysis.AnalyzerService;
import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import picocli.CommandLine;

import java.nio.file.Files;
import java.nio.file.Path;

@CommandLine.Command(name = "analyze", description = "Analyze a Java project")
public class AnalyzeCommand implements Runnable {

    @CommandLine.Option(names = {"--source-path","--source"}, required = true, description = "Source project folder")
    String sourcePath;
    @CommandLine.Option(names = {"--target-path","--target"}, required = true, description = "Target output folder")
    String targetPath;
    @CommandLine.Option(names = "--target-java", required = true, description = "Target Java version (9-17)")
    String targetJava;
    @CommandLine.Option(names = "--current-java", defaultValue = "8", description = "Current Java version (8-11)")
    String currentJava;

    @Override
    public void run() {
        try {
            if (!Files.isDirectory(Path.of(sourcePath))) throw new IllegalArgumentException("Source path must exist");
            ProjectMetadata pm = new ProjectMetadata();
            pm.setProjectId("cli-project");
            pm.setSourceFolder(sourcePath);
            pm.setTargetFolder(targetPath);
            pm.setCurrentJavaVersion(currentJava);
            pm.setTargetJavaVersion(targetJava);
            AnalyzerService analyzer = new AnalyzerService();
            AnalysisResult ar = analyzer.analyze(pm);
            System.out.println("Analysis ID: " + ar.getAnalysisId());
            System.out.println("Stats: " + ar.getStatistics());
            System.out.println("Findings: " + ar.getFindings());
        } catch (Exception e) {
            System.err.println("Analyze failed: " + e.getMessage());
            System.exit(2);
        }
    }
}
