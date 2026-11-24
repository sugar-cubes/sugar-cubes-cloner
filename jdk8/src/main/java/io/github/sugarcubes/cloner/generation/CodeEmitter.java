/*
 * Copyright 2017-2026 the original author or authors.
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
package io.github.sugarcubes.cloner.generation;

/**
 * Instructions of a method body, the subset the copier template needs. Names of classes are internal names
 * ({@code java/lang/Object}), types of fields and methods are descriptors.
 *
 * @author Maxim Butov
 */
public interface CodeEmitter {

    /**
     * Pushes a reference from a local variable.
     *
     * @param slot slot of the variable
     */
    void loadReference(int slot);

    /**
     * Pushes an {@code int} from a local variable.
     *
     * @param slot slot of the variable
     */
    void loadInt(int slot);

    /**
     * Pushes {@code null}.
     */
    void pushNull();

    /**
     * Pushes an {@code int} constant.
     *
     * @param value value
     */
    void pushInt(int value);

    /**
     * Checks the type of the reference on the stack.
     *
     * @param type internal name of a class or descriptor of an array
     */
    void checkCast(String type);

    /**
     * Replaces the object on the stack with the value of its field.
     *
     * @param owner internal name of the class declaring the field
     * @param name field name
     * @param descriptor field descriptor
     */
    void getField(String owner, String name, String descriptor);

    /**
     * Sets the field of the object below the value on the stack.
     *
     * @param owner internal name of the class declaring the field
     * @param name field name
     * @param descriptor field descriptor
     */
    void putField(String owner, String name, String descriptor);

    /**
     * Pushes a string constant.
     *
     * @param value value
     */
    void loadString(String value);

    /**
     * Pushes a class constant.
     *
     * @param type internal name of a class
     */
    void loadClass(String type);

    /**
     * Pushes the value of a static field.
     *
     * @param owner internal name of the class declaring the field
     * @param name field name
     * @param descriptor field descriptor
     */
    void getStatic(String owner, String name, String descriptor);

    /**
     * Sets a static field to the value on the stack.
     *
     * @param owner internal name of the class declaring the field
     * @param name field name
     * @param descriptor field descriptor
     */
    void putStatic(String owner, String name, String descriptor);

    /**
     * Invokes a static method of a class.
     *
     * @param owner internal name of the class declaring the method
     * @param name method name
     * @param descriptor method descriptor
     */
    void invokeStatic(String owner, String name, String descriptor);

    /**
     * Invokes a constructor or a private or super method.
     *
     * @param owner internal name of the class declaring the method
     * @param name method name
     * @param descriptor method descriptor
     */
    void invokeSpecial(String owner, String name, String descriptor);

    /**
     * Invokes a virtual method.
     *
     * @param owner internal name of the class declaring the method
     * @param name method name
     * @param descriptor method descriptor
     */
    void invokeVirtual(String owner, String name, String descriptor);

    /**
     * Returns from a {@code void} method.
     */
    void returnVoid();

    /**
     * Creates a label to be bound later.
     *
     * @return label
     */
    CodeLabel newLabel();

    /**
     * Binds the label to the current position.
     *
     * @param label label
     */
    void bind(CodeLabel label);

    /**
     * Jumps by the {@code int} on the stack: to {@code labels[value - low]} or to the default label.
     *
     * @param low value of the first label
     * @param defaultLabel target of the other values
     * @param labels targets of the values from {@code low}
     */
    void tableSwitch(int low, CodeLabel defaultLabel, CodeLabel... labels);

}
