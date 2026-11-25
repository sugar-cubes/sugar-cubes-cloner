# Getting started

## The first clone

```java
import io.github.sugarcubes.cloner.Cloners;

Order clone = Cloners.clone(order);
```

`Cloners.clone()` copies the whole graph reachable from the object: every object is copied once, so shared references
stay shared and cycles stay cycles in the clone. Immutable JDK objects (strings, boxed numbers, `java.time` values and
others) are not copied, the clone references the same instances, see [JDK types](jdk-types.md).

No constructors of your classes are called (except copy constructors added by the [build tools](build-tools.md)):
objects are created the way deserialization creates them, and their fields are copied.

## Cloners

A `Cloner` is configured once and reused:

```java
Cloner cloner = Cloners.builder()
    .typeAction(Logger.class, CopyAction.ORIGINAL)
    .build();

Order clone = cloner.clone(order);
```

| Method                          | Cloner                                                                 |
|---------------------------------|------------------------------------------------------------------------|
| `Cloners.defaults()`            | the default cloner, the same as `Cloners.builder().build()`            |
| `Cloners.builder()`             | a builder of a configured cloner, see [Configuration](configuration.md) |
| `Cloners.serialization()`       | clones by Java serialization, for `Serializable` objects only          |
| `Cloners.clone(object)`         | clones with a new default cloner                                       |
| `Cloners.serializationClone(o)` | clones by serialization                                                |

Cloners are thread safe: every call of `clone()` has its own state. Building a cloner probes the JVM and prepares
caches, so keep a configured cloner, for example in a static field or a bean, instead of building one per call.

## Errors

Everything that goes wrong while cloning is a `ClonerException`, an unchecked exception; checked exceptions of
custom copiers are wrapped into it.

`UncopyableObjectException`, a subclass of it, means that the graph contains an object which cannot be copied
meaningfully: a thread, a reference, an open file, a socket, a process, an executor. Decide what the clone gets:

```java
Cloner cloner = Cloners.builder()
    // the clone works with the same executor
    .typeAction(ExecutorService.class, CopyAction.ORIGINAL)
    // the clone has no worker thread
    .fieldAction(Task.class, "worker", CopyAction.NULL)
    .build();
```

See [Troubleshooting](troubleshooting.md) for other messages.

## Java modules

The jar is a multi-release jar with the module `io.github.sugarcubes.cloner`:

```java
module com.acme.app {
    requires io.github.sugarcubes.cloner;
}
```

The default toolset reaches the fields of any class, see [Toolsets](toolsets.md). Classes of your named modules are
cloned faster with generated copiers if their packages are open to the cloner:

```java
module com.acme.model {
    opens com.acme.model to io.github.sugarcubes.cloner;
}
```

Classes on the classpath need nothing.
