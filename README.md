
# The java cloner library [![build](actions/workflows/build.yml/badge.svg)](actions/workflows/build.yml)
                  
Deep cloning of any objects.

Java 8 compatible.

Thread safe.

Supports parallel execution.

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
| [Cloner](jdk8/src/main/java/org/sugarcubes/cloner/Cloner.java) | The cloner interface. |
| [ClonerException](jdk8/src/main/java/org/sugarcubes/cloner/ClonerException.java) | Wrapper for all (checked and unchecked) exceptions, happened during cloning. Unchecked. |
| [Cloners](jdk8/src/main/java/org/sugarcubes/cloner/Cloners.java) | Factory for standard cloners. |
| [CopyAction](jdk8/src/main/java/org/sugarcubes/cloner/CopyAction.java) | Copy action (skip/null/original/clone). |
| [CopyPolicy](jdk8/src/main/java/org/sugarcubes/cloner/CopyPolicy.java) | Set of class/field rules for cloning. |
| [ObjectCopier](jdk8/src/main/java/org/sugarcubes/cloner/ObjectCopier.java) | Object copier interface. |
| [ReflectionClonerBuilder](jdk8/src/main/java/org/sugarcubes/cloner/ReflectionClonerBuilder.java) | Builder for creating custom cloners. |
| [TraversalAlgorithm](jdk8/src/main/java/org/sugarcubes/cloner/TraversalAlgorithm.java) | DFS (default) or BFS. |
            
### Usage

```java
Object clone = Cloners.reflection().clone(original);
```

For the cloning with serialization one can use:

```java
Object clone = Cloners.serialization().clone(original);
```

### Customization

```java
Cloner cloner =
    // new builder instance
    Cloners.builder()
        // custom allocator
        .setAllocator(new ObjenesisAllocator())
        // copy thread locals by reference
        .setTypeAction(ThreadLocal.class, CopyAction.ORIGINAL)
        // do not clone closeables
        .setTypeAction(Predicates.subclass(AutoCloseable.class), CopyAction.ORIGINAL)
        // skip SomeObject.cachedValue field when cloning
        .setFieldAction(SomeObject.class, "cachedValue", CopyAction.SKIP)
        // set fields with @Transient annotation to null
        .setFieldAction(Predicates.annotatedWith(Transient.class), CopyAction.NULL)
        // custom copier for SomeOtherObject type
        .setCopier(SomeOtherObject.class, new SomeOtherObjectCopier())
        // parallel mode
        .setMode(CloningMode.PARALLEL)
        // create cloner
        .build();

// perform cloning
SomeObject myObjectClone = cloner.clone(myObject);
```

### Annotations
                                  
It's possible to use annotations to configure field/type actions and custom type copiers.

| Annotation | Description |
| --- | --- |
| [FieldPolicy](jdk8/src/main/java/org/sugarcubes/cloner/FieldPolicy.java) | Field copy policy. |
| [TypeCopier](jdk8/src/main/java/org/sugarcubes/cloner/TypeCopier.java) | Type copier. |
| [TypePolicy](jdk8/src/main/java/org/sugarcubes/cloner/TypePolicy.java) | Type copy policy. |

### Implementation
                       
There is three modes of execution: recursive, sequential and parallel.

In sequential mode does not use recursion. Uses [DFS](https://en.wikipedia.org/wiki/Depth-first_search) (by default) or [BFS](https://en.wikipedia.org/wiki/Breadth-first_search) algorithm for the object graph traversal.

In parallel mode order is unpredictable.

If the [Objenesis](https://github.com/easymock/objenesis) library is available, uses it to instantiate objects. Otherwise, uses reflection.

The priority of copy configurations is:
1. (high) builder configuration;
2. annotations;
3. (low) default configuration (JDK immutable classes).

### Known limitations

Default configuration of reflection cloner does not clone lambdas and method references. These can be cloned using [UnsafeAllocator](jdk8/src/main/java/org/sugarcubes/cloner/UnsafeAllocator.java).
        
### Versioning

[Semantic Versioning](https://semver.org).

### License

[Apache License 2.0](LICENSE.txt) © Maxim Butov
