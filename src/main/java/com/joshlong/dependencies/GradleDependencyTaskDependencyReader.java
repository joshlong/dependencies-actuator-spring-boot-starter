package com.joshlong.dependencies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * This looks for a file produced by configuring Gradle using the plugin configuration
 * documented in <pre>README.md</pre>.
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
		var ws = " ";
		var manifestDependencies = lines//
				.subList(7, lines.size())//
				.stream()//
				.filter(StringUtils::hasText)//
				.filter(l -> l.contains(":")).map(line -> {
					log.debug("==============================");
					log.debug(line);
					var nc = -1;
					while (true) {
						nc += 1;
						if (!Character.isAlphabetic(line.charAt(nc))) {
							continue;
						}
						var dep = line.substring(nc);
						log.debug("dependency line: " + dep);
						var triad = dep.split(":");
						var groupId = triad[0];
						var artifactId = triad[1];
						var arrow = "->";
						var version = "";
						if (artifactId.contains(arrow)) {
							log.debug("the artifactId for " + artifactId + " contains an arrow ");
							var strings = artifactId.split(arrow);
							artifactId = strings[0].trim();
							version = strings[1].trim();
						} //
						else {
							version = triad[2].trim();
						}

						if (version.contains(ws)) {
							version = version.split(ws)[0];
						}

						var dependency = new Dependency(groupId, artifactId, version);
						log.debug("dependency object: " + dependency);
						return dependency;
					}
				})//
				.toList();
		return new HashSet<>(manifestDependencies);
	}

}
