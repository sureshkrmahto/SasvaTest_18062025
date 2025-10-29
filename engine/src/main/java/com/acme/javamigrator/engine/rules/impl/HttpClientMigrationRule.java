package com.acme.javamigrator.engine.rules.impl;

import com.acme.javamigrator.engine.rules.Rule;
import com.acme.javamigrator.engine.rules.RuleContext;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class HttpClientMigrationRule implements Rule {
    @Override
    public String id() { return "java8-11-httpurlconnection-to-httpclient"; }

    @Override
    public String description() {
        return "Replace legacy HttpURLConnection usage with Java 11 HttpClient minimal patterns";
    }

    @Override
    public RuleResult apply(RuleContext context) throws IOException {
        Path root = context.getTargetRoot();
        AtomicInteger filesChanged = new AtomicInteger();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> Files.isRegularFile(p) && p.toString().endsWith(".java")).forEach(p -> {
                try {
                    String source = Files.readString(p);
                    if (source.contains("HttpURLConnection") || source.contains("java.net.HttpURLConnection")) {
                        String transformed = transform(source);
                        if (!transformed.equals(source)) {
                            Files.writeString(p, transformed, StandardCharsets.UTF_8);
                            filesChanged.incrementAndGet();
                        }
                    }
                } catch (IOException ignored) {}
            });
        }
        int count = filesChanged.get();
        return new RuleResult() {
            @Override public boolean changed() { return count > 0; }
            @Override public String summary() { return count + " Java files updated for HttpClient"; }
        };
    }

    private String transform(String source) {
        String updated = source;
        // Basic import adjustments
        updated = updated.replace("import java.net.HttpURLConnection;", "import java.net.http.HttpClient;\nimport java.net.http.HttpRequest;\nimport java.net.http.HttpResponse;\nimport java.net.URI;");
        // Replace type mentions conservatively (may need manual fix in complex cases)
        updated = updated.replace("HttpURLConnection", "/* migrated */ HttpClient");
        // Replace openConnection call sites with a basic template
        updated = updated.replaceAll("(\\w+)\\s*=\\s*\\(HttpURLConnection\\)\\s*(\\w+)\\.openConnection\\(\\);",
                "HttpClient $1 = HttpClient.newHttpClient(); // migrated from $2.openConnection()\nHttpRequest request = HttpRequest.newBuilder().uri(URI.create($2.toString())).build();\nHttpResponse<String> response = $1.send(request, HttpResponse.BodyHandlers.ofString());");
        return updated;
    }
}
