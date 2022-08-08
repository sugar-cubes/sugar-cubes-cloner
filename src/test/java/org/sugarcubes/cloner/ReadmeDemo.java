package org.sugarcubes.cloner;

/**
 * @author Maxim Butov
 */
public class ReadmeDemo {

    public static void main(String[] args) {

        SomeObject myObject = new SomeObject();

        Cloner cloner =
            // new cloner instance
            new ReflectionCloner(
                // custom allocator
                new ObjenesisAllocator(),
                // custom policy
                new CustomCloningPolicy()
                    // copy thread locals by reference
                    .type(ThreadLocal.class, CopyAction.ORIGINAL)
                    // skip MyObject.cachedValue field
                    .field(SomeOtherObject.class, "cachedValue", CopyAction.NULL)
            )
                // custom copier for MyObject type
                .copier(SomeObject.class, new MyObjectCopier())
                // parallel mode
                .parallel();

        SomeObject myObjectClone = cloner.clone(myObject);                    // perform cloning

    }

    private static class SomeObject {

    }

    private static class SomeOtherObject {

    }

    private static class MyObjectCopier implements ObjectCopier<SomeObject> {

        @Override
        public CopyResult<SomeObject> copy(SomeObject original, CopyContext context) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
