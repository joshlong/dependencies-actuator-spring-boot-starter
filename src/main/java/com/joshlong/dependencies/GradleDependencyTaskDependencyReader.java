package com.joshlong.dependencies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.HashSet;
import java.util.Set;

/**
 * Supports ingesting the output of a Gradle task such as documented in the
 * {@code README.md}
 *
 * @author Josh Long
 */
@Slf4j
class GradleDependencyTaskDependencyReader extends AbstractBuildPluginDependencyReportDependencyReader
		implements DependencyReader {

	GradleDependencyTaskDependencyReader(Resource classpath) throws Exception {
		super(classpath);
	}

	@Override
	protected Set<Dependency> parseDependencies(String manifestString) {
		var lines = manifestString.lines().toList();
		var manifestDependencies = lines//
				.subList(7, lines.size())//
				.stream()//
				.map(line -> {
					var nc = -1;
					while (true) {
						nc += 1;
						if (!Character.isAlphabetic(line.charAt(nc))) {
							continue;
						}
						var dep = line.substring(nc);
						System.out.println("dep: " + dep);
						var triad = dep.split(":");
						var groupId = triad[0];
						var artifactId = triad[1];
						var version = triad[2];
						if (version.indexOf(' ') != -1)
							version = version.split(" ")[0];
						return new Dependency(groupId, artifactId, version);
					}
				})//
				.toList();
		return new HashSet<>(manifestDependencies);
	}

}
