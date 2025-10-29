package com.acme.javamigrator.engine.rules.impl;

import com.acme.javamigrator.engine.rules.Rule;
import com.acme.javamigrator.engine.rules.RuleContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpringPropertyMigrationRule implements Rule {

    @Override
    public String id() { return "spring-boot-property-mappings-2x-to-3x"; }

    @Override
    public String description() { return "Rename known Spring Boot properties per mapping table"; }

    @Override
    public RuleResult apply(RuleContext context) throws Exception {
        Mapping mapping = loadMapping();
        Path root = context.getTargetRoot();
        AtomicInteger filesChanged = new AtomicInteger();
        try (Stream<Path> paths = Files.walk(root)) {
            paths.filter(p -> Files.isRegularFile(p) && (p.getFileName().toString().equals("application.properties")))
                 .forEach(p -> {
                    try {
                        String content = Files.readString(p, StandardCharsets.UTF_8);
                        String updated = migrateProperties(content, mapping);
                        if (!updated.equals(content)) {
                            Files.writeString(p, updated, StandardCharsets.UTF_8);
                            filesChanged.incrementAndGet();
                        }
                    } catch (IOException ignored) {}
                 });
        }
        int count = filesChanged.get();
        return new RuleResult() {
            @Override public boolean changed() { return count > 0; }
            @Override public String summary() { return count + " config files updated (application.properties)"; }
        };
    }

    private String migrateProperties(String content, Mapping mapping) {
        StringBuilder out = new StringBuilder();
        for (String line : content.split("\n", -1)) {
            String trimmed = line.trim();
            if (trimmed.startsWith("#") || !trimmed.contains("=")) {
                out.append(line).append('\n');
                continue;
            }
            int idx = line.indexOf('=');
            String key = line.substring(0, idx).trim();
            String value = line.substring(idx + 1);
            if (mapping.remove.containsKey(key)) {
                // skip this property (removed)
                continue;
            }
            String newKey = mapping.mappings.getOrDefault(key, key);
            out.append(newKey).append('=').append(value).append('\n');
        }
        return out.toString();
    }

    private Mapping loadMapping() throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        try (InputStream is = SpringPropertyMigrationRule.class.getResourceAsStream("/rules/spring-boot-properties-mapping.yml")) {
            if (is == null) {
                Mapping m = new Mapping();
                m.mappings = new HashMap<>();
                m.remove = new HashMap<>();
                return m;
            }
            return mapper.readValue(is, Mapping.class);
        }
    }

    public static class Mapping {
        public Map<String, String> mappings = new HashMap<>();
        public Map<String, Boolean> remove = new HashMap<>();
    }
}
