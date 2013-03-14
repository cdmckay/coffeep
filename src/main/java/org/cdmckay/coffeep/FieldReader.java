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

public class FieldReader {

    private static Logger logger = Logger.getLogger(FieldReader.class);

    private ConstantPool constantPool;
    private Field field;

    public FieldReader(ConstantPool constantPool, Field field) {
        this.constantPool = constantPool;
        this.field = field;
    }

    public CoffeepField read() {
        final CoffeepField coffeepField = new CoffeepField();

        coffeepField.internalType = ReaderHelper.getString(field.descriptor, "value", constantPool);
        coffeepField.modifiers = field.access_flags.getFieldModifiers();
        coffeepField.type = ReaderHelper.getString(field.descriptor, "fieldType", constantPool);
        coffeepField.name = ReaderHelper.getString(field, "name", constantPool);
        coffeepField.flags = field.access_flags.getFieldFlags();

        final Signature_attribute signatureAttribute = (Signature_attribute) field.attributes.get(Attribute.Signature);
        if (signatureAttribute != null) {
            try {
                final Type type = signatureAttribute.getParsedSignature().getType(constantPool);
                coffeepField.type = type.toString().replace('/', '.');
            } catch (ConstantPoolException e) {
                logger.warn("Exception while getting signature attribute", e);
            }
        }

        return coffeepField;
    }

}
