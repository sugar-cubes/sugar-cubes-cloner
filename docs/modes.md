# Modes

```java
Cloner cloner = Cloners.builder()
    .mode(CloningMode.SEQUENTIAL)
    .traversalAlgorithm(TraversalAlgorithm.BREADTH_FIRST)
    .build();
```

| Mode                   | How the graph is copied                                                                  |
|------------------------|------------------------------------------------------------------------------------------|
| `SEQUENTIAL` (default) | one thread, without recursion: any depth of the graph, e.g. a linked list of millions of nodes |
| `RECURSIVE`            | one thread, by recursion: the simplest order, the stack limits the depth of the graph    |
| `PARALLEL`             | the objects are copied by the tasks of an executor                                       |

In the sequential mode the traversal algorithm is `DEPTH_FIRST` (default) or `BREADTH_FIRST`; it changes the order
of copying, not the result.

## Parallel mode

```java
Cloner cloner = Cloners.builder()
    .executor(pool)   // implies CloningMode.PARALLEL
    .build();
```

Without an executor the parallel mode uses `ForkJoinPool.commonPool()`. Parallel copying pays off for large graphs
with many objects; for small ones the sequential mode is faster.

## Shared references and cycles

Every object of the graph is copied once: if two objects reference the same object, their clones reference the same
clone, and cycles are reproduced. Objects which create their clones from already copied values (records, `Copyable`,
single-phase custom copiers) cannot be completed while an object they wait for waits for them; the cloner reports such
cycles with a `ClonerException`, see [Troubleshooting](troubleshooting.md).
