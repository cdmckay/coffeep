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

public class TypeReader {

    private static Logger logger = Logger.getLogger(TypeReader.class);

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
            logger.warn("Exception while getting sourceFile attribute", e);
            coffeepType.sourceFile = "???";
        }

        coffeepType.modifiers = classFile.access_flags.getClassModifiers();
        coffeepType.type = classFile.isClass() ? "class" : "interface";
        coffeepType.name = ReaderHelper.getString(classFile, "name");
        coffeepType.majorVersion = classFile.major_version;
        coffeepType.minorVersion = classFile.minor_version;
        coffeepType.flags = classFile.access_flags.getClassFlags();

        for (Field field : classFile.fields) {
            coffeepType.fields.add(new FieldReader(classFile.constant_pool, field).read());
        }

        for (Method method : classFile.methods) {
            coffeepType.methods.add(new MethodReader(classFile.constant_pool, method).read());
        }

        return coffeepType;
    }

}
