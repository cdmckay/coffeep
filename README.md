# coffeep

A Java class file disassembler that outputs to JSON.

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
