package com.android.sample.asm

import org.objectweb.asm.Attribute
import org.objectweb.asm.Label
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 实现一个适配器：
 * 目的是为了让MethodVisitor按照我们的建议来进行方法的增强和改造。
 * 比如方法的入口onMethodEnter 方法的出口onMethodExit
 * 这里就可以用来统计方法的耗时情况
 *
 */
class AnalyticsMethodVisitor extends AdviceAdapter {


    /**
     * Creates a new {@link AdviceAdapter}.
     *
     * @param api
     *            the ASM API version implemented by this visitor. Must be one
     *            of {@link Opcodes#ASM4}, {@link Opcodes#ASM5} or {@link Opcodes#ASM6}.
     * @param mv
     *            the method visitor to which this adapter delegates calls.
     * @param access
     *            the method's access flags (see {@link Opcodes}).
     * @param name
     *            the method's name.
     * @param desc
     *            the method's descriptor (see {@link Type Type}).
     */
    protected AnalyticsMethodVisitor(MethodVisitor mv, int access, String name, String desc) {
        super(Opcodes.ASM6, mv, access, name, desc)
    }

    /**
     * 表示ASM 开始扫描这个方法
     */
    @Override
    void visitCode() {
        super.visitCode()
        AnalyticsUtils.logD("visitCode 开始扫描method ")

    }

    @Override
    void visitAttribute(Attribute attr) {
        super.visitAttribute(attr)
    }

    /**
     * 访问方法的结尾。该方法是最后一个被调用的方法，用于通知访问者该方法的所有注释和属性都已被访问。
     */
    @Override
    void visitEnd() {
        super.visitEnd()
    }

    @Override
    void visitIincInsn(int var, int increment) {
        super.visitIincInsn(var, increment)
    }
    /**
     * 使用单个int操作数访问指令。
     * @param opcode
     * @param operand
     */
    @Override
    void visitIntInsn(int opcode, int operand) {
        super.visitIntInsn(opcode, operand)
    }

    @Override
    void visitVarInsn(int opcode, int var) {
        super.visitVarInsn(opcode, var)
    }
    /**
     *访问方法的最大堆栈大小和局部变量的最大数量
     */
    @Override
    void visitMaxs(int maxStack, int maxLocals) {
        super.visitMaxs(maxStack, maxLocals)
    }

    @Override
    void visitJumpInsn(int opcode, Label label) {
        super.visitJumpInsn(opcode, label)
    }

    @Override
    void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels) {
        super.visitLookupSwitchInsn(dflt, keys, labels)
    }


}