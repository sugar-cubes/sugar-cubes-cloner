package io.github.sugarcubes.cloner.internal;

import java.security.AccessController;
import java.security.PrivilegedExceptionAction;

import io.github.sugarcubes.cloner.ClonerExceptionUtils;

public class AccessControllerUtils {

    public static <T> T tryGetPrivileged(ThrowingSupplier<T, ? extends Exception>... suppliers) {
        RuntimeException ex = null;
        for (ThrowingSupplier<T, ? extends Exception> supplier : suppliers) {
            try {
                return tryGetPrivileged(supplier);
            }
            catch (RuntimeException e) {
                ex = e;
            }
        }
        throw ex;
    }

    public static <T> T tryGetPrivileged(ThrowingSupplier<T, ? extends Exception> supplier) {
        try {
            return AccessController.doPrivileged((PrivilegedExceptionAction<T>) supplier::get);
        }
        catch (Exception e) {
            return ClonerExceptionUtils.replaceException(supplier::get);
        }
    }

}
