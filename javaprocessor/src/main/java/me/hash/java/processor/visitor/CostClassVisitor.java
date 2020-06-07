package me.hash.java.processor.visitor;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.AdviceAdapter;

import me.hash.java.processor.annotation.Cost;


/**
 * Created by Hash on 2020/6/6.Ø
 * <p>
 * 基于访问者模式，来访问自定义的注解。
 * <p>
 * 该类功能很强大，可以遍历所有类
 * 比如重写visitMethod方法就可以获取到所有类的方法
 */
public class CostClassVisitor extends ClassVisitor {
    public CostClassVisitor(ClassVisitor visitor) {
        super(Opcodes.ASM5, visitor);
    }


    @Override
    public MethodVisitor visitMethod(int access, final String name, final String desc, String signature, String[] exceptions) {

        // 编译所有类文件的方法
        MethodVisitor mv = cv.visitMethod(access, name, desc, signature, exceptions);

        // 然后对MethodVisitor中的所有方法进行一步处理，观察方法的注解，方法的执行起始
        mv = new AdviceAdapter(Opcodes.ASM5, mv, access, name, desc) {
            // 局部变量
            protected boolean inject = false;

            // 局部变量
            protected long startTime, endTime; // 两个方法之间不会共享局部变量， 怎么办。静态变量，供不同方法之间调用

            @Override
            public AnnotationVisitor visitAnnotation(String desc, boolean visiable) {
                if (Type.getDescriptor(Cost.class).equals(desc)) {
                    inject = true;
                }

                return super.visitAnnotation(desc, visiable);

            }

            @Override
            protected void onMethodEnter() {
                if (inject) {
                    // 统计时间 开始
//                    startTime = System.currentTimeMillis();
//                    PrintStream out = System.out; //
//                    out.println();
                    /**
                     * 1.static
                     * 2.owner java/lang/System
                     * 3.name out
                     * 4.desc Ljava/io/Print
                     */

//                    System.out.println("========start=========");
//                    TimeCache.setStartTime("newFunc", System.nanoTime());
//                    mv.visitLdcInsn("newFunc");
//                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime",
//                            "()J", false);
//                    // ()J 表示返回值为long 类型，参数为空
                    mv.visitMethodInsn(INVOKESTATIC, "me/java/hash/processor/TimeCache",
                            "setStartTime", "(Ljava/lang/String;J)V", false);
//
//                    //（Ljava/lang/String;J)V 表示调用了setStartTime 方法 该方法接收的是一个字符串和long类型参数，返回为void ，是否是接口：不是
//
//                    TimeCache.setEndTime("newFunc", System.nanoTime());
//
//
//                    System.err.println(TimeCache.getCostTime("newFunc"));
//                    mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/TimeCache",
//                            "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
//
//                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "println",
//                            "(Ljava/lang/String;)V", false);
//
//                    System.out.println("========end============");
//                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/System",
//                            "println", "(Ljava/lang/String;)V", false);
//                    mv.visitInsn(POP);

                    // System.out.println(=======start=========);
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System",
                            "out", "Ljava/io/PrintStream;");
                    mv.visitLdcInsn("================start================");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                            "println", "(Ljava/lang/String;)V", false);
                    // 要统计的方法函数的名称
                    mv.visitLdcInsn(name);

                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System",
                            "nanoTime", "()J", false
                    );

                    mv.visitMethodInsn(INVOKESTATIC, " me/hash/java/processor/time/TimeCache",
                            "setStartTime", "(Ljava/lang/String;J)V", false);


                }
            }

            @Override
            protected void onMethodExit(int i) {
                if (inject) {
                    // 统计时间结束
//                    endTime = System.currentTimeMillis();
                    mv.visitLdcInsn(name);
                    mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime",
                            "()J", false);
                    mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/time/TimeCache",
                            "setEndTime", "(Ljava/lang/String;J)V", false);


//                     System.out.println() 首先拿到PrintStream Field
//                    System.out.println(TimeCache.getCostTime(name));
                    mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out",
                            "Ljava/io/PrintStream;");
                    mv.visitLdcInsn(name);

                    mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/time/TimeCache",
                            "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
                    mv.visitMethodInsn(INVOKESTATIC, "java/io/PrintStream",
                            "println", "(Ljava/lang/String;)V", false);

                    mv.visitFieldInsn(GETSTATIC, "java/lang/System",
                            "out", "Ljava/io/PrintStream;");
                    // 将参数传入
                    mv.visitLdcInsn("==========end================");
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream",
                            "println", "(Ljava/lang/String;)V", false);


                }
            }
        };
        return mv;
    }

}
