package com.android.sample.asm


import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.IOUtils
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.regex.Matcher
import java.util.zip.ZipEntry;

/**
 * 这个类就是用来改写字节码的功能，
 */
class AnalyticsClassModifier {


    static File modifyJar(File jarFile, File tempDir, boolean nameHex, AnalyticsExtension analyticsExtension) {
        /**
         * 读取原 jar
         */
        def file = new JarFile(jarFile, false)

        /**
         * 设置输出到的 jar
         */
        def hexName = ""
        if (nameHex) {
            hexName = DigestUtils.md5Hex(jarFile.absolutePath).substring(0, 8)
        }
        def outputJar = new File(tempDir, hexName + jarFile.name)
        JarOutputStream jarOutputStream = new JarOutputStream(new FileOutputStream(outputJar))
        Enumeration enumeration = file.entries()
        while (enumeration.hasMoreElements()) {
            JarEntry jarEntry = (JarEntry) enumeration.nextElement()
            InputStream inputStream = null
            try {
                inputStream = file.getInputStream(jarEntry)
            } catch (Exception e) {
                return null
            }
            String entryName = jarEntry.getName()
            if (entryName.endsWith(".DSA") || entryName.endsWith(".SF")) {
                //ignore
            } else {
                String className
                ZipEntry zipEntry = new ZipEntry(entryName)
                jarOutputStream.putNextEntry(zipEntry)

                byte[] modifiedClassBytes = null
                byte[] sourceClassBytes = IOUtils.toByteArray(inputStream)
                if (entryName.endsWith(".class")) {
                    className = entryName.replace(Matcher.quoteReplacement(File.separator), ".").replace(".class", "")
                    if (isShouldModify(className, analyticsExtension)) {
                        modifiedClassBytes = modifyClass(sourceClassBytes)
                    }
                }
                if (modifiedClassBytes == null) {
                    modifiedClassBytes = sourceClassBytes
                }
                jarOutputStream.write(modifiedClassBytes)
                jarOutputStream.closeEntry()
            }
        }
        jarOutputStream.close()
        file.close()
        return outputJar
    }

    private static byte[] modifyClass(byte[] srcClass) throws IOException {
        // 1.利用ASM读取原始的class字节流并且加载类
        ClassReader classReader = new ClassReader(srcClass)
        //2.0 我们需要定义一个ClassWrite用来接收访问对原始字节流数据进行修改或增强
        ClassWriter classWriter = new ClassWriter(ClassWriter.COMPUTE_MAXS)
        //2.1然后利用自定义的一个ClassVisitor访问者来访问ClassReader,
        ClassVisitor classVisitor = new AnalyticsClassVisitor(classWriter);
        //2.2 通过ClassReader接收一个访问者用来访问字节码数据
        classReader.accept(classVisitor, ClassReader.EXPAND_FRAMES)
        //3。将写入的已经经过修改的字节流数据返回
        return classWriter.toByteArray();
    }

    /**
     * 是否需要修改（先简单过滤一些不需要操作的文件，提高编译速度，可以根据实际情况添加更多过滤）
     * @param className
     * @return
     */
    protected static boolean isShouldModify(String className, com.android.sample.asm.AnalyticsExtension analyticsExtension) {
        if (className.contains('R$') ||
                className.contains('R2$') ||//R2.class及其子类（butterknife）
                className.contains('R.class') ||
                className.contains('R2.class') ||
                className.contains('BuildConfig.class')) {
            AnalyticsUtils.logD("全埋点R/Build过滤>>>" + className)
            return false
        }
        /**
         * 方便一些needExcludePackageList子类型 定向插桩
         */
        if (analyticsExtension != null && analyticsExtension.needModifyPackageList != null && analyticsExtension.needModifyPackageList.size() > 0) {
            Iterator<String> iterator = analyticsExtension.needModifyPackageList.iterator()
            while (iterator.hasNext()) {
                String packageName = iterator.next()
                if (className.startsWith(packageName)) {
                    AnalyticsUtils.logD("需要埋点的包名>>>packageName:" + packageName)
                    AnalyticsUtils.logD("需要插桩的类>>>" + className)
                    return true
                }
            }
        }
        if (analyticsExtension != null && analyticsExtension.excludePackageList != null && analyticsExtension.excludePackageList.size() > 0) {
            Iterator<String> iterator = analyticsExtension.excludePackageList.iterator()
            while (iterator.hasNext()) {
                String packageName = iterator.next()
                if (className.startsWith(packageName)) {
                    AnalyticsUtils.logD("需要过滤的包名>>>packageName:" + packageName)
                    AnalyticsUtils.logD("需要过滤的类>>>" + className)
                    return false
                }
            }
        }
        AnalyticsUtils.logD("need Modify：>>>" + className + " 测试" )
        return true
    }

    static File modifyClassFile(File dir, File classFile, File tempDir) {
        File modified = null
        try {
            String className = path2ClassName(classFile.absolutePath.replace(dir.absolutePath + File.separator, ""))
            byte[] sourceClassBytes = IOUtils.toByteArray(new FileInputStream(classFile))
            byte[] modifiedClassBytes = modifyClass(sourceClassBytes)
            if (modifiedClassBytes) {//这种用法相当于if(modifiedClassBytes!=null)
                AnalyticsUtils.logD("current modify class:>>>" + className)
                modified = new File(tempDir, className.replace('.', '_') + '.class')
                AnalyticsUtils.logD("modify file name:>>>>"+modified.getPath())
                if (modified.exists()) {
                    modified.delete()
                }
                modified.createNewFile()
                new FileOutputStream(modified).write(modifiedClassBytes)
            }
        } catch (Exception e) {
            e.printStackTrace()
            modified = classFile
        }
        return modified
    }

    static String path2ClassName(String pathName) {
        //me/hash/asm/MainActivity$1.class
        AnalyticsUtils.logD("current path Name:>>>>"+pathName)
        pathName.replace(File.separator, ".").replace(".class", "")
        //me.hash.asm.MainActivity$1

    }
}