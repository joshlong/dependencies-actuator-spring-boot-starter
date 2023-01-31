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

	/*
	 * private final Resource classpath;
	 *
	 * private final Set<Dependency> dependencies = new ConcurrentSkipListSet<>(
	 * Comparator.comparing(o -> (o.artifactId() + o.groupId() + o.version())));
	 *
	 * GradleDependencyTaskDependencyReader(Resource resource) throws Exception {
	 * this.classpath = resource; log.info("the classpath exists " +
	 * this.classpath.exists()); if (!classpath.exists()) { return; } try (var in =
	 * classpath.getInputStream()) { var bytes = in.readAllBytes();
	 * this.dependencies.addAll(dependencies(new String(bytes))); } }
	 *
	 * private Set<Dependency> dependencies(String manifestString) { var lines =
	 * manifestString.lines().toList(); var manifestDependencies = lines// .subList(7,
	 * lines.size())// .stream()// .map(line -> { var nc = -1; while (true) { nc += 1; if
	 * (!Character.isAlphabetic(line.charAt(nc))) { continue; } var dep =
	 * line.substring(nc); System.out.println("dep: " +dep); var triad = dep.split(":");
	 * var groupId = triad[0]; var artifactId = triad[1]; var version = triad[2]; if
	 * (version.indexOf(' ')!=-1) version = version.split (" ")[0]; return new
	 * Dependency(groupId, artifactId, version); } })// .toList(); return new
	 * HashSet<>(manifestDependencies); }
	 *
	 * @Override public Set<Dependency> dependencies() { return this.dependencies; }
	 *
	 * public static void main(String args[]) throws Exception { var resource = new
	 * FileSystemResource(new File(
	 * "/Users/jlong/Downloads/dependencies-gradle-demo/build/resources/main/gradle-classpath"
	 * )); var dependencyReader = new GradleDependencyTaskDependencyReader(resource); var
	 * deps = dependencyReader.dependencies(); Assert.state(deps.size() > 0,
	 * "there should be more than 0 dependencies"); for (var d : deps)
	 * System.out.println("d " + d.toString()); }
	 */

}
