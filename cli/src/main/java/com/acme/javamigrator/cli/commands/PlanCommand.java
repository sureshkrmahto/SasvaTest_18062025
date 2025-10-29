package com.acme.javamigrator.cli.commands;

import com.acme.javamigrator.engine.analysis.AnalyzerService;
import com.acme.javamigrator.engine.model.AnalysisResult;
import com.acme.javamigrator.engine.model.MigrationPlan;
import com.acme.javamigrator.engine.planning.PlanService;
import com.acme.javamigrator.engine.model.ProjectMetadata;
import picocli.CommandLine;

@CommandLine.Command(name = "plan", description = "Generate migration plan")
public class PlanCommand implements Runnable {
    @CommandLine.Option(names = {"--source","--source-path"}, required = true)
    String source;
    @CommandLine.Option(names = {"--target","--target-path"}, required = true)
    String target;
    @CommandLine.Option(names = "--target-java", required = true)
    String targetJava;
    @CommandLine.Option(names = "--current-java", defaultValue = "8")
    String currentJava;

    @Override
    public void run() {
        try {
            ProjectMetadata pm = new ProjectMetadata();
            pm.setProjectId("cli-project");
            pm.setSourceFolder(source);
            pm.setTargetFolder(target);
            pm.setCurrentJavaVersion(currentJava);
            pm.setTargetJavaVersion(targetJava);
            AnalyzerService analyzer = new AnalyzerService();
            AnalysisResult ar = analyzer.analyze(pm);
            PlanService planner = new PlanService();
            MigrationPlan plan = planner.generatePlan(pm.getProjectId(), ar, "incremental");
            System.out.println("Plan ID: " + plan.getPlanId());
            System.out.println("Steps: " + plan.getSteps().size());
        } catch (Exception e) {
            System.err.println("Plan failed: " + e.getMessage());
            System.exit(2);
        }
    }
}
