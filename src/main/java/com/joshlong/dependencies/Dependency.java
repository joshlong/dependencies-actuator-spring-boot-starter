package com.joshlong.dependencies;

/**
 * Represents the coordinates for a Maven or Gradle-resolved dependencies.
 * @param groupId the groupId of the dependency
 * @param artifactId the artifactId of the dependency
 * @param version the version of the dependency
 */
public record Dependency(String groupId, String artifactId, String version) {
}
