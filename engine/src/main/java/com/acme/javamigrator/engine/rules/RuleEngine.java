package com.acme.javamigrator.engine.rules;

import com.acme.javamigrator.engine.rules.impl.HttpClientMigrationRule;
import com.acme.javamigrator.engine.rules.impl.SpringPropertyMigrationRule;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RuleEngine {

    private final List<Rule> rules = new ArrayList<>();

    public RuleEngine() {
        // Default rules; can be extended via config
        rules.add(new HttpClientMigrationRule());
        rules.add(new SpringPropertyMigrationRule());
    }

    public void applyAll(Path targetRoot, Map<String, Object> params, List<String> logs) throws Exception {
        RuleContext ctx = new RuleContext(targetRoot, targetRoot, params == null ? new HashMap<>() : params);
        for (Rule rule : rules) {
            Rule.RuleResult result = rule.apply(ctx);
            logs.add(rule.id() + ": " + result.summary());
        }
    }
}
