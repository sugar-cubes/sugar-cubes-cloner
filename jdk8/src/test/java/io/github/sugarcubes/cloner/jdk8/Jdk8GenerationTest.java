/*
 * Copyright 2017-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sugarcubes.cloner.jdk8;

import java.util.Collections;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import io.github.sugarcubes.cloner.Cloners;
import io.github.sugarcubes.cloner.CloningMode;
import io.github.sugarcubes.cloner.CopyPolicy;
import io.github.sugarcubes.cloner.ObjectCopier;
import io.github.sugarcubes.cloner.Toolsets;
import io.github.sugarcubes.cloner.engine.ReflectionCopierProvider;
import io.github.sugarcubes.cloner.generation.CopierGenerator;
import io.github.sugarcubes.cloner.generation.GeneratedFieldsCopier;
import io.github.sugarcubes.cloner.jdk8.model.Account;

/**
 * Generated copiers on JDK 8-14: anonymous classes of sun.misc.Unsafe with the access of the copied class.
 */
class Jdk8GenerationTest {

    @Test
    void copierIsGenerated() {
        Assertions.assertTrue(CopierGenerator.instance().isPresent());
        ReflectionCopierProvider provider = new ReflectionCopierProvider(Toolsets.defaults(), null,
            CopyPolicy.defaultPolicy(), CopyPolicy.defaultPolicy(), Collections.emptyMap(), Collections.emptySet(),
            true);

        ObjectCopier<Account> copier = provider.getCopier(new Account(1, 2, null));

        Assertions.assertTrue(copier instanceof GeneratedFieldsCopier, copier.getClass().getName());
        Assertions.assertTrue(copier.getClass().getName().startsWith(Account.class.getName() + "$$Copier"));
    }

    @ParameterizedTest
    @EnumSource(CloningMode.class)
    void accountsAreCloned(CloningMode mode) {
        Account parent = new Account(1, 10.5, null);
        Account account = new Account(2, 20.5, parent);
        account.setLinked(parent);
        parent.setLinked(account);

        Account clone = Cloners.builder().generatedCopiers(true).mode(mode).build().clone(account);

        Assertions.assertNotSame(account, clone);
        Assertions.assertEquals(2, clone.getNumber());
        Assertions.assertEquals(20.5, clone.getBalance());
        Assertions.assertTrue(clone.isActive());
        Assertions.assertEquals('E', clone.getCurrency());
        Assertions.assertEquals(Collections.singletonList("opened 2"), clone.getHistory());
        Assertions.assertNotSame(account.getHistory(), clone.getHistory());
        Assertions.assertArrayEquals(new int[] {1, 2}, clone.getLimits());
        Assertions.assertNotSame(account.getLimits(), clone.getLimits());
        Assertions.assertEquals("owner2", clone.getName());
        Assertions.assertEquals(7, clone.getRank());
        Assertions.assertSame(clone.getParent(), clone.getLinked());
        Assertions.assertSame(clone, clone.getParent().getLinked());
        Assertions.assertEquals("owner1", clone.getParent().getName());
    }

}
