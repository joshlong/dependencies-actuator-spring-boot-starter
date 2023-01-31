package com.joshlong.dependencies;

import com.joshlong.dependencies.actuator.DependencyEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
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

	public static final String MAVEN_CLASSPATH_RESOURCE_STRING = "/maven-classpath";

	public static final Resource MAVEN_CLASSPATH_RESOURCE = new ClassPathResource(MAVEN_CLASSPATH_RESOURCE_STRING);

	public static final String GRADLE_CLASSPATH_RESOURCE_STRING = "/gradle-classpath";

	public static final Resource GRADLE_CLASSPATH_RESOURCE = new ClassPathResource(GRADLE_CLASSPATH_RESOURCE_STRING);

	@Bean
	ApplicationRunner dependencyManagementContextConfigurationListener() {
		return args -> log.debug("starting the " + DependencyEndpoint.class.getName());
	}

	@Bean
	DependencyEndpoint dependencyEndpoint(@Qualifier("compositeDependencyReader") DependencyReader dependencyReader) {
		return new DependencyEndpoint(dependencyReader);
	}

	static class MavenRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.resources().registerResource(MAVEN_CLASSPATH_RESOURCE);
		}

	}

	static class GradleRuntimeHints implements RuntimeHintsRegistrar {

		@Override
		public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
			hints.resources().registerResource(GRADLE_CLASSPATH_RESOURCE);
		}

	}

	@Bean
	@ConditionalOnResource(resources = GRADLE_CLASSPATH_RESOURCE_STRING)
	@ImportRuntimeHints(GradleRuntimeHints.class)
	GradleDependencyTaskDependencyReader gradleDependencyTaskDependencyReader() throws Exception {
		return new GradleDependencyTaskDependencyReader(GRADLE_CLASSPATH_RESOURCE);
	}

	@Bean
	@ConditionalOnResource(resources = MAVEN_CLASSPATH_RESOURCE_STRING)
	@ImportRuntimeHints(MavenRuntimeHints.class)
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
		log.info("there are " + readers.size() + " " + DependencyReader.class.getName());
		var set = new ConcurrentSkipListSet<Dependency>(Comparator
				.comparing(dependency -> dependency.groupId() + dependency.version() + dependency.artifactId()));
		readers.forEach((beanName, dr) -> {
			log.info("\tcontributing " + beanName + " with class name " + dr.getClass().getName() + '.');
			set.addAll(dr.dependencies());
		});
		return () -> set;
	}

}
