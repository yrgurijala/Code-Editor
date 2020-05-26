package bs.code;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;

public class CustomClassLoader extends ClassLoader {

    private byte[] getBytes(String className) throws IOException {
        File classFile = new File(className);
        long len = classFile.length();

        byte[] raw = new byte[(int)len];

        FileInputStream reader = new FileInputStream(classFile);

        int byteLength = reader.read(raw);
        if(byteLength != len) {
            throw new IOException("Cannot read all of " + className);
        }

        reader.close();
        return raw;
    }

    private boolean compile(String file) throws IOException {
        System.out.println("Compiling " + file + "...");
        Process p = Runtime.getRuntime().exec("javac "+file);
        try {
            p.waitFor();
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
        int ret = p.exitValue();
        return ret==0;
    }

    @Override
    public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class <?> loadedClass = findLoadedClass(name);
        //Look for the file
        String fileName = name.replace('.', '/');

        String javaFileName = name + ".java";
        String classFileName = name + ".class";

        File javaFile = new File(javaFileName);
        File classFile = new File(classFileName);

        //Try to compile
        if(javaFile.exists() && !classFile.exists()) {
            try {
                if(!compile(javaFileName) || !classFile.exists()) {
                    throw new ClassNotFoundException("Could not compile " + javaFileName);
                }
            } catch(IOException e) {
                throw new ClassNotFoundException();
            }
        }

        //Try to load bytes
        try {
            byte[] classRaw = getBytes(classFileName);
            loadedClass = defineClass(name, classRaw, 0, classRaw.length);

        } catch(IOException e) {
            //Not failure
        }
        //Check System Library
        if(loadedClass == null) {
            loadedClass = findSystemClass(name);
        }

        //Try to resolve class
        if(resolve && loadedClass != null) {
            resolveClass(loadedClass);
        }

        if(loadedClass == null) {
            throw new ClassNotFoundException(name);
        }

        return loadedClass;
    }
}
