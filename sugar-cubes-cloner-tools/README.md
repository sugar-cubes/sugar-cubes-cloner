# Build tools of the cloner

Add copy constructors to your classes at build time: the cloner creates clones by `new`, writes final fields
directly, needs neither `ReflectionFactory` nor `Unsafe` for them. Classes without copy constructors are cloned as
usual, so the tools are optional.

## Which classes

List them by masks in `src/main/resources/META-INF/sugar-cubes-cloner/copy-constructors` (and
`src/test/resources/...` for test classes), one mask per line, `#` starts a comment:

```
com.acme.model.**
!com.acme.model.cache.**
```

`?` is any character but a dot, `*` any characters within a package, `**` any characters including subpackages,
`!` excludes. Interfaces, enums and annotations are ignored. A class gets a copy constructor only if its superclass is
`Object`, `Record` or a class with a copy constructor; other listed classes are reported.

## Three ways, use one or combine

The jar `io.github.sugar-cubes:sugar-cubes-cloner-tools` contains an annotation processor, a Maven plugin and a Gradle
plugin. Enhancing is idempotent: combined with the processor, a plugin finds the classes already enhanced.

* Annotation processor: no build plugin, works in IDE builds (javac only, not Eclipse). Reports problems at the
  sources. Options: `-Asugarcubes.cloner.targets=masks`, `-Asugarcubes.cloner.targetsFile=paths`,
  `-Asugarcubes.cloner.enhance=false` (only check). Without options it reads the masks from the class output, where
  Maven copies resources before compilation.
* Maven plugin: goals `enhance` (`process-classes`) and `enhance-test` (`process-test-classes`), works with any
  compiler.
* Gradle plugin `io.github.sugar-cubes.cloner`: enhances the classes of every source set after `compileJava` and
  passes the masks to the processor if it is on the processor path.

### Maven

```xml
<plugin>
  <groupId>io.github.sugar-cubes</groupId>
  <artifactId>sugar-cubes-cloner-tools</artifactId>
  <version>2.0.0</version>
  <executions>
    <execution>
      <goals>
        <goal>enhance</goal>
      </goals>
    </execution>
  </executions>
</plugin>
```

Processor:

```xml
<plugin>
  <artifactId>maven-compiler-plugin</artifactId>
  <configuration>
    <annotationProcessorPaths>
      <path>
        <groupId>io.github.sugar-cubes</groupId>
        <artifactId>sugar-cubes-cloner-tools</artifactId>
        <version>2.0.0</version>
      </path>
    </annotationProcessorPaths>
  </configuration>
</plugin>
```

### Gradle

```kotlin
buildscript {
    dependencies {
        classpath("io.github.sugar-cubes:sugar-cubes-cloner-tools:2.0.0")
    }
}

apply(plugin = "io.github.sugar-cubes.cloner")

dependencies {
    // optional: enhancing at compilation, also in IntelliJ IDEA builds
    annotationProcessor("io.github.sugar-cubes:sugar-cubes-cloner-tools:2.0.0")
}
```

## The copy constructor

`C(CopyConstructors marker, C original, CopyContext context)`, synthetic, private in final classes and protected in
the others (a serializable class without `serialVersionUID` gets the value computed for the original class, so the
serial form does not change). The cloner uses it when the field policy copies all fields of the class by default;
`Cloners.builder().copyConstructors(false)` disables it.
