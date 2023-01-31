# A Spring Boot Actuator Endpoint to Enumerate Your Build Dependencies   

A Spring Boot Actuator to enumerate the dependencies that make up the classpath for the running application. 
It'll include information like your `artifactId`, `version`, and `groupId`.

## The `java.class.path` System Property

The module will detect and use the `java.class.path` System property if it exists.

## Apache Maven 

You can configure Apache Maven to enumerate which dependencies it thinks you have on the classpath with the out-of-the-box Apache Maven Dependency plugin.

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <version>3.3.0</version>
    <executions>
        <execution>
            <goals>
                <goal>build-classpath</goal>
                <goal>tree</goal>
            </goals>
            <phase>generate-resources</phase>
        </execution>
    </executions>
    <configuration>
        <attach/>
        <includeScope>compile</includeScope>
        <appendOutput>true</appendOutput>
        <pathSeparator>::</pathSeparator>
        <prependGroupId>true</prependGroupId>
        <outputFile>target/classes/classpath</outputFile>
    </configuration>
</plugin>
```

## Gradle 

You can use the following incantation in a Gradle build to enumerate all the dependencies on the classpath in a Gradle application, as well: 

```groovy 

```