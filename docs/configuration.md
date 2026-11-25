# Configuration

A cloner is configured by `Cloners.builder()`. Everything below can be combined.

## Copy actions

What the clone gets for an object, a type or a field is a `CopyAction`:

| Action     | The clone gets                                                         |
|------------|------------------------------------------------------------------------|
| `DEFAULT`  | a copy, unless the object is immutable or shared by default            |
| `ORIGINAL` | the same object as the original                                        |
| `NULL`     | `null` (not for primitive fields)                                      |
| `SKIP`     | for fields only: the field is not written, it keeps its default value  |

## Types

```java
Cloner cloner = Cloners.builder()
    // instances of a type and of its subtypes are shared
    .typeAction(Predicates.subclass(Connection.class), CopyAction.ORIGINAL)
    // instances of exactly this class are replaced with null
    .typeAction(Cache.class, CopyAction.NULL)
    // by class name, e.g. for optional dependencies
    .typeAction("org.slf4j.Logger", CopyAction.ORIGINAL)
    .build();
```

`typeAction(Class, ...)` applies to the class only, not to its subclasses: use `Predicates.subclass()` for a hierarchy.

## Fields

```java
Cloner cloner = Cloners.builder()
    // one field
    .fieldAction(Order.class, "cachedTotal", CopyAction.SKIP)
    // all transient fields
    .fieldAction(field -> Modifier.isTransient(field.getModifiers()), CopyAction.NULL)
    // fields with an annotation
    .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
    .build();
```

## Objects

```java
Cloner cloner = Cloners.builder()
    // this very object is shared
    .objectAction(Currency.getInstance("EUR"), CopyAction.ORIGINAL)
    // the same, shorter
    .singleton(DEFAULT_SETTINGS)
    // the clone of this object is given
    .clone(originalCustomer, preparedCustomer)
    .build();
```

Object actions and object policies are asked for every copied object, which slows cloning down; prefer type and field
actions. `singleton()` and `clone()` cost nothing per object.

## Policies

Actions are shortcuts for policies: functions from a type, a field or an object to an action. A policy decides
everything at once:

```java
Cloner cloner = Cloners.builder()
    .typePolicy(type -> type.getName().startsWith("com.acme.cache.") ? CopyAction.NULL : CopyAction.DEFAULT)
    .fieldPolicy(CopyPolicy.original())   // a shallow clone: every field references the original value
    .build();
```

A policy returns `CopyAction.DEFAULT` for the cases it does not decide. The builder asks the policy first, then the
actions, then the annotations below.

## Annotations

Classes can carry their own rules:

```java
@TypePolicy(CopyAction.ORIGINAL)
public class Settings { ... }

@TypePolicy(value = CopyAction.NULL, applyToSubtypes = true)
public abstract class Session { ... }

public class Order {

    @FieldPolicy(CopyAction.SKIP)
    private transient BigDecimal cachedTotal;

}

@TypeCopier(MoneyCopier.class)
public final class Money { ... }
```

`@FieldPolicy` on a class applies to all fields declared by the class. The configuration of the builder takes
precedence over annotations, annotations take precedence over the defaults.

## Shallow mode of a type

```java
Cloner cloner = Cloners.builder()
    .shallow(Snapshot.class)
    .build();
```

Instances of `Snapshot` are copied, but their fields reference the same values as the original.

## Custom copiers

A copier creates the clone of an object of its type itself:

```java
public class MoneyCopier implements ObjectCopier<Money> {

    @Override
    public Money copy(Money original, CopyContext context) throws Exception {
        // register the clone before copying anything which may refer back to it
        Money clone = context.register(new Money(original.getAmount(), original.getCurrency()));
        return clone;
    }

}

Cloner cloner = Cloners.builder()
    .copier(Money.class, new MoneyCopier())
    .build();
```

A copier must call `context.register(clone)` exactly once. `context.copy(value)` clones referenced objects.

For objects which may be part of reference cycles extend `TwoPhaseObjectCopier`: `allocate()` creates an empty clone,
`deepCopy()` fills it later, when referenced objects may be still in progress.

A class can also copy itself by implementing `Copyable`:

```java
public class Matrix implements Copyable<Matrix> {

    private final double[][] cells;

    @Override
    public Matrix copy(CopyContext context) {
        return context.register(new Matrix(cells.clone()));
    }

}
```

## Toolset and modes

`toolset(Toolset)` chooses how objects are created and fields are accessed, see [Toolsets](toolsets.md);
`mode()`, `traversalAlgorithm()` and `executor()` choose how the graph is traversed, see [Modes](modes.md);
`generatedCopiers(boolean)` and `copyConstructors(boolean)` switch the fast paths, see
[Performance](performance.md).
