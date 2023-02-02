package com.joshlong.dependencies;

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
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Installs the {@link DependencyEndpoint} Spring Boot Actuator endpoint and all
 * supporting infrastructure
 *
 * @author Josh Long
 */
@Slf4j
@Configuration
@ImportRuntimeHints(DependencyEndpointAutoConfiguration.ClasspathFilesRuntimeHints.class)
class DependencyEndpointAutoConfiguration {

	private static final Resource MAVEN_CLASSPATH_RESOURCE = new ClassPathResource("/maven-classpath");

	private static final Resource GRADLE_CLASSPATH_RESOURCE = new ClassPathResource("/gradle-classpath");

	@Bean
	ApplicationRunner dependencyManagementContextConfigurationListener() {
		return args -> log.debug("starting the " + DependencyEndpoint.class.getName());
	}

	@Bean
	DependencyEndpoint dependencyEndpoint(@Qualifier("compositeDependencyReader") DependencyReader dependencyReader) {
		return new DependencyEndpoint(dependencyReader);
	}

	static class ClasspathFilesRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			Set.of(GRADLE_CLASSPATH_RESOURCE, MAVEN_CLASSPATH_RESOURCE)//
					.stream()//
					.filter(Resource::exists)//
					.forEach(r -> hints.resources().registerResource(r));
		}

	}

	@Bean
	GradleDependencyTaskDependencyReader gradleDependencyTaskDependencyReader() throws Exception {
		return new GradleDependencyTaskDependencyReader(GRADLE_CLASSPATH_RESOURCE);
	}

	@Bean
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
		readers.forEach((beanName, dr) -> {
			log.debug("\tcontributing {} with class name {}.", beanName, dr.getClass().getName());
			set.addAll(dr.dependencies());
		});
		return () -> set;
	}

}
