package bootiful.actuatorsupplychain;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

@SpringBootApplication
@Slf4j
public class ActuatorSupplychainApplication {

    public static void main(String[] args) {
        SpringApplication.run(ActuatorSupplychainApplication.class, args);
    }
}


@Endpoint(id = "dependencies")
class DependencyEndpoint {

    private final DependencyReader dependencyReader;

    DependencyEndpoint(DependencyReader dependencyReader) {
        this.dependencyReader = dependencyReader;
    }

    @ReadOperation
    Set<Dependency> dependencies() {
        return this.dependencyReader.dependencies();
    }
}

record Dependency(String groupId, String artifactId, String version) {
}

interface DependencyReader {
    Set<Dependency> dependencies();
}

@Configuration
class DependencyManifestReaderAutoConfiguration {

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

@Slf4j
class ClasspathSystemPropertyDependencyReader implements DependencyReader {

    private final Set<Dependency> dependencies = new HashSet<>();

    ClasspathSystemPropertyDependencyReader() {
        var systemPropertyKey = "java.class.path";
        var property = System.getProperty(systemPropertyKey);

        if (!StringUtils.hasText(property)) return;

        var deps = Arrays.stream(property.split(":")).filter(fn -> fn.endsWith(".jar")).map(this::mapRepositoryLine).toList();
        var out = new HashSet<Dependency>();
        for (var depString : deps) {
            var fn = depString.split("/");
            var jar = fn[fn.length - 1];
            var version = fn[fn.length - 2];
            var artifactId = fn[fn.length - 3];
            // groupId is everything up to fn.length -3
            var collect = new ArrayList<>(Arrays.asList(fn).subList(0, fn.length - 3));
            var groupId = String.join(".", collect);
            if (log.isDebugEnabled())
                log.debug("" + Map.of("groupId", groupId, "artifactId", artifactId, "version", version, "jar", jar));
            out.add(new Dependency(groupId, artifactId, version));
        }
        this.dependencies.addAll(out);
    }

    @Override
    public Set<Dependency> dependencies() {
        return this.dependencies;
    }

    private String mapRepositoryLine(String line) {
        var repositoryToken = "repository/";
        var index = line.indexOf(repositoryToken) + repositoryToken.length();
        return line.substring(index);
    }

}

@Slf4j
class MavenDependencyPluginDependencyReader implements DependencyReader {

    private final Set<Dependency> dependencies = new ConcurrentSkipListSet<>(Comparator.comparing(o -> (o.artifactId() + o.groupId() + o.version())));

    MavenDependencyPluginDependencyReader() throws Exception {
        var manifestString = "";
        var classpath = new ClassPathResource("/META-INF/classpath");
        if (classpath.exists()) {
            try (var in = classpath.getInputStream()) {
                var bytes = in.readAllBytes();
                manifestString = new String(bytes);
            }
        }
        this.dependencies.addAll(dependencies(manifestString));
    }

    private Set<Dependency> dependencies(String manifestString) {
        // there are two parts to the Maven manifest: the classpath and the dependencies tree
        // the classpath is a list of jar files that are on the classpath.
        // the dependency tree is all the dependencies that are used by the application.
        var lines = manifestString.lines().toList();
        var cp = lines.get(0).split("::");
        var manifestDependencies = lines.subList(1, lines.size()).stream().map(line -> {
                    var nc = -1;
                    while (true) {
                        nc += 1;
                        if (!Character.isAlphabetic(line.charAt(nc))) {
                            continue;
                        }
                        var dep = line.substring(nc);
                        var triad = dep.split(":");
                        var groupId = triad[0];
                        var artifactId = triad[1];
                        var version = triad[3];
                        return new Dependency(groupId, artifactId, version);
                    }
                })//
                .toList();
        return new HashSet<>(manifestDependencies)//
                .stream()//
                .filter(dependency -> Arrays//
                        .stream(cp)//
                        .anyMatch(fileName -> {
                            if (fileName.contains(dependency.version())) {
                                if (fileName.contains(dependency.artifactId())) {
                                    var gid = dependency.groupId();
                                    var gidAlternate = dependency.groupId().replaceAll("\\.", "/");
                                    return fileName.contains(gid) || fileName.contains(gidAlternate);
                                }
                            }
                            return false;
                        })).collect(Collectors.toSet());

    }

    @Override
    public Set<Dependency> dependencies() {
        return this.dependencies;
    }
}