package io.github.sugarcubes.cloner.internal;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import io.github.sugarcubes.cloner.ClassUtils;
import io.github.sugarcubes.cloner.ClonerExceptionUtils;
import sun.misc.Unsafe;

public class EnvironmentImpl$Jdk9 extends EnvironmentImpl$Jdk8 {

    private static final boolean NARCISSUS = ClassUtils.isClassAvailable("io.github.toolfactory.narcissus.Narcissus");

    private final List<TypeOpener> openers = new ArrayList<>();

    public EnvironmentImpl$Jdk9() {

        Instrumentation instrumentation = ClonerAgent.getInstrumentation();
        if (instrumentation != null) {
            openers.add(new InstrumentationModuleOpenerImpl(instrumentation));
        }
        if (NARCISSUS) {
            openers.add(new NarcissusModuleOpenerImpl());
        }
        openers.add(TypeOpener.NOOP);

    }

    @Override
    public TypeOpener getOpener() {
        Instrumentation instrumentation = ClonerAgent.getInstrumentation();
        if (instrumentation != null) {
            return new InstrumentationModuleOpenerImpl(instrumentation);
        }
        if (NARCISSUS) {
            return new NarcissusModuleOpenerImpl();
        }
        return TypeOpener.NOOP;
    }

    @Override
    public Unsafe getUnsafe() {
        return null;
    }

}
