package org.sugarcubes.cloner;

public class ReadmeDemo {

    public static void main(String[] args) {

        SomeObject myObject = new SomeObject();

        Cloner cloner =
            // new builder instance
            new ReflectionClonerBuilder()
                // custom allocator
                .setAllocator(new ObjenesisAllocator())
                // copy thread locals by reference
                .setTypeAction(ThreadLocal.class, CopyAction.ORIGINAL)
                // set SomeObject.cachedValue field to null when cloning
                .setFieldAction(SomeObject.class, "cachedValue", FieldCopyAction.NULL)
                // custom copier for SomeOtherObject type
                .setObjectCopier(SomeOtherObject.class, new SomeOtherObjectCopier())
                // parallel mode
                .setDefaultExecutor()
                // create cloner
                .build();

        // perform cloning
        SomeObject myObjectClone = cloner.clone(myObject);

    }

    private static class SomeObject {

    }

    private static class SomeOtherObject {

    }

    private static class SomeOtherObjectCopier implements ObjectCopier<SomeOtherObject> {

        @Override
        public SomeOtherObject copy(SomeOtherObject original, CopyContext context) throws Exception {
            throw new UnsupportedOperationException();
        }
    }
}
