package com.joshlong.dependencies;


import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.actuate.autoconfigure.web.ManagementContextConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListSet;

/**
 * Installs the {@link DependencyEndpoint}
 *
 * @author Josh Long
 */
@Slf4j
@ManagementContextConfiguration(
        proxyBeanMethods = false
)
class DependencyReaderManagementContextAutoConfiguration {

    @Bean
    ApplicationRunner dependencyManagementContextConfigurationListener() {
        return args -> {
            if (log.isDebugEnabled())
                log.debug("starting the " + DependencyEndpoint.class.getName());
        };
    }

    @Bean
    DependencyEndpoint dependencyEndpoint(DependencyReader dependencyReader) {
        return new DependencyEndpoint(dependencyReader);
    }

    @Bean
    MavenDependencyPluginDependencyReader mavenDependencyPluginDependencyReader() throws Exception {
        return new MavenDependencyPluginDependencyReader();
    }

    @Bean
    ClasspathSystemPropertyDependencyReader classpathSystemPropertyDependencyReader() {
        return new ClasspathSystemPropertyDependencyReader();
    }

    @Bean
    @Primary
    DependencyReader compositeDependencyReader(Map<String, DependencyReader> readers) {
        var set = new ConcurrentSkipListSet<Dependency>(
                Comparator.comparing(dependency -> dependency.groupId() + dependency.artifactId() + dependency.version()));
        readers.values().forEach(dr -> set.addAll(dr.dependencies()));
        return () -> set;
    }
}
