package com.acme.javamigrator.cli;

import com.acme.javamigrator.cli.commands.AnalyzeCommand;
import com.acme.javamigrator.cli.commands.PlanCommand;
import com.acme.javamigrator.cli.commands.MigrateCommand;
import com.acme.javamigrator.cli.commands.ReportCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "java-migrator", mixinStandardHelpOptions = true, version = "0.1.0",
        subcommands = {AnalyzeCommand.class, PlanCommand.class, MigrateCommand.class, ReportCommand.class})
public class JavaMigratorCli implements Runnable {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new JavaMigratorCli()).execute(args);
        System.exit(exitCode);
    }
    @Override
    public void run() { new CommandLine(this).usage(System.out); }
}
