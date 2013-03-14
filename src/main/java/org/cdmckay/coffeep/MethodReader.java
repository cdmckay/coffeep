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
import org.apache.log4j.Logger;

import java.util.Arrays;

public class MethodReader {

    private static Logger logger = Logger.getLogger(MethodReader.class);

    private ConstantPool constantPool;
    private Method method;

    public MethodReader(ConstantPool constantPool, Method method) {
        this.constantPool = constantPool;
        this.method = method;
    }

    public CoffeepMethod read() {
        final CoffeepMethod coffeepMethod = new CoffeepMethod();

        coffeepMethod.internalType = ReaderHelper.getString(method.descriptor, "value", constantPool);
        coffeepMethod.modifiers = method.access_flags.getMethodModifiers();
        coffeepMethod.name = ReaderHelper.getString(method, "name", constantPool);
        coffeepMethod.flags = method.access_flags.getFieldFlags();

        final Descriptor descriptor;
        final Signature_attribute signatureAttribute =
            (Signature_attribute) method.attributes.get(Attribute.Signature);
        if (signatureAttribute == null) {
            descriptor = method.descriptor;

            // This doesn't need to handle the case Foo<Bar, Baz> because if the parameter types have type parameters,
            // they'll have a Signature attribute.
            final String joinedParameterTypes = ReaderHelper.getString(descriptor, "parameterTypes", constantPool);
            coffeepMethod.parameterTypes = Arrays.asList(
                joinedParameterTypes.substring(1, joinedParameterTypes.length() - 1).split(", ")
            );

            final Exceptions_attribute exceptionsAttribute =
                (Exceptions_attribute) method.attributes.get(Attribute.Exceptions);
            if (exceptionsAttribute != null) {
                for (int i = 0; i < exceptionsAttribute.number_of_exceptions; i++) {
                    coffeepMethod.throwsTypes.add(
                        ReaderHelper.getString(exceptionsAttribute, "exception", i, constantPool)
                    );
                }
            }
        } else {
            final Signature methodSignature = signatureAttribute.getParsedSignature();
            descriptor = methodSignature;
            try {
                final Type.MethodType methodType = (Type.MethodType) methodSignature.getType(constantPool);
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
                logger.warn("Exception while getting signature attribute", e);
            }
        }

        coffeepMethod.returnType = ReaderHelper.getString(descriptor, "returnType", constantPool);

        return coffeepMethod;
    }

}
