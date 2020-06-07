package com.android.sample.asm

import org.objectweb.asm.AnnotationVisitor
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.Handle
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes;


/**
 * 使用ASM的ClassReader读取原始的类字节码的数据，并加载类
 * 使用自定义的ClassVisitor,进行修改符合特定条件的方法
 * 最后返回修改后的字节数组
 */
class AnalyticsClassVisitor extends ClassVisitor {

    private ClassVisitor api;

    private MethodVisitor methodVisitor;

    protected String currentClassName;
    protected String[] interfaces;

    AnalyticsClassVisitor(final ClassVisitor api) {
        super(Opcodes.ASM6, api)
        this.api = api;
    }

    /**
     * 拿到.class的所有信息，比如当前类实现的接口 类
     * @param version jdk 版本
     * @param access 访问权限  public ---- ACC_PUBLIC
     * @param name 当前类名称
     * @param signature 方法签名   String getString(int code)---- > (I)Ljava/lang/String;
     * @param superName 当前类的父类
     * @param interfaces 当前类实现的接口列表
     */
    @Override
    void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.currentClassName = name;
        this.interfaces = interfaces;
        AnalyticsUtils.logD("当前类是：" + name)
        AnalyticsUtils.logD("当前类的父：" + superName + " 当前类实现的接口类别：" + interfaces);
    }

    /**
     * 这里可以拿到关于method的所有信息，比如方法名称，方法的参数描述
     * @param access 方法的修饰符
     * @param name 方法名称
     * @param desc 方法的返回值是 boolean array int see {@link org.objectweb.asm.Type Type}
     * @param signature 方法签名
     * @param exceptions 方法抛出异常信息
     * @return
     */
    @Override
    MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {

        AnalyticsUtils.logD("current method 【 access= " + access + "，name=" + name + "，desc=" + desc + "，signature=" + signature + ",exceptions=" + exceptions + "")

        // 从父类这里拿到方法访问者，获取到所有的方法信息
        methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)

        String nameDesc = name + desc;
        // 将拿到的方法访问者 自行处理，比如处理对应的注解信息，以及方法的入口 出口等操作
        methodVisitor = new AnalyticsMethodVisitor(methodVisitor, access, name, desc) {
            @Override
            protected void onMethodEnter() { // 方法进入节点
                super.onMethodEnter()
//                if (interfaces != null && interfaces.length > 0) {
//                    // 如果当前类实现的接口有View$OnClickListener.并且当前进入的是onClick(Landroid/view/View;)V
//                    if (interfaces.contains('android/view/View$View.OnClickListener')
//                            && nameDesc == 'onClick(Landroid/view/View;)V') {
//
//                        AnalyticsUtils.logD("插桩：OnClickListener nameDesc:" + nameDesc + " currentClassName:"
//                                + currentClassName)
//                        //插入逻辑
//                        methodVisitor.visitVarInsn(ALOAD, 1)
//                        methodVisitor.visitMethodInsn(INVOKESTATIC,
//                                "me/hash/asm/OnClickAnalytics", "onViewClick",
//                                "(Landroid/view/View;)V"
//                                , false)
//                    }
//                }
                if ((interfaces!= null && interfaces.length > 0)) {
                    //如果当前类实现的接口有View$OnClickListener，并且当前进入的方法是onClick(Landroid/view/View;)V
                    //这里如果不知道怎么写，可以写个demo打印一下，就很快知道了，这里涉及一些ASM和Java中不同的写法。
                    if ((interfaces.contains('android/view/View$OnClickListener')
                            && nameDesc == 'onClick(Landroid/view/View;)V')) {
                        AnalyticsUtils.logD("插桩：OnClickListener nameDesc:" + nameDesc + " currentClassName:" + currentClassName)

                        //这里就是插代码逻辑了
                        methodVisitor.visitVarInsn(ALOAD, 1)
                        methodVisitor.visitMethodInsn(INVOKESTATIC, "me/hash/asm/OnClickAnalytics", "onViewClick",
                                "(Landroid/view/View;)V", false)
                    }
                }
            }

            @Override
            protected void onMethodExit(int opcode) { // 方法退出节点
                super.onMethodExit(opcode)
            }

            @Override
            void visitInvokeDynamicInsn(String n, String s, Handle bsm, Object... bsmArgs) {
                super.visitInvokeDynamicInsn(n, s, bsm, bsmArgs)
            }

            @Override
            AnnotationVisitor visitAnnotation(String d, boolean visible) {
                return super.visitAnnotation(d, visible)
            }
        }
        return methodVisitor


    }
}