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
import org.cdmckay.coffeep.CoffeepField;

public class FieldReader {

    private static final Logger logger = Logger.getLogger(FieldReader.class);

    private final ClassFile classFile;
    private final Field field;

    public FieldReader(ClassFile classFile, Field field) {
        this.classFile = classFile;
        this.field = field;
    }

    public CoffeepField read() throws ConstantPoolException, DescriptorException {
        final CoffeepField coffeepField = new CoffeepField();

        coffeepField.internalType = field.descriptor.getValue(classFile.constant_pool);
        coffeepField.modifiers = field.access_flags.getFieldModifiers();
        coffeepField.type = field.descriptor.getFieldType(classFile.constant_pool);
        coffeepField.name = field.getName(classFile.constant_pool);
        coffeepField.flags = field.access_flags.getFieldFlags();

        final Signature_attribute signatureAttribute = (Signature_attribute) field.attributes.get(Attribute.Signature);
        if (signatureAttribute != null) {
            try {
                final Type type = signatureAttribute.getParsedSignature().getType(classFile.constant_pool);
                coffeepField.type = type.toString();
            } catch (ConstantPoolException e) {
                throw new RuntimeException(e);
            }
        }

        return coffeepField;
    }

}
