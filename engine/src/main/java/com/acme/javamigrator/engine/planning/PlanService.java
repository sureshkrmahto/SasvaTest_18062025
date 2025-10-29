package com.acme.javamigrator.engine.planning;

import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.MigrationPlan;
import com.acme.javamigrator.engine.model.MigrationStep;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public class PlanService {

    public MigrationPlan generatePlan(String projectId, AnalysisResult analysis, String strategy) {
        MigrationPlan plan = new MigrationPlan();
        plan.setPlanId("plan_" + UUID.randomUUID().toString().replace("-", ""));
        plan.setAnalysisId(analysis.getAnalysisId());
        plan.setProjectId(projectId);
        plan.setGeneratedAt(Instant.now());
        plan.setMigrationStrategy(strategy == null ? "incremental" : strategy);

        Map<String, Object> findings = analysis.getFindings();
        boolean spring = Boolean.TRUE.equals(findings.get("spring_detected"));
        plan.getSteps().add(new MigrationStep(MigrationStep.StepType.JAVA_VERSION_UPGRADE, "Update compiler and runtime to target Java"));
        if (spring) {
            plan.getSteps().add(new MigrationStep(MigrationStep.StepType.SPRING_BOOT_UPGRADE, "Upgrade Spring Boot and Spring dependencies to compatible versions"));
        }
        plan.getSteps().add(new MigrationStep(MigrationStep.StepType.DEPENDENCY_UPGRADE, "Upgrade incompatible third-party dependencies"));
        plan.getSteps().add(new MigrationStep(MigrationStep.StepType.CODE_TRANSFORMATION, "Apply code transformation rules (Java + Spring)"));
        return plan;
    }
}
