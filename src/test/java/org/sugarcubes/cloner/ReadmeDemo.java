package org.sugarcubes.cloner;

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
                    // set SomeObject.cachedValue field to null when cloning
                    .field(SomeObject.class, "cachedValue", CopyAction.NULL)
            )
                // custom copier for SomeOtherObject type
                .copier(SomeOtherObject.class, new SomeOtherObjectCopier())
                // parallel mode
                .parallel();

        // perform cloning
        SomeObject myObjectClone = cloner.clone(myObject);

    }

    private static class SomeObject {

    }

    private static class SomeOtherObject {

    }

    private static class SomeOtherObjectCopier implements ObjectCopier<SomeOtherObject> {

        @Override
        public CopyResult<SomeOtherObject> copy(SomeOtherObject original, CopyContext context) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
