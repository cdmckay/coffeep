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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.tools.classfile.Attribute;
import com.sun.tools.classfile.ClassFile;
import com.sun.tools.classfile.ConstantPoolException;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.util.Context;
import org.apache.log4j.Logger;
import org.cdmckay.coffeep.model.Coffeep;
import org.cdmckay.coffeep.model.CoffeepSystemInfo;

import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Program {

    static Logger logger = Logger.getLogger(Program.class);

    public static void main(String[] args) throws IOException, ConstantPoolException {
        if (args.length == 0) {
            logger.fatal("No class argument");
            System.out.println("Usage: coffeep <class>");
            return;
        }
        final String className = args[0];
        final JavaFileObject fileObject = getFileObject(className);
        if (fileObject == null) {
            logger.fatal("Class not found: " + className);
            System.out.println("Class not found: " + className);
            return;
        }

        final Coffeep coffeep = new Coffeep();
        coffeep.systemInfo = new CoffeepSystemInfo();

        final ClassFile classFile = getClassFile(fileObject, coffeep.systemInfo);
        final TypeReader typeReader = new TypeReader(classFile);
        coffeep.type = typeReader.read();

        final Gson gson = new GsonBuilder().setPrettyPrinting().create();
        final String json = gson.toJson(coffeep);
        System.out.println(json);
    }

    private static JavaFileObject getFileObject(String className) throws IOException {
        final Context context = new Context();
        final JavaFileManager fileManager = new JavacFileManager(context, true, null);

        JavaFileObject fileObject;

        fileObject = fileManager.getJavaFileForInput(
            StandardLocation.PLATFORM_CLASS_PATH, className, JavaFileObject.Kind.CLASS
        );
        if (fileObject != null) return fileObject;

        fileObject = fileManager.getJavaFileForInput(
            StandardLocation.CLASS_PATH, className, JavaFileObject.Kind.CLASS
        );
        if (fileObject != null) return fileObject;

        final StandardJavaFileManager standardFileManager = (StandardJavaFileManager) fileManager;
        return standardFileManager.getJavaFileObjects(className).iterator().next();
    }

    private static ClassFile getClassFile(JavaFileObject fileObject, CoffeepSystemInfo systemInfo) throws IOException, ConstantPoolException {
        if (fileObject == null) {
            throw new IllegalArgumentException("fileObject cannot be null");
        }
        if (systemInfo == null) {
            throw new IllegalArgumentException("systemInfo cannot be null");
        }

        InputStream inputStream = fileObject.openInputStream();
        try {
            MessageDigest messageDigest = null;
            try {
                messageDigest = MessageDigest.getInstance("MD5");
            } catch (NoSuchAlgorithmException e) {
                logger.warn("NoSuchAlgorithmException while getting MD5 MessageDigest", e);
            }

            DigestInputStream digestInputStream = new DigestInputStream(inputStream, messageDigest);
            CountingInputStream countingInputStream = new CountingInputStream(digestInputStream);

            Attribute.Factory attributeFactory = new Attribute.Factory();
            ClassFile classFile = ClassFile.read(countingInputStream, attributeFactory);

            systemInfo.classFileUri = fileObject.toUri();
            systemInfo.classFileSize = countingInputStream.getSize();
            systemInfo.lastModifiedTimestamp = fileObject.getLastModified();
            if (messageDigest != null) {
                systemInfo.digestAlgorithm = messageDigest.getAlgorithm();
                systemInfo.digest = new BigInteger(1, messageDigest.digest()).toString(16);
            }

            return classFile;
        } finally {
            inputStream.close();
        }
    }

    private static class CountingInputStream extends FilterInputStream {

        private int size;

        CountingInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public int read(byte[] buf, int offset, int length) throws IOException {
            int n = super.read(buf, offset, length);
            if (n > 0) {
                size += n;
            }
            return n;
        }

        @Override
        public int read() throws IOException {
            int b = super.read();
            size += 1;
            return b;
        }

        public int getSize() {
            return size;
        }

    }

}