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

import com.sun.tools.classfile.Code_attribute;
import com.sun.tools.classfile.Instruction;
import org.apache.log4j.Logger;
import org.cdmckay.coffeep.CoffeepCode;
import org.cdmckay.coffeep.CoffeepInstruction;

public class CodeReader {

    private static final Logger logger = Logger.getLogger(CodeReader.class);

    final Code_attribute codeAttribute;

    public CodeReader(Code_attribute codeAttribute) {
        this.codeAttribute = codeAttribute;
    }

    public CoffeepCode read() {
        final CoffeepCode coffeepCode = new CoffeepCode();

        for (Instruction instruction : codeAttribute.getInstructions()) {
            final CoffeepInstruction coffeepInstruction = new CoffeepInstruction();
            coffeepInstruction.pc = instruction.getPC();
            coffeepInstruction.mnemonic = instruction.getMnemonic();
            coffeepCode.instructions.add(coffeepInstruction);
        }

        return coffeepCode;
    }

}
