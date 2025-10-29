package com.acme.javamigrator.engine.rules;

import com.acme.javamigrator.engine.rules.impl.HttpClientMigrationRule;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class HttpClientMigrationRuleTest {

    @Test
    void transformsWhenHttpURLConnectionPresent() throws Exception {
        Path tempDir = Files.createTempDirectory("rule-test");
        Path file = tempDir.resolve("Sample.java");
        String source = "import java.net.HttpURLConnection;\nclass X { void a(java.net.URL url) throws Exception { HttpURLConnection c = (HttpURLConnection) url.openConnection(); } }";
        Files.writeString(file, source);
        HttpClientMigrationRule rule = new HttpClientMigrationRule();
        RuleContext ctx = new RuleContext(tempDir, tempDir, Map.of());
        Rule.RuleResult result = rule.apply(ctx);
        assertTrue(result.changed());
        String updated = Files.readString(file);
        assertTrue(updated.contains("HttpClient.newHttpClient()"));
    }
}
