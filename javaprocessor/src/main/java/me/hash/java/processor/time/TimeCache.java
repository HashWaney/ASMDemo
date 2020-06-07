package me.hash.java.processor.time;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Hash on 2020/6/6.
 */


public class TimeCache {
    public static Map<String, Long> sStartTime = new HashMap<>();

    public static Map<String, Long> sEndTime = new HashMap<>();


    public static void setStartTime(String methodName, long time) {
        sStartTime.put(methodName, time);
    }

    public static void setEndTime(String methodName, long time) {
        sEndTime.put(methodName, time);


//
//        System.out.println("========start=========");
//        TimeCache.setStartTime("newFunc",System.nanoTime());
////        mv.visit
//
//        TimeCache.setEndTime("newFunc",System.nanoTime());
//
//        System.err.println(TimeCache.getCostTime("newFunc"));
//        System.out.println("========end============");


//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"); // 访问System类的 out方法 方法为静态的，println为PrintStream
//        mv.visitLdcInsn("========start=========");
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//        Label l2 = new Label();
//        mv.visitLabel(l2);
//        mv.visitLineNumber(27, l2);
//        mv.visitLdcInsn("newFunc");
//        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
//        mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/time/TimeCache", "setStartTime", "(Ljava/lang/String;J)V", false);
//        Label l3 = new Label();
//        mv.visitLabel(l3);
//        mv.visitLineNumber(29, l3);
//        mv.visitLdcInsn("newFunc");
//        mv.visitMethodInsn(INVOKESTATIC, "java/lang/System", "nanoTime", "()J", false);
//        mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/time/TimeCache", "setEndTime", "(Ljava/lang/String;J)V", false);
//        Label l4 = new Label();
//        mv.visitLabel(l4);
//        mv.visitLineNumber(31, l4);
//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "err", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn("newFunc");
//        mv.visitMethodInsn(INVOKESTATIC, "me/hash/java/processor/time/TimeCache", "getCostTime", "(Ljava/lang/String;)Ljava/lang/String;", false);
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
//        Label l5 = new Label();
//        mv.visitLabel(l5);
//        mv.visitLineNumber(32, l5);
//        mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//        mv.visitLdcInsn("========end============");
//        mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
    }


    public static String getCostTime(String methodName) {
        long start = sStartTime.get(methodName);
        long end = sEndTime.get(methodName);
        return "method:" + methodName + " main " + Long.valueOf(end - start) + "ns";


    }
}
