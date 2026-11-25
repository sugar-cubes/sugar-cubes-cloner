# Troubleshooting

## `UncopyableObjectException: Cannot copy java.lang.Thread in com.acme.Task ...`

The graph contains a live object: a thread, a reference, an open file, a socket, a process, an executor. Decide what
the clone gets by a type, field or object action: `CopyAction.ORIGINAL` to share it, `CopyAction.CLEAR` to drop it, or
a custom copier. See [JDK types](jdk-types.md#not-copied-live-objects).

## `Cannot make ... accessible. Open its package to the cloner ...`

A toolset relies on reflection, and the package of the class is not open to the cloner. Use the default toolset, open
the package by `--add-opens`, or choose a module opening, see [Toolsets](toolsets.md#closed-packages-of-the-jdk).

## `Cannot access field ...`

No field access mechanism of the toolset can access the field. The same remedies as above; `Toolsets.unsafe()` reaches
any field of a normal class.

## `Cannot clone ...: it is part of a reference cycle ...`

An object which creates its clone from copied values (a record, a `Copyable`, a single-phase custom copier) is part of
a cycle which needs it before its clone exists. Break the cycle for cloning, e.g. by `CopyAction.CLEAR` for one of its
fields, or write a `TwoPhaseObjectCopier` for one of the types.

## `No instance allocator supports ...`

No mechanism of the toolset can create an instance of the class without a constructor, and the constructor fallback
is disabled or the class has no usable constructor. Use the default toolset or enable the constructor fallback.

## `Registration mismatch when cloning ...`

A custom copier or a `Copyable` must call `CopyContext.register(clone)` exactly once and return the registered clone.

## `WARNING: sun.misc.Unsafe::... has been called by io.github.sugarcubes.cloner...`

JDK 24+ warns once about `Unsafe` memory access, which the default toolset uses for fields. It is harmless; see
[Toolsets](toolsets.md#the-default-toolset) for how to avoid it.

## `WARNING: A restricted method in java.lang.System has been called ... narcissus`

Narcissus loads a native library. Start the JVM with `--enable-native-access=ALL-UNNAMED`, or remove Narcissus from
the classpath if you do not need it.
