package org.sugarcubes.cloner;

import org.junit.jupiter.api.Test;

class ParallelReflectionClonerTest extends AbstractClonerTests {

    public ParallelReflectionClonerTest() {
        super(new ReflectionClonerBuilder().setDefaultExecutor().build());
    }

    @Test
    @Override
    void testSimpleFields() {
        super.testSimpleFields();
    }
}
