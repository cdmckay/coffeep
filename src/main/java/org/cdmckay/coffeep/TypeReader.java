/*
 * Copyright 2013 Cameron McKay
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cdmckay.coffeep;

import com.sun.tools.classfile.*;
import org.cdmckay.coffeep.model.CoffeepType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class TypeReader {

    private ClassFile classFile;

    public TypeReader(ClassFile classFile) {
        if (classFile == null) {
            throw new IllegalArgumentException("classFile cannot be null");
        }

        this.classFile = classFile;
    }

    public CoffeepType read() {
        final CoffeepType coffeepType = new CoffeepType();

        final SourceFile_attribute sourceFileAttribute =
            (SourceFile_attribute) classFile.getAttribute(Attribute.SourceFile);
        try {
            coffeepType.sourceFile = sourceFileAttribute.getSourceFile(classFile.constant_pool);
        } catch (ConstantPoolException e) {
            coffeepType.sourceFile = "???";
        }

        coffeepType.modifiers = classFile.access_flags.getClassModifiers();
        coffeepType.type = classFile.isClass() ? "class" : "interface";
        coffeepType.name = get(classFile, "name");

        final Signature_attribute signatureAttribute = (Signature_attribute) classFile.getAttribute(
            Attribute.Signature
        );
        if (signatureAttribute == null) {
            boolean hasSuperclass = classFile.isClass() && classFile.super_class != 0;
            coffeepType.superClass = hasSuperclass ? get(classFile, "superclassName") : "java.lang.Object";
            for (int i = 0; i < classFile.interfaces.length; i++) {
                coffeepType.interfaces.add(get(classFile, "interfaceName", i));
            }
        } else {
            try {
                final Type signatureType = signatureAttribute.getParsedSignature().getType(classFile.constant_pool);
                if (signatureType instanceof Type.ClassSigType) {

                } else {

                }
            } catch (ConstantPoolException ignored) {
            }
        }

        return coffeepType;
    }

    private String get(Object object, String name, Object... parameters) {
        String value = "???";
        try {
            String methodName = "get" + name.substring(0, 1).toUpperCase() + name.substring(1);
            List<Class<?>> parametersTypeList = new ArrayList<Class<?>>();
            for (Object parameter : parameters) {
                parametersTypeList.add(parameter.getClass());
            }
            Class<?>[] parameterTypes = parametersTypeList.toArray(new Class<?>[parametersTypeList.size()]);

            Method method;
            try {
                method = object.getClass().getMethod(methodName, parameterTypes);
            } catch (NoSuchMethodException e) {
                // Try again, but this time with all boxed types as primitives.
                for (int i = 0; i < parameterTypes.length; i++) {
                    parameterTypes[i] = primitivize(parameterTypes[i]);
                }
                method = object.getClass().getMethod(methodName, parameterTypes);
            }
            value = (String) method.invoke(object, parameters);
        } catch (NoSuchMethodException ignored) {
        } catch (InvocationTargetException ignored) {
        } catch (IllegalAccessException ignored) {
        }
        return value.replace('/', '.');
    }

    public static Class<?> primitivize(Class<?> type) {
        if (type.isPrimitive()) {
            return type;
        }

        if (Boolean.class.equals(type)) {
            return Boolean.TYPE;
        }
        if (Character.class.equals(type)) {
            return Character.TYPE;
        }
        if (Byte.class.equals(type)) {
            return Byte.TYPE;
        }
        if (Short.class.equals(type)) {
            return Short.TYPE;
        }
        if (Integer.class.equals(type)) {
            return Integer.TYPE;
        }
        if (Long.class.equals(type)) {
            return Long.TYPE;
        }
        if (Float.class.equals(type)) {
            return Float.TYPE;
        }
        if (Double.class.equals(type)) {
            return Double.TYPE;
        }

        throw new RuntimeException("Error translating type: " + type);
    }

}
