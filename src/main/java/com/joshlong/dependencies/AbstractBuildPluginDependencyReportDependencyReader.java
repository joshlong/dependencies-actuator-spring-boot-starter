package com.joshlong.dependencies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Base implementation for the Apache Maven and Gradle {@link DependencyReader
 * dependencyReader} implementations.
 *
 * @author Josh Long
 */
@Slf4j
abstract class AbstractBuildPluginDependencyReportDependencyReader implements DependencyReader {

	private final Set<Dependency> dependencies = new ConcurrentSkipListSet<>(
			Comparator.comparing(o -> (o.artifactId() + o.groupId() + o.version())));

	AbstractBuildPluginDependencyReportDependencyReader(Resource classpath) throws Exception {
		log.debug("the classpath Resource exists? " + classpath.exists());
		if (!classpath.exists())
			return;
		try (var in = classpath.getInputStream()) {
			var bytes = in.readAllBytes();
			this.dependencies.addAll(parseDependencies(new String(bytes)));
		}
	}

	/**
	 *
	 */
	protected abstract Set<Dependency> parseDependencies(String report);

	@Override
	public Set<Dependency> dependencies() {
		return this.dependencies;
	}

}
