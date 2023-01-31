package com.joshlong.dependencies;

import com.joshlong.dependencies.ClasspathSystemPropertyDependencyReader;
import com.joshlong.dependencies.Dependency;
import com.joshlong.dependencies.DependencyReader;
import com.joshlong.dependencies.MavenDependencyPluginDependencyReader;
import com.joshlong.dependencies.actuator.DependencyEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Installs the {@link DependencyEndpoint} and all supporting infrastructure
 *
 * @author Josh Long
 */
@Slf4j
@Configuration
class DependencyEndpointAutoConfiguration {

	private static final Resource MAVEN_CLASSPATH_RESOURCE = new ClassPathResource("/classpath");

	@Bean
	ApplicationRunner dependencyManagementContextConfigurationListener() {
		return args -> {
			if (log.isDebugEnabled())
				log.debug("starting the " + DependencyEndpoint.class.getName());
		};
	}

	@Bean
	DependencyEndpoint dependencyEndpoint(@Qualifier("compositeDependencyReader") DependencyReader dependencyReader) {
		return new DependencyEndpoint(dependencyReader);
	}

	/**
	 * Registers a hint for the Apache Maven-generated file so that it can be discovered
	 * in the context of a GraalVM native image.
	 */
	private static class DependencyRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.resources().registerResource(MAVEN_CLASSPATH_RESOURCE);
		}

	}

	@Bean
	@ImportRuntimeHints(DependencyRuntimeHints.class)
	MavenDependencyPluginDependencyReader mavenDependencyPluginDependencyReader() throws Exception {
		return new MavenDependencyPluginDependencyReader(MAVEN_CLASSPATH_RESOURCE);
	}

	@Bean
	ClasspathSystemPropertyDependencyReader classpathSystemPropertyDependencyReader() {
		return new ClasspathSystemPropertyDependencyReader();
	}

	@Bean
	@Primary
	DependencyReader compositeDependencyReader(Map<String, DependencyReader> readers) {
		log.debug("there are " + readers.size() + " " + DependencyReader.class.getName());
		var set = new ConcurrentSkipListSet<Dependency>(Comparator
				.comparing(dependency -> dependency.groupId() + dependency.version() + dependency.artifactId()));
		readers.values().forEach(dr -> {
			log.info("contributing " + dr.getClass().getName() + '.');
			set.addAll(dr.dependencies());
		});
		return () -> set;
	}

}
