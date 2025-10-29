package com.acme.javamigrator.engine.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public final class VersionUtils {
    private VersionUtils() {}

    private static final Set<String> SUPPORTED_SOURCE_JAVA = new HashSet<>(Arrays.asList("8","9","10","11"));
    private static final Set<String> SUPPORTED_TARGET_JAVA = new HashSet<>(Arrays.asList("9","10","11","12","13","14","15","16","17"));

    public static void validateJavaVersions(String source, String target) {
        if (!SUPPORTED_SOURCE_JAVA.contains(source)) {
            throw new IllegalArgumentException("Unsupported source Java version: " + source);
        }
        if (!SUPPORTED_TARGET_JAVA.contains(target)) {
            throw new IllegalArgumentException("Unsupported target Java version: " + target);
        }
        if (Integer.parseInt(target) <= Integer.parseInt(source)) {
            throw new IllegalArgumentException("Target Java version must be higher than source version");
        }
    }
}
