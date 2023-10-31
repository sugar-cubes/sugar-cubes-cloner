package io.github.sugarcubes.cloner.internal;

public interface Environment {

    class Holder {

        private static final Environment INSTANCE = new EnvironmentImpl();

    }

    static Environment getEnvironment() {
        return Holder.INSTANCE;
    }

    TypeOpener getOpener();

    sun.misc.Unsafe getUnsafe();

    Reflection getReflection();

    InstanceAllocatorFactory getAllocator();

}
