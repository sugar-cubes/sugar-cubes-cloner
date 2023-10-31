package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Field;

@FunctionalInterface
public
interface FieldAdapterFactory {

    FieldAdapter newAdapter(Field field);

}
