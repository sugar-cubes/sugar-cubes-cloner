package org.sugarcubes.cloner;

import org.junit.jupiter.api.Test;

class ParallelReflectionClonerTest extends AbstractClonerTests {

    public ParallelReflectionClonerTest() {
        super(Cloners.builder().setMode(CloningMode.PARALLEL).build());
    }

    @Test
    @Override
    void testSimpleFields() {
        super.testSimpleFields();
    }
}
