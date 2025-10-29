package com.acme.javamigrator.cli.commands;

import picocli.CommandLine;

@CommandLine.Command(name = "report", description = "Generate or print migration report")
public class ReportCommand implements Runnable {
    @CommandLine.Option(names = "--migration-id", required = false)
    String migrationId;

    @Override
    public void run() {
        System.out.println("Reporting is available via the server API. Use --migration-id to reference an execution.");
    }
}
