package com.acme.javamigrator.engine.rules;

import java.nio.file.Path;
import java.util.Map;

public class RuleContext {
    private final Path projectRoot;
    private final Path targetRoot;
    private final Map<String, Object> parameters;

    public RuleContext(Path projectRoot, Path targetRoot, Map<String, Object> parameters) {
        this.projectRoot = projectRoot;
        this.targetRoot = targetRoot;
        this.parameters = parameters;
    }

    public Path getProjectRoot() { return projectRoot; }
    public Path getTargetRoot() { return targetRoot; }
    public Map<String, Object> getParameters() { return parameters; }
}
