package io.github.sugarcubes.cloner.internal;

import java.lang.reflect.Field;

public interface FieldAdapter {

    <T> T getObject(Object target) throws Exception;

    <T> void setObject(Object target, T value) throws Exception;

    int getInt(Object target) throws Exception;

    void setInt(Object target, int value) throws Exception;

    byte getByte(Object target) throws Exception;

    void setByte(Object target, byte value) throws Exception;

    boolean getBoolean(Object target) throws Exception;

    void setBoolean(Object target, boolean value) throws Exception;

    short getShort(Object target) throws Exception;

    void setShort(Object target, short value) throws Exception;

    char getChar(Object target) throws Exception;

    void setChar(Object target, char value) throws Exception;

    long getLong(Object target) throws Exception;

    void setLong(Object target, long value) throws Exception;

    float getFloat(Object target) throws Exception;

    void setFloat(Object target, float value) throws Exception;

    double getDouble(Object target) throws Exception;

    void setDouble(Object target, double value) throws Exception;

}
