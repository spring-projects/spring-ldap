package org.springframework.ldap.odm.test.utils;

import java.io.File;
import java.util.Arrays;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

public class CompilerInterface {
    // Compile the given file - when we can drop Java 5 we'll use the Java 6 compiler API
    public static void compile(String directory, String file) throws Exception {
        File toCompile = new File(directory, file);
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        Iterable<? extends JavaFileObject> javaFileObjects =
            fileManager.getJavaFileObjectsFromFiles(Arrays.asList(toCompile));
        compiler.getTask(null, fileManager, null, null, null, javaFileObjects).call();

        fileManager.close();
    }
}
