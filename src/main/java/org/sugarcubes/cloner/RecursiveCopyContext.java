package org.sugarcubes.cloner;

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
    public void thenInvoke(ContextAction task) throws Exception {
        task.perform();
    }

    @Override
    public void complete() throws Exception {
    }

}
