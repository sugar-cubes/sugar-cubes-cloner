# Sugar Cubes Cloner

Deep cloning of any Java objects: object graphs with shared references and cycles, JDK collections, records, your
own classes without any code in them.

```java
Order clone = Cloners.clone(order);
```

- **Fast.** Copiers are generated at runtime for your classes; copy constructors can be added at build time.
- **Configurable.** Choose per type, field or object whether a value is cloned, shared or replaced with `null`; plug
  your own copiers.
- **Safe by default.** Threads, open files, sockets and the like are not copied silently: the cloner asks you what the
  clone gets.
- **Sequential or parallel.** One thread without recursion, or a thread pool for large graphs.
- **Java 8 to 26.** One multi-release jar, the module `io.github.sugarcubes.cloner`, tested on HotSpot and OpenJ9.

## Installation

=== "Maven"

    ```xml
    <dependency>
        <groupId>io.github.sugar-cubes</groupId>
        <artifactId>sugar-cubes-cloner</artifactId>
        <version>2.0.0</version>
    </dependency>
    ```

=== "Gradle"

    ```kotlin
    implementation("io.github.sugar-cubes:sugar-cubes-cloner:2.0.0")
    ```

The library has no required dependencies.

## Where next

- [Getting started](getting-started.md): the first clone, cloners, errors.
- [Configuration](configuration.md): copy actions, policies, annotations, custom copiers.
- [Modes](modes.md): sequential, recursive and parallel cloning.
- [JDK types](jdk-types.md): what happens to strings, collections, records, threads.
- [Toolsets](toolsets.md): how objects are created and fields accessed, JVM options.
- [Performance](performance.md): generated copiers and copy constructors.
- [Build tools](build-tools.md): copy constructors by an annotation processor, a Maven or a Gradle plugin.
- [Troubleshooting](troubleshooting.md): common exceptions and warnings.
- [API reference](api/): the Javadoc.
