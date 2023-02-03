plugins {
    java
    id("org.springframework.boot") version "3.0.2"
    id("io.spring.dependency-management") version "1.1.0"
    id("org.graalvm.buildtools.native") version "0.9.18"
}

group = "com.joshlong"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenLocal()
    mavenCentral()
}

val actuatorDependencies = tasks.register("actuatorDependencies", DependencyReportTask::class.java) {
    setConfiguration("runtimeClasspath")
    outputFile = File(project.buildDir.absoluteFile, "resources/main/gradle-classpath")
}

tasks
    .matching { it.name != actuatorDependencies.name }
    .forEach { task: Task ->
        task.dependsOn(actuatorDependencies.name)
    }



dependencies {
    implementation("com.joshlong:dependencies-actuator-spring-boot-starter:" + File("../../version.txt").readText(Charsets.UTF_8).trim())
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
