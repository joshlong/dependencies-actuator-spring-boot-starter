package com.joshlong.dependencies;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;

import java.util.Set;

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
