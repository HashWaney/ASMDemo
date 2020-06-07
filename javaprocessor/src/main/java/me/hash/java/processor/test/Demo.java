package me.hash.java.processor.test;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import me.hash.java.processor.MyClass;
import me.hash.java.processor.visitor.CostClassVisitor;

/**
 * Created by Hash on 2020/6/7.
 */


public class Demo extends ClassLoader {
    public static void main(String[] args) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {

        System.out.println(System.getProperty("user.dir"));
        ClassReader reader = new ClassReader(MyClass.class.getName());
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        ClassVisitor visitor = new CostClassVisitor(writer);
        reader.accept(visitor, ClassReader.EXPAND_FRAMES);

        byte[] bytes = writer.toByteArray();

        //最终返回被修改或者说增加了的字节流
        FileOutputStream fos = null;

        //javaprocessor/build/classes/java/main/me/hash/java/processor/MyClass.class
        try {
            fos = new FileOutputStream(System.getProperty("user.dir") + "javaprocessor/build/classes/java/main/me/hash/java/processor/MyClass.class");
            fos.write(bytes);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }
        Demo demo = new Demo();
        Class a = demo.defineClass("me.hash.java.processor.MyClass", bytes, 0, bytes.length);
        Object o = a.newInstance();
        Method method = o.getClass().getMethod("showTime", String.class);
        method.invoke(o, "hello world");


    }
}
