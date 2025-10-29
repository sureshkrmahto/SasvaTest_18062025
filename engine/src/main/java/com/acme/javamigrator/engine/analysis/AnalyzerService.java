package com.acme.javamigrator.engine.analysis;

import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import com.acme.javamigrator.engine.util.VersionUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class AnalyzerService {

    public AnalysisResult analyze(ProjectMetadata metadata) throws IOException {
        if (metadata.getSourceFolder() == null || metadata.getTargetFolder() == null) {
            throw new IllegalArgumentException("source_folder and target_folder are required");
        }
        VersionUtils.validateJavaVersions(metadata.getCurrentJavaVersion(), metadata.getTargetJavaVersion());

        Path source = Paths.get(metadata.getSourceFolder());
        if (!Files.isDirectory(source)) {
            throw new IllegalArgumentException("Source folder does not exist: " + source);
        }

        AnalysisResult result = new AnalysisResult();
        result.setAnalysisId("analysis_" + UUID.randomUUID().toString().replace("-",""));
        result.setProjectId(metadata.getProjectId());
        result.setStartedAt(Instant.now());

        AtomicInteger totalFiles = new AtomicInteger();
        AtomicInteger javaFiles = new AtomicInteger();
        AtomicInteger testFiles = new AtomicInteger();
        AtomicInteger configFiles = new AtomicInteger();
        AtomicInteger totalLines = new AtomicInteger();
        AtomicInteger javaLines = new AtomicInteger();

        try (Stream<Path> paths = Files.walk(source)) {
            paths.forEach(p -> {
                if (Files.isRegularFile(p)) {
                    totalFiles.incrementAndGet();
                    String fn = p.getFileName().toString();
                    if (fn.endsWith(".java")) {
                        javaFiles.incrementAndGet();
                        try {
                            javaLines.addAndGet(Files.readAllLines(p, StandardCharsets.UTF_8).size());
                        } catch (IOException ignored) {}
                    } else if (fn.equals("pom.xml") || fn.equals("build.gradle") || fn.endsWith(".properties") || fn.endsWith(".yml")) {
                        configFiles.incrementAndGet();
                    }
                    try {
                        totalLines.addAndGet(Files.readAllLines(p, StandardCharsets.UTF_8).size());
                    } catch (IOException ignored) {}
                }
            });
        }

        Map<String, Object> stats = result.getStatistics();
        stats.put("file_count", totalFiles.get());
        stats.put("java_files", javaFiles.get());
        stats.put("test_files", testFiles.get()); // not computed deeply in this MVP
        stats.put("config_files", configFiles.get());
        stats.put("total_lines", totalLines.get());
        stats.put("java_lines", javaLines.get());

        Map<String, Object> findings = result.getFindings();
        findings.put("build_tool", detectBuildTool(source));
        findings.put("spring_detected", detectSpringPresence(source));

        result.setCompletedAt(Instant.now());
        return result;
    }

    private String detectBuildTool(Path source) {
        if (Files.exists(source.resolve("pom.xml"))) return "maven";
        if (Files.exists(source.resolve("build.gradle"))) return "gradle";
        return "unknown";
    }

    private boolean detectSpringPresence(Path source) {
        try (Stream<Path> paths = Files.walk(source)) {
            return paths.anyMatch(p -> {
                String fn = p.getFileName().toString();
                if (fn.endsWith(".java")) {
                    try {
                        String content = Files.readString(p);
                        return content.contains("@SpringBootApplication") || content.contains("org.springframework");
                    } catch (IOException ignored) {}
                }
                return false;
            });
        } catch (IOException e) {
            return false;
        }
    }
}
