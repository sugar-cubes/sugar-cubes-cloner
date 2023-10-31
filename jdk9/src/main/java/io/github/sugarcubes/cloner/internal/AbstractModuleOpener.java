package io.github.sugarcubes.cloner.internal;

public abstract class AbstractModuleOpener extends AbstractTypeOpener {

    private final Module thisModule = AbstractModuleOpener.class.getModule();

    @Override
    protected void openImpl(Class<?> type) throws Exception {
        String packageName = type.getPackageName();
        Module module = type.getModule();
        if (!module.isOpen(packageName, thisModule)) {
            openModule(module, packageName, thisModule);
        }
    }

    protected abstract void openModule(Module module, String packageName, Module thisModule) throws Exception;

}
