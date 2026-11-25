# JDK types

The cloner knows how to treat the types of the JDK; your [configuration](configuration.md) overrides it.

## Shared: immutable values

The clone references the same instances: strings, boxed primitives, `BigInteger`, `BigDecimal`, `java.time` values,
`UUID`, `URI`, `URL`, `File`, `Path`, `Locale`, `Currency`, `Charset`, `ZoneId`, `Pattern`, the empty collections of
`Collections`, enums and `Class` objects.

## Shared: identities of the JVM

Copies of these would be other objects, so the clone references the originals: class loaders, modules, reflection
objects (`Method`, `Field`, ...), method handles, `ThreadLocal` and `ClassValue` keys, process handles, `Runtime`.
Lambdas and instances of hidden classes are shared too: they cannot be created by reflection.

## Not copied: live objects

Threads, thread groups, references (`WeakReference` and others, so also `WeakHashMap`), reference queues, cleaners,
processes, file descriptors, file streams, `RandomAccessFile`, sockets, channels, selectors, file locks, timers and
executor services. A clone cannot get its own one, and sharing the original may be wrong, so cloning fails with
`UncopyableObjectException` unless the configuration decides:

```java
Cloner cloner = Cloners.builder()
    .typeAction(Predicates.subclass(Thread.class), CopyAction.ORIGINAL)
    .fieldAction(Job.class, "executor", CopyAction.NULL)
    .build();
```

The exception tells the classes of the objects holding the uncopyable one.

## Copied by their own `clone()`

`Date`, `GregorianCalendar`, `BitSet`, `EnumSet`, `SimpleDateFormat`, `DecimalFormat` and other `java.text` formats.

## Rebuilt collections

`ArrayList`, `LinkedList`, `ArrayDeque`, `Vector`, `Stack`, `HashSet`, `LinkedHashSet`, `HashMap`, `LinkedHashMap`,
`Hashtable`, `ConcurrentHashMap`, `IdentityHashMap`, `EnumMap` and the immutable collections of `List.of()`,
`Set.of()`, `Map.of()` are rebuilt from the copied elements. Hash-based collections are rebuilt after their elements
are copied, when the hash codes of the copied elements are known. Other collections, e.g.
`TreeMap`, are copied by their fields.

## Records

Records are created by their canonical constructors from the copied components, so the checks of the constructor run
for the clone too. A record in a reference cycle which needs itself ready is reported, see
[Modes](modes.md#shared-references-and-cycles).

## Arrays

Arrays of primitives and enums are copied by `clone()`, other arrays element by element.
