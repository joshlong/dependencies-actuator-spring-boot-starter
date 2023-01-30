package com.joshlong.dependencies;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Set;

@Endpoint(id = "dependencies")
public class DependencyEndpoint {

	private final DependencyReader dependencyReader;

	public DependencyEndpoint(DependencyReader dependencyReader) {
		this.dependencyReader = dependencyReader;
	}

	@ReadOperation
	Set<Dependency> dependencies() {
		return this.dependencyReader.dependencies();
	}

}
