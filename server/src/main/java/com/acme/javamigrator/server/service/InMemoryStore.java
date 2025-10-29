package com.acme.javamigrator.server.service;

import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.MigrationExecution;
import com.acme.javamigrator.engine.model.MigrationPlan;
import com.acme.javamigrator.engine.model.ProjectMetadata;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryStore {
    public final Map<String, ProjectMetadata> projects = new ConcurrentHashMap<>();
    public final Map<String, AnalysisResult> analyses = new ConcurrentHashMap<>();
    public final Map<String, MigrationPlan> plans = new ConcurrentHashMap<>();
    public final Map<String, MigrationExecution> migrations = new ConcurrentHashMap<>();
    public final Map<String, List<String>> migrationLogs = new ConcurrentHashMap<>();

    private static final InMemoryStore INSTANCE = new InMemoryStore();
    private InMemoryStore() {}
    public static InMemoryStore getInstance() { return INSTANCE; }
}
