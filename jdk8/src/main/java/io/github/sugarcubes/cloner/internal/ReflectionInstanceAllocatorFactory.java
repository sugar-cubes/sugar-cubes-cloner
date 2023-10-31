package io.github.sugarcubes.cloner.internal;

class ReflectionInstanceAllocatorFactory implements InstanceAllocatorFactory {

    private final Reflection reflection;

    public ReflectionInstanceAllocatorFactory(Reflection reflection) {
        this.reflection = reflection;
    }

    @Override
    public <T> InstanceAllocator<T> newAllocator(Class<T> type) {
        return reflection.makeAccessible(reflection.getDeclaredConstructor(type))::newInstance;
    }

}
