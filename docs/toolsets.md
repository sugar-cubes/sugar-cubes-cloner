# Toolsets

A toolset holds the low-level mechanisms of a cloner: how instances are created without constructors, how fields are
read and written, how the packages of the JDK are opened to the cloner. The defaults suit most applications; this
page is for the cases when the JVM restricts them.

```java
Cloner cloner = Cloners.builder()
    .toolset(Toolsets.conservative())
    .build();
```

## The default toolset

`Toolsets.defaults()` probes the JVM once and takes the fastest mechanisms it allows:

| Mechanism      | Choice, in the order of preference                                                          |
|----------------|---------------------------------------------------------------------------------------------|
| Field access   | `Unsafe`, unless its memory access is denied; reflection; `VarHandle` if `Unsafe` is denied and `java.lang.invoke` is open |
| Allocation     | `Unsafe.allocateInstance()`; `sun.reflect.ReflectionFactory`; constructors as the last fallback |
| Module opening | the cloner agent if started; `Unsafe`, if its memory access is allowed; Narcissus if it is on the classpath |

`Toolsets.defaults().toString()` tells the choice, e.g.
`Toolset[moduleOpening=UNSAFE, allocation=[UNSAFE, REFLECTION_FACTORY], constructorFallback=constructors, fieldAccess=[UNSAFE, REFLECTION]]`.

!!! note "Warnings of JDK 24+"
    Since JDK 24 the JVM warns once when `sun.misc.Unsafe` accesses memory (JEP 471). The warning is harmless. To get
    rid of it, start the JVM with `--sun-misc-unsafe-memory-access=allow`, or use a toolset without `Unsafe` field
    access (see below) and open the packages the cloner needs.

## Profiles

| Toolset                    | Mechanisms                                                                           |
|----------------------------|--------------------------------------------------------------------------------------|
| `Toolsets.defaults()`      | chosen for the running JVM, see above                                                |
| `Toolsets.conservative()`  | reflection, `Unsafe` only for fields reflection cannot access, `ReflectionFactory`, no module opening; the same on every JVM |
| `Toolsets.reflection()`    | reflection only, never `Unsafe`: fields of closed packages cannot be copied          |
| `Toolsets.unsafe()`        | `Unsafe` for allocation and fields                                                   |
| `Toolsets.narcissus()`     | the [Narcissus](https://github.com/toolfactory/narcissus) library for fields         |

## A custom toolset

```java
Toolset toolset = Toolsets.builder()
    .moduleOpening(ModuleOpening.INSTRUMENTATION)
    .allocation(Allocation.REFLECTION_FACTORY)
    .fieldAccess(FieldAccess.REFLECTION, FieldAccess.VAR_HANDLE)
    .noConstructorFallback()
    .build();
```

Mechanisms are tried in the order of adding; for every type or field the first one which supports it is used. A
mechanism not available in the JVM fails the build with an explanation; `Toolsets.isAvailable(...)` checks it
beforehand. The `spi` package lets you add your own allocators and field adapters.

## Closed packages of the JDK

Since JDK 16 reflection cannot access private members of JDK classes unless their packages are open. The default
toolset copies common graphs without any JVM options. If a toolset relies on reflection, open the packages, either by
JVM options:

```
--add-opens java.base/java.util=ALL-UNNAMED
```

(`io.github.sugarcubes.cloner` instead of `ALL-UNNAMED` if the cloner is a named module), or by one of the module
openers:

| `ModuleOpening`   | How                                                                                       |
|-------------------|-------------------------------------------------------------------------------------------|
| `INSTRUMENTATION` | the cloner agent: start the JVM with `-javaagent:/path/to/sugar-cubes-cloner.jar`         |
| `UNSAFE`          | `Unsafe`, needs its memory access allowed                                                 |
| `NARCISSUS`       | the Narcissus library on the classpath; since JDK 24 it needs `--enable-native-access=ALL-UNNAMED` to avoid a warning |
| `NONE`            | only what the JVM options open                                                            |
