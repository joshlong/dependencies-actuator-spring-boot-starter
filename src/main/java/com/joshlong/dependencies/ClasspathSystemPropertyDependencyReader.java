package com.joshlong.dependencies;

import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.*;

@Slf4j
class ClasspathSystemPropertyDependencyReader implements DependencyReader {

	private final Set<Dependency> dependencies = new HashSet<>();

	ClasspathSystemPropertyDependencyReader() {

		var systemPropertyKey = "java.class.path";

		var property = System.getProperty(systemPropertyKey);

		if (!StringUtils.hasText(property))
			return;

		var deps = Arrays.stream(property.split(":")).filter(fn -> fn.endsWith(".jar")).map(this::mapRepositoryLine)
				.toList();
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
