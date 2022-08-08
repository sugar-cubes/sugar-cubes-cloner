
# The java cloner library [![build](https://github.com/mbutov/sugar-cubes-cloner/actions/workflows/build.yml/badge.svg)](https://github.com/mbutov/sugar-cubes-cloner/actions/workflows/build.yml)
                  
Deep cloning of any objects.

Java 8 compatible.

## Objectives

- To get simple and convenient way of deep-cloning of objects of any types.
- Make clean, configurable and extensible classes structure.

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
      
Faster than java.io serialization but still almost non-customizable.

### Other cloning libraries

- [kryo](https://github.com/EsotericSoftware/kryo#deep-and-shallow-copies)
- [kostaskougios/cloning](https://github.com/kostaskougios/cloning)

## The solution

### Interface

| Class | Description |
| --- | --- |
| [Cloner](src/main/java/org/sugarcubes/cloner/Cloner.java) | The cloner interface. |
| [ClonerException](src/main/java/org/sugarcubes/cloner/ClonerException.java) | Wrapper for all (checked and unchecked) exceptions, happened during cloning. Unchecked. |
| [Cloners](src/main/java/org/sugarcubes/cloner/Cloners.java) | Factory for standard cloners. |
| [CloningPolicy](src/main/java/org/sugarcubes/cloner/CloningPolicy.java) | Set of class/field rules for cloning. |
| [CopyAction](src/main/java/org/sugarcubes/cloner/CopyAction.java) | Copy action (null/original/clone). |
| [ObjectAllocator](src/main/java/org/sugarcubes/cloner/ObjectAllocator.java)| Allocator (instantiator) interface. |
| [ObjectCopier](src/main/java/org/sugarcubes/cloner/ObjectCopier.java) | Object copier interface. |
| [TraversalAlgorithm](src/main/java/org/sugarcubes/cloner/TraversalAlgorithm.java) | DFS (default) or BFS. |
            
### Usage

```java
Object clone = Cloners.reflection().clone(original);
```

### Customization

```java
Cloner cloner = new ReflectionCloner(new ObjenesisAllocator())  // new cloner instance with custom allocator
    .copier(MyObject.class, new MyObjectCopier())               // custom copier for MyObject type
    .type(ThreadLocal.class, CopyAction.ORIGINAL)               // copy thread locals by reference 
    .field(MyObject.class, "cachedValue", CopyAction.NULL);     // skip MyObject.cachedValue field
    .parallel();                                                // parallel mode

    MyObject myObjectClone = cloner.clone(myObject);            // perform cloning
```
          
### Implementation
                  
In sequential mode does not use recursion. Uses [DFS](https://en.wikipedia.org/wiki/Depth-first_search) (by default) or [BFS](https://en.wikipedia.org/wiki/Breadth-first_search) algorithm for the object graph traversal.

In parallel mode order is chosen by ForkJoinPool.

### Known limitations

Default configuration of reflection cloner does not clone lambdas and method references. These can be cloned using [UnsafeAllocator](src/main/java/org/sugarcubes/cloner/UnsafeAllocator.java).
        
### License

[Apache License 2.0](LICENSE.txt) © Maxim Butov
