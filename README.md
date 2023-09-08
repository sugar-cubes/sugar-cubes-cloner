
# Java deep cloning library

[![build](https://github.com/sugar-cubes/sugar-cubes-cloner/actions/workflows/build.yml/badge.svg)](https://github.com/sugar-cubes/sugar-cubes-cloner/actions/workflows/build.yml)
[![maven central](https://maven-badges.herokuapp.com/maven-central/io.github.sugar-cubes/sugar-cubes-cloner/badge.svg)](https://search.maven.org/search?q=g:io.github.sugar-cubes+a:sugar-cubes-cloner)
                  
Deep cloning of any objects.

- Intuitive.
- Fast.
- Thread safe.
- Supports parallel execution.
- Java 8+ compatible.

Maven:
```xml
<dependency>
    <groupId>io.github.sugar-cubes</groupId>
    <artifactId>sugar-cubes-cloner</artifactId>
    <version>1.2.0</version>
</dependency>
```

Gradle:
```groovy
implementation "io.github.sugar-cubes:sugar-cubes-cloner:1.2.0"
```

It is recommended also to include [Objenesis](https://github.com/easymock/objenesis) library into your application.  

## Objectives

- To get simple, convenient and configurable way of deep-cloning of objects of any types.
- Make clean and extensible classes structure.

## Existing solutions
                    
### Java serialization

Many projects use serialization for object cloning. The raw solution is: 
```java
ByteArrayOutputStream buffer = new ByteArrayOutputStream();
new ObjectOutputStream(buffer).writeObject(original);
return new ObjectInputStream(new ByteArrayInputStream(buffer.toByteArray())).readObject();
```
This code can be simplified with Apache commons-lang [SerializationUtils.clone(object)](https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/SerializationUtils.html#clone-T-) or Spring Framework [SerializationUtils.deserialize(SerializationUtils.serialize(object))](https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/util/SerializationUtils.html).

Pros:
- portable, works on any JVM
- no external dependencies

Cons:
- slow
- requires all objects to be serializable
- may be customized only by changing [serialization](https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) process

### Other serialization libraries
                                       
Still serialization, but with non-standard libraries, such as:
- [fast-serialization](https://github.com/RuedigerMoeller/fast-serialization)
- [kryo](https://github.com/EsotericSoftware/kryo)
      
Faster than java.io serialization.

### Other cloning libraries

- [kryo](https://github.com/EsotericSoftware/kryo#deep-and-shallow-copies)
- [kostaskougios/cloning](https://github.com/kostaskougios/cloning)

## The solution

### Interface

| Class | Description |
| --- | --- |
| [Cloner](jdk8/src/main/java/io/github/sugar-cubes/cloner/Cloner.java) | The cloner interface. |
| [ClonerException](jdk8/src/main/java/io/github/sugar-cubes/cloner/ClonerException.java) | Wrapper for all (checked and unchecked) exceptions, happened during cloning. Unchecked. |
| [Cloners](jdk8/src/main/java/io/github/sugar-cubes/cloner/Cloners.java) | Factory for standard cloners. |
| [CopyAction](jdk8/src/main/java/io/github/sugar-cubes/cloner/CopyAction.java) | Copy action (skip/null/original/clone). |
| [CopyPolicy](jdk8/src/main/java/io/github/sugar-cubes/cloner/CopyPolicy.java) | Set of class/field rules for cloning. |
| [ObjectCopier](jdk8/src/main/java/io/github/sugar-cubes/cloner/ObjectCopier.java) | Object copier interface. |
| [ReflectionClonerBuilder](jdk8/src/main/java/io/github/sugar-cubes/cloner/ReflectionClonerBuilder.java) | Builder for creating custom cloners. |
| [TraversalAlgorithm](jdk8/src/main/java/io/github/sugar-cubes/cloner/TraversalAlgorithm.java) | DFS (default) or BFS. |
            
### Usage

#### Clone an object

```java
SomeObject clone = Cloners.reflection().clone(original);
```

#### Do not clone instances of a class

```java
Cloner cloner = Cloners.builder()
    .setTypeAction(NonCloneableType.class, CopyAction.NULL)
    .build();
SomeObject clone = cloner.clone(original);
```

Here the instances of NonCloneableType in the original's object tree will be replaced with nulls in the clone.

```java
Cloner cloner = Cloners.builder()
    .setTypeAction(NonCloneableType.class, CopyAction.ORIGINAL)
    .build();
SomeObject clone = cloner.clone(original);
```

Here the instances of NonCloneableType in the original's object tree will be copied by reference into the clone.

The same thing can also bew done with annotations:

```java
@TypePolicy(CopyAction.NULL)
public class NonCloneableType {
```

or

```java
@TypePolicy(CopyAction.ORIGINAL)
public class NonCloneableType {
```

#### Skip some fields

Skip transient fields:

```java
Cloner cloner = Cloners.builder()
    .fieldAction(field -> Modifier.isTransient(field.getModifiers()), CopyAction.SKIP)
    .build();
SomeObject clone = cloner.clone(original);
```

Or (Hibernate case):

```java
Cloner cloner = Cloners.builder()
    .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.SKIP)
    .build();
SomeObject clone = cloner.clone(original);
```

#### Creating shallow copy

If you wish to create a shallow copy of an object, i.e. another object which references same children as the original,
you may do:

```java
Cloner cloner = Cloners.builder()
    .fieldPolicy(CopyPolicy.original())
    .build();
SomeObject clone = cloner.clone(original);
```

### Customization

```java
Cloner cloner =
    // new builder instance
    Cloners.builder()
        // custom allocator
        .objectFactoryProvider(new ObjenesisObjectFactoryProvider())
        // copy thread locals by reference
        .typeAction(Predicates.subclass(ThreadLocal.class), CopyAction.ORIGINAL)
        // copy closeables by reference
        .typeAction(Predicates.subclass(AutoCloseable.class), CopyAction.ORIGINAL)
        // skip SomeObject.cachedValue field when cloning
        .fieldAction(SomeObject.class, "cachedValue", CopyAction.SKIP)
        // set fields with @Transient annotation to null
        .fieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
        // custom copier for SomeOtherObject type
        .copier(CustomObject.class, new CustomObjectCopier())
        // parallel mode
        .mode(CloningMode.PARALLEL)
        // create cloner
        .build();

// perform cloning
SomeObject clone = cloner.clone(original);
```

### Annotations
                                  
It's possible to use annotations to configure field/type actions and custom type copiers.

| Annotation | Description |
| --- | --- |
| [FieldPolicy](jdk8/src/main/java/io/github/sugar-cubes/cloner/FieldPolicy.java) | Field copy policy. |
| [TypeCopier](jdk8/src/main/java/io/github/sugar-cubes/cloner/TypeCopier.java) | Type copier. |
| [TypePolicy](jdk8/src/main/java/io/github/sugar-cubes/cloner/TypePolicy.java) | Type copy policy. |

### Implementation
                       
There is three modes of execution: recursive, sequential (default) and parallel.

In sequential mode does not use recursion. Uses [Depth-first](https://en.wikipedia.org/wiki/Depth-first_search) (by default) or [Breadth-first](https://en.wikipedia.org/wiki/Breadth-first_search) algorithm for the object graph traversal.

In parallel mode the order of copying is unpredictable.

If the [Objenesis](https://github.com/easymock/objenesis) library is available in the classpath, uses it to instantiate objects. Otherwise, uses reflection.

The priority of copy configurations is:
1. (high) builder configuration;
2. annotations;
3. (low) default configuration (JDK immutable classes).

### Known limitations

The library requires Java 8 or higher.

Default configuration of reflection cloner does not clone lambdas and method references. These can be cloned using [UnsafeObjectFactoryProvider](jdk8/src/main/java/io/github/sugar-cubes/cloner/UnsafeObjectFactoryProvider.java).

Java 9+ restricts accessing objects members via Reflection API. To solve this one may
- use `--illegal-access=permit` JVM argument (works on Java below 17);
- use `--add-opens` JVM arguments, e.g. `--add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.lang.invoke=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAMED --add-opens java.base/java.util.concurrent=ALL-UNNAMED`.

### Versioning

[Semantic Versioning](https://semver.org).

### License

[Apache License 2.0](LICENSE.txt) © [Maxim Butov](https://github.com/mbutov)
