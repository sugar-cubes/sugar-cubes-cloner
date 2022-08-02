package org.sugarcubes.cloner.thirdparty;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.sugarcubes.cloner.AbstractClonerTests;
import org.sugarcubes.cloner.Cloner;

public class KKClonerTest extends AbstractClonerTests {

    @Override
    protected Cloner getCloner() {
        return new KKCloner();
    }

    @Test
    void testMap() {
        Map map = new HashMap();
        map.put("me", map);
        getCloner().clone(map);
    }
}
