
# The java cloner library
           
This library is a part of [sugar-cubes-library](https://github.com/mbutov/sugar-cubes).

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
- Portable, works on any JVM
- No external dependencies

Cons:
- Slow
- Requires all objects to be serializable
- May be customized only by changing [serialization](https://docs.oracle.com/javase/8/docs/api/java/io/Serializable.html) process

### Alternative serialization libraries
                                       
Still serialization, but with non-standard libraries, such as:
- [fast-serialization](https://github.com/RuedigerMoeller/fast-serialization)
- [kryo](https://github.com/EsotericSoftware/kryo)
      
Faster than java.io serialization but still non-custmizable.

### Cloning libraries

#### [cloning](https://github.com/kostaskougios/cloning)
- Fast.
- Uses recursive algorithm, on some object graphs can overflow the stack. 
- Has problems with cyclic dependencies, e.g. such map:
```java
Map map = new HashMap();
map.put("me", map);
``` 
will not be cloned (stack overflow).                                                      

## The solution

### Interface
- [Cloner](src/main/java/org/sugarcubes/cloner/Cloner.java)
- [Cloners](src/main/java/org/sugarcubes/cloner/Cloners.java)
- [CloningPolicy](src/main/java/org/sugarcubes/cloner/CloningPolicy.java)
- [CopyAction](src/main/java/org/sugarcubes/cloner/CopyAction.java)
- [ObjectAllocator](src/main/java/org/sugarcubes/cloner/ObjectAllocator.java)
- [ObjectCopier](src/main/java/org/sugarcubes/cloner/ObjectCopier.java)

### Usage
```java
Object clone = Cloners.reflection().clone(original);
```

### Customization
```java
Cloner cloner = new ReflectionCloner(new ObjenesisAllocator())
    .copier(MyObject.class, new MyObjectCopier())
    .type(ThreadLocal.class, CopyAction.ORIGINAL)
    .field(MyObject.class, "cachedValue", CopyAction.NULL);

    MyObject myObjectClone = cloner.clone(myObject);
```
          
### Implementation
                  
Does not use recursion. A kind of DFS algorithm is used for the object graph traversal. The algorithm can easily be switched to BFS, see queue in [CopyContextImpl](src/main/java/org/sugarcubes/cloner/CopyContextImpl.java).

### Limitations

Default configuration of reflection cloner does not clone lambdas and method references. These can be cloned using [UnsafeObjectAllocator](src/main/java/org/sugarcubes/cloner/unsafe/UnsafeObjectAllocator.java).
