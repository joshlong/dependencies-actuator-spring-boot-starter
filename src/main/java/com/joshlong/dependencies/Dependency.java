package com.joshlong.dependencies;

/**
 * Represents the coordinates for a Maven or Gradle-resolved dependencies.
 * @param groupId
 * @param artifactId
 * @param version
 */
public record Dependency(String groupId, String artifactId, String version) {
}
