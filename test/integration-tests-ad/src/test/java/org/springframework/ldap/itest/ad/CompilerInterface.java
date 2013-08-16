package org.springframework.ldap.itest.ad;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

public class CompilerInterface {
    // Compile the given file - when we can drop Java 5 we'll use the Java 6 compiler API
    public static void compile(String directory, String file) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                new String[] { "javac", 
                               "-cp", "."+File.pathSeparatorChar+"target"+File.separatorChar+"classes"+
                                  File.pathSeparatorChar+System.getProperty("java.class.path"),
                                  directory+File.separatorChar+file });

        pb.redirectErrorStream(true);
        Process proc = pb.start();
        InputStream is = proc.getInputStream();
        InputStreamReader isr = new InputStreamReader(is);

        char[] buf = new char[1024];
        int count;
        StringBuilder builder = new StringBuilder();
        while ((count = isr.read(buf)) > 0) {
            builder.append(buf, 0, count);
        }
        
        boolean ok = proc.waitFor() == 0;

        if (!ok) {
            throw new RuntimeException(builder.toString());
        }
    }
}
