package com.acme.javamigrator.engine.rules;

import java.nio.file.Path;

public interface Rule {
    String id();
    String description();
    RuleResult apply(RuleContext context) throws Exception;

    interface RuleResult {
        boolean changed();
        String summary();
    }
}
