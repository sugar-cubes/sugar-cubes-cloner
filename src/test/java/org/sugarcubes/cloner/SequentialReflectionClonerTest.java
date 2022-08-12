package org.sugarcubes.cloner;

import org.junit.jupiter.api.Test;

class SequentialReflectionClonerTest extends AbstractClonerTests {

    public SequentialReflectionClonerTest() {
        super(new ReflectionClonerBuilder().build());
    }

    @Test
    @Override
    void testSimpleFields() {
        super.testSimpleFields();
    }
}
