package me.hash.java.processor.transform;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.security.ProtectionDomain;

import me.hash.java.processor.visitor.CostClassVisitor;

/**
 * Created by Hash on 2020/6/6.
 * ClassFileTransformer 可以通过Instrumentation添加一个代理，这个代理可以在类被加载到JVM之前进行相应的修改或者增加操作
 * Instrumentation.addTransformer(自定义的一个Transformer,实现transform方法转换被修改或增加的原类字节码文件然后通过ClassWriter写入
 */
public class MethodCostTransform implements ClassFileTransformer {


    public static void premain(String args, Instrumentation instrumentation) {
        // TODO: 2020/6/6
        instrumentation.addTransformer(new MethodCostTransform());
    }


    // 将修改之后的class文件转换为二进制流 然后丢给JVM，Jvm加载的就是修改或者说增强的字节码，这样也就达到我们的目的了

    @Override
    public byte[] transform(ClassLoader loader, String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        // 传入一个字节码二进制流 所有的数据
        ClassReader reader = new ClassReader(classfileBuffer);
        // ClassWriter 其实就是将ClassReader读取的原始字节码流复制到新被修改的字节流中
        ClassWriter writer = new ClassWriter(reader, ClassWriter.COMPUTE_MAXS);
        // 提供一个访问入口accept() 方法来接收一个访问者 访问所有类结构，进行修改和增强
        reader.accept(new CostClassVisitor(writer), 8);
        byte[] bytes = writer.toByteArray();


        return bytes;
    }
}
