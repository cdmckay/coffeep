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

package org.cdmckay.coffeep.readers;

import com.sun.tools.classfile.*;
import org.apache.log4j.Logger;
import org.cdmckay.coffeep.CoffeepMethod;

import java.util.Arrays;

public class MethodReader {

    private static final Logger logger = Logger.getLogger(MethodReader.class);

    private final ClassFile classFile;
    private final Method method;

    public MethodReader(ClassFile classFile, Method method) {
        this.classFile = classFile;
        this.method = method;
    }

    public CoffeepMethod read() throws ConstantPoolException, DescriptorException {
        final CoffeepMethod coffeepMethod = new CoffeepMethod();

        coffeepMethod.internalType = method.descriptor.getValue(classFile.constant_pool);
        coffeepMethod.modifiers = method.access_flags.getMethodModifiers();
        coffeepMethod.name = method.getName(classFile.constant_pool);
        coffeepMethod.flags = method.access_flags.getFieldFlags();
        coffeepMethod.returnType = method.descriptor.getReturnType(classFile.constant_pool);

        final Signature_attribute signatureAttribute = (Signature_attribute) method.attributes.get(Attribute.Signature);
        if (signatureAttribute == null) {
            // This doesn't need to handle the case Foo<Bar, Baz> because if the parameter types have type parameters,
            // they'll have a Signature attribute.
            final String joinedParameterTypes = method.descriptor.getParameterTypes(classFile.constant_pool);
            coffeepMethod.parameterTypes = Arrays.asList(
                joinedParameterTypes.substring(1, joinedParameterTypes.length() - 1).split(", ")
            );

            final Exceptions_attribute exceptionsAttribute =
                (Exceptions_attribute) method.attributes.get(Attribute.Exceptions);
            if (exceptionsAttribute != null) {
                for (int i = 0; i < exceptionsAttribute.number_of_exceptions; i++) {
                    coffeepMethod.throwsTypes.add(exceptionsAttribute.getException(i, classFile.constant_pool));
                }
            }
        } else {
            final Signature methodSignature = signatureAttribute.getParsedSignature();
            try {
                final Type.MethodType methodType = (Type.MethodType) methodSignature.getType(classFile.constant_pool);
                if (methodType.typeParamTypes != null) {
                    for (Type type : methodType.typeParamTypes) {
                        coffeepMethod.typeParameterTypes.add(type.toString().replace('/', '.'));
                    }
                }
                if (methodType.paramTypes != null) {
                    for (Type type : methodType.paramTypes) {
                        coffeepMethod.parameterTypes.add(type.toString().replace('/', '.'));
                    }
                }
                if (methodType.throwsTypes != null) {
                    for (Type type : methodType.throwsTypes) {
                        coffeepMethod.throwsTypes.add(type.toString().replace('/', '.'));
                    }
                }
            } catch (ConstantPoolException e) {
                throw new RuntimeException(e);
            }
        }

        final Code_attribute codeAttribute = (Code_attribute) method.attributes.get(Attribute.Code);
        if (codeAttribute != null) {
            coffeepMethod.code = new CodeReader(classFile, codeAttribute).read();
            final LineNumberTable_attribute lineNumberTableAttribute =
                (LineNumberTable_attribute) codeAttribute.attributes.get(Attribute.LineNumberTable);
            if (lineNumberTableAttribute != null) {
                for (LineNumberTable_attribute.Entry entry : lineNumberTableAttribute.line_number_table) {
                    coffeepMethod.lineNumberMap.put(entry.line_number, entry.start_pc);
                }
            }
        }

        return coffeepMethod;
    }

}
