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
import org.cdmckay.coffeep.CoffeepCode;
import org.cdmckay.coffeep.CoffeepInstruction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class CodeReader {

    private static final Logger logger = Logger.getLogger(CodeReader.class);

    final ClassFile classFile;
    final Code_attribute codeAttribute;

    public CodeReader(ClassFile classFile, Code_attribute codeAttribute) {
        this.classFile = classFile;
        this.codeAttribute = codeAttribute;
    }

    public CoffeepCode read() {
        final CoffeepCode coffeepCode = new CoffeepCode();

        for (Instruction instruction : codeAttribute.getInstructions()) {
            final CoffeepInstruction coffeepInstruction = new CoffeepInstruction();
            coffeepInstruction.pc = instruction.getPC();
            coffeepInstruction.mnemonic = instruction.getMnemonic();

            coffeepInstruction.operands = instruction.accept(
                new Instruction.KindVisitor<List<String>, Void>() {

                    @Override
                    public List<String> visitNoOperands(Instruction instruction, Void v) {
                        return Collections.emptyList();
                    }

                    @Override
                    public List<String> visitArrayType(
                        Instruction instruction, Instruction.TypeKind typeKind, Void v
                    ) {
                        return Arrays.asList(typeKind.name);
                    }

                    @Override
                    public List<String> visitBranch(Instruction instruction, int offset, Void v) {
                        return Arrays.asList(String.valueOf(offset));
                    }

                    @Override
                    public List<String> visitConstantPoolRef(Instruction instruction, int index, Void v) {
                        return Arrays.asList(getConstantPoolRefValue(index));
                    }

                    @Override
                    public List<String> visitConstantPoolRefAndValue(
                        Instruction instruction, int index, int value, Void v
                    ) {
                        return Arrays.asList(getConstantPoolRefValue(index), String.valueOf(value));
                    }

                    @Override
                    public List<String> visitLocal(Instruction instruction, int index, Void v) {
                        return Arrays.asList(String.valueOf(index));
                    }

                    @Override
                    public List<String> visitLocalAndValue(
                        Instruction instruction, int index, int value, Void v
                    ) {
                        return Arrays.asList(String.valueOf(index), String.valueOf(value));
                    }

                    @Override
                    public List<String> visitLookupSwitch(
                        Instruction instruction, int defaultCase, int length, int[] matches, int[] offsets, Void v
                    ) {
                        final int pc = instruction.getPC();
                        final List<String> operands = new ArrayList<String>();
                        operands.add(String.valueOf(pc + defaultCase));
                        operands.add(String.valueOf(length));
                        for (int i = 0; i < length; i++) {
                            operands.add(String.valueOf(matches[i]));
                            operands.add(String.valueOf(pc + offsets[i]));
                        }
                        return operands;
                    }

                    @Override
                    public List<String> visitTableSwitch(
                        Instruction instruction, int defaultCase, int low, int high, int[] offsets, Void v
                    ) {
                        final int pc = instruction.getPC();
                        final List<String> operands = new ArrayList<String>();
                        operands.add(String.valueOf(pc + defaultCase));
                        operands.add(String.valueOf(low));
                        operands.add(String.valueOf(high));
                        for (int offset : offsets) {
                            operands.add(String.valueOf(pc + offset));
                        }
                        return operands;
                    }

                    @Override
                    public List<String> visitValue(Instruction instruction, int value, Void v) {
                        return Arrays.asList(String.valueOf(value));
                    }

                    @Override
                    public List<String> visitUnknown(Instruction instruction, Void v) {
                        return Collections.emptyList();
                    }

                }, null
            );

            coffeepCode.instructions.add(coffeepInstruction);
        }

        return coffeepCode;
    }

    private String getConstantPoolRefValue(int index) {
        ConstantPool.CPInfo constantPoolInfo;

        try {
            constantPoolInfo = classFile.constant_pool.get(index);
            final int tag = constantPoolInfo.getTag();
            switch (tag) {
                case ConstantPool.CONSTANT_Methodref:
                case ConstantPool.CONSTANT_InterfaceMethodref:
                case ConstantPool.CONSTANT_Fieldref:
                    ConstantPool.CPRefInfo constantPoolRefInfo = (ConstantPool.CPRefInfo) constantPoolInfo;
                    constantPoolInfo = classFile.constant_pool.get(constantPoolRefInfo.name_and_type_index);
            }
        } catch (ConstantPool.InvalidIndex e) {
            throw new RuntimeException(e);
        }

        return getConstantPoolReferenceValue(constantPoolInfo);
    }

    private String getConstantPoolReferenceValue(ConstantPool.CPInfo constantPoolInfo) {
        return constantPoolInfo.accept(
            new ConstantPool.Visitor<String, Void>() {

                @Override
                public String visitClass(
                    ConstantPool.CONSTANT_Class_info info, Void v
                ) {
                    try {
                        return info.getName();
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitDouble(
                    ConstantPool.CONSTANT_Double_info info, Void v
                ) {
                    return String.valueOf(info.value);
                }

                @Override
                public String visitFieldref(
                    ConstantPool.CONSTANT_Fieldref_info info, Void v
                ) {
                    return visitRef(info, v);
                }

                @Override
                public String visitFloat(
                    ConstantPool.CONSTANT_Float_info info, Void v
                ) {
                    return String.valueOf(info.value);
                }

                @Override
                public String visitInteger(
                    ConstantPool.CONSTANT_Integer_info info, Void v
                ) {
                    return String.valueOf(info.value);
                }

                @Override
                public String visitInterfaceMethodref(
                    ConstantPool.CONSTANT_InterfaceMethodref_info info, Void v
                ) {
                    return visitRef(info, v);
                }

                @Override
                public String visitInvokeDynamic(
                    ConstantPool.CONSTANT_InvokeDynamic_info info, Void v
                ) {
                    try {
                        return String.format(
                            "#%d:%s",
                            info.bootstrap_method_attr_index,
                            getConstantPoolReferenceValue(info.getNameAndTypeInfo())
                        );
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitLong(
                    ConstantPool.CONSTANT_Long_info info, Void v
                ) {
                    return String.valueOf(info.value);
                }

                @Override
                public String visitNameAndType(
                    ConstantPool.CONSTANT_NameAndType_info info, Void v
                ) {
                    try {
                        return String.format(
                            "%s:%s",
                            info.getName(),
                            info.getType()
                        );
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitMethodref(
                    ConstantPool.CONSTANT_Methodref_info info, Void v
                ) {
                    return visitRef(info, v);
                }

                @Override
                public String visitMethodHandle(
                    ConstantPool.CONSTANT_MethodHandle_info info, Void v
                ) {
                    try {
                        return String.format(
                            "%s:%s",
                            info.reference_kind.name,
                            getConstantPoolReferenceValue(info.getCPRefInfo())
                        );
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitMethodType(
                    ConstantPool.CONSTANT_MethodType_info info, Void v
                ) {
                    try {
                        return info.getType();
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitString(
                    ConstantPool.CONSTANT_String_info info, Void v
                ) {
                    try {
                        return getConstantPoolReferenceValue(classFile.constant_pool.getUTF8Info(info.string_index));
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String visitUtf8(
                    ConstantPool.CONSTANT_Utf8_info info, Void v
                ) {
                    return info.toString();
                }

                private String visitRef(
                    ConstantPool.CPRefInfo info, Void v
                ) {
                    try {
                        return String.format(
                            "%s.%s",
                            info.getClassName(),
                            getConstantPoolReferenceValue(info.getNameAndTypeInfo())
                        );
                    } catch (ConstantPoolException e) {
                        throw new RuntimeException(e);
                    }
                }

            }, null
        );
    }

}
