package org.sugarcubes.cloner;

import java.util.concurrent.Callable;

/**
 * Copy context with recursive invocations.
 *
 * @author Maxim Butov
 */
public class RecursiveCopyContext extends AbstractCopyContext {

    /**
     * Creates an instance.
     *
     * @param copierProvider copier provider
     */
    public RecursiveCopyContext(CopierProvider copierProvider) {
        super(copierProvider);
    }

    @Override
    public void thenInvoke(Callable<?> task) throws Exception {
        task.call();
    }

    @Override
    public void complete() throws Exception {
    }

}
