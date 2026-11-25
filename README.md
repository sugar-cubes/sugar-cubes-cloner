# Sugar Cubes Cloner

[![build](https://github.com/sugar-cubes/sugar-cubes-cloner/actions/workflows/build.yml/badge.svg)](https://github.com/sugar-cubes/sugar-cubes-cloner/actions/workflows/build.yml)
[![maven central](https://img.shields.io/maven-central/v/io.github.sugar-cubes/sugar-cubes-cloner)](https://central.sonatype.com/artifact/io.github.sugar-cubes/sugar-cubes-cloner)

Deep cloning of any Java objects: object graphs with shared references and cycles, JDK collections, records, your own
classes without any code in them. Fast, configurable, thread safe, Java 8 to 26.

```java
Order clone = Cloners.clone(order);
```

```java
Cloner cloner = Cloners.builder()
    .typeAction(Predicates.subclass(Connection.class), CopyAction.ORIGINAL)
    .fieldAction(Order.class, "cachedTotal", CopyAction.SKIP)
    .build();

Order clone = cloner.clone(order);
```

## Installation

Maven:

```xml
<dependency>
    <groupId>io.github.sugar-cubes</groupId>
    <artifactId>sugar-cubes-cloner</artifactId>
    <version>2.0.0</version>
</dependency>
```

Gradle:

```kotlin
implementation("io.github.sugar-cubes:sugar-cubes-cloner:2.0.0")
```

## Documentation

The guide and the API reference: [sugar-cubes.github.io/sugar-cubes-cloner](https://sugar-cubes.github.io/sugar-cubes-cloner/).

- [Changelog](CHANGELOG.md)
- [Contributing](CONTRIBUTING.md)
- [Build tools](sugar-cubes-cloner-tools/README.md): copy constructors by an annotation processor, a Maven or a Gradle
  plugin

## License

[Apache License 2.0](LICENSE.txt)
