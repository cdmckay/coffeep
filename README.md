# coffeep

A Java class file disassembler inspired by [javap](http://docs.oracle.com/javase/7/docs/technotes/tools/windows/javap.html)
that outputs to JSON.

The easiest way to use coffeep is to build the JAR using Maven (remember to install Sun Tools first!):

```
> mvn package
> cd target
> java -jar coffeep-1.0-SNAPSHOT.jar --pretty Foo.class
{
  "systemInfo": {
    "classFileUri": "file:/path/to/Program.class",
    "classFileSize": 7974,
    "lastModifiedTimestamp": 1363472205248,
    "digestAlgorithm": "MD5",
    "digest": "fb9ebd25984722295d13ab79da3a28e8"
  },
  "type": {
    "sourceFile": "Program.java",
    "modifiers": [
      "public"
    ],
    "type": "class",
    "name": "org/cdmckay/coffeep/Program",
    "majorVersion": 51,
    "minorVersion": 0,
    "flags": [
      "ACC_PUBLIC",
      "ACC_SUPER"
    ],
    "fields": [ ... ],
    "methods": [ ... ]
  }
}
```

## Sun Tools

`coffeep` uses the Sun Tools suite included in the JDK. In order to compile `coffeep`, you will need to add Sun Tools
to your local Maven repository.

To do so, locate the `tools.jar` file on your system. Typically, this is in the `lib` folder of your JDK 7 install.

Next, run this command:

```
mvn install:install-file \
    -DgroupId=com.sun    \
    -DartifactId=tools   \
    -Dversion=1.7.0      \
    -Dpackaging=jar      \
    -Dfile=/path/to/tools.jar
```

Sun Tools should now be available to Maven.
