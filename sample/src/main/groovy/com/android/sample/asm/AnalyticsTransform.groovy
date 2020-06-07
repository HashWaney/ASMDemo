package com.android.sample.asm

import com.android.build.api.transform.Context
import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import groovy.io.FileType
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

/**
 *  这个类的作用就是在被编译成dex文件之前能够拦截.class文件
 *  然后通过访问者模式完成对.class文件的特定的修改然后替换。
 *  最主要的作用在编译过程中拦截.class文件 这个是修改的先觉条件
 */
class AnalyticsTransform extends Transform {

    private static Project project;

    private AnalyticsExtension extension;

    AnalyticsTransform(Project project, AnalyticsExtension extension) {
        this.project = project;
        this.extension = extension;
    }

    /**
     * 返回该transform对应的task名称
     * 编译后会出现在build/intermediates/transform下生成对应的文件夹
     *
     * @return
     */
    @Override
    String getName() {
        return AnalyticsSetting.PLUGIN_NAME
    }
    /**
     * 需要处理的数据类型 有两种类型
     * CLASSES 代表处理的java的class文件，RESOURCE代表要处理的java的资源
     * 所以此处我们返回的是class 因为处理的就是class文件
     * @return
     */
    @Override
    Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }
    /**
     * 指的是Transform要操作的内容的范围
     * 1。 External_Libraries  只有外部库
     * 2。 Project   只有项目内容
     * 3。 Project_local_deps 只有项目的本地依赖（本地jar）
     * 4。 Provided_only 只提供本地或者远程依赖项
     * 5。 Sub_Projects  只有子项目
     * 6。 Sub_Projects_Local_Deps 只有子项目的本地依赖（本地jar）
     * 7。 TESTED_Code  由当前变量（包括依赖项）测试的代码
     *
     *
     * @return
     */
    @Override
    Set<? super QualifiedContent.Scope> getScopes() {
        //   ImmutableSet.of(Scope.PROJECT, Scope.SUB_PROJECTS, Scope.EXTERNAL_LIBRARIES);
//        SCOPE_FULL_PROJECT == Project + Sub_Projects + External_Libraries 项目，子项目，项目依赖
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    boolean isIncremental() {  // 是否支持增量构建
        return false
    }

    // TODO 就算什么都不做，也需要把所有的   《输入文件》》？？？？？类型？？？？？？（关注）》
    // TODO  拷贝到  《目标目录》》？？？？？where？（关注）？？？》下，
    // 如果不这样做，下一个Task就没有TransformInput，也就是说上一个Task的输出是作为下一个Task的输入，是一种链式的编译手法
    // 此方法为空实现就会导致最后打包的APK缺少.class文件
    @Override
    void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        _internalTransform(transformInvocation.context, transformInvocation.inputs, transformInvocation.outputProvider, transformInvocation.incremental)

    }

    void _internalTransform(Context context, Collection<TransformInput> inputs, TransformOutputProvider outputProvider, boolean isIncremental)
            throws IOException, TransformException, InterruptedException {

        _internalPrintMsg()


        if (!isIncremental) {
            outputProvider.deleteAll()
        }
        //1. 输入文件 inputs
        // Transform 的inputs 有两种类型，一种是目录，一种是jar包
        // 遍历 分开
        inputs.each { TransformInput input ->
            /**
             * 1.遍历目录
             */
            input.directoryInputs.each { DirectoryInput directoryInput ->
                //首先构建输出目录 根据输入的目录的名称，
                //返回给定范围，内容类型和格式的给定内容的位置。
                /**
                 * @name 目录的名称 独一无二的
                 * @contentType 类型* @scope 范围* @format 格式： 文件夹目录
                 */
                File dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes,
                        directoryInput.scopes,
                        Format.DIRECTORY)
                File dir = directoryInput.file
                if (dir) { // 文件夹不为空
                    HashMap<String, File> modifyMap = new HashMap<>()
                    //遍历 以某一个扩展名结尾的文件 .class文件
                    dir.traverse(type: FileType.FILES, nameFilter: ~/.*\.class/) {
                        File classFile ->
                            //是否需要进行修改的class文件
                            if (AnalyticsClassModifier.isShouldModify(classFile.name, extension)) {
                                File modifyFile = AnalyticsClassModifier.modifyClassFile(dir, classFile, context.getTemporaryDir());
                                // 修改的文件有了 那么最后是要去替换原来的那个class文件
                                // 首先要进行一次保存操作，map key 选谁？ 唯一的包名+类名确定唯一性
                                if (modifyFile) {
                                    // 用原文件的包名+类名来保存被修改的class文件方便后面去替换同样类名+包名的class字节码
                                    String key = classFile.absolutePath.replace(dir.absolutePath, "")
                                    modifyMap.put(key, modifyFile)
                                }
                            }
                    }
                    // 将输入的目录拷贝到目标目录
                    FileUtils.copyDirectory(directoryInput.file, dest)
                    //将map中的文件进行替换
                    modifyMap.entrySet().each {
                        Map.Entry<String, File> en ->
                            File target = new File(dest.absolutePath + en.getKey())
                            // 删除源文件中的内容
                            if (target.exists()) {
                                target.delete()
                            }
                            //将修改之后的class文件内容存入到原有的目录下的源文件
                            FileUtils.copyFile(en.getValue(), target)
                            // 拷贝完成 记得删除这个文件
                            en.getValue().delete()

                    }

                }
            }
            println("+++++++++delegate debug insert jar+++++++ " + input.jarInputs.size())
            //2. 拷贝jar到输出文件

            input.jarInputs.each { JarInput jarInput ->
                println("delegate debug iterator begin ")
                String destName = jarInput.file.name
                // 截取文件路径的md5值重命名输出文件 ？？？ 同名覆盖？？
                def hexName = DigestUtils.md5Hex(jarInput.file.absolutePath).substring(0, 8)
                AnalyticsUtils.logD("jar operation hexName:" + hexName);
                // 获取jar名字
                if (destName.endsWith(".jar")) {
                    // .jar干掉 拿到名字
                    destName = destName.substring(0, destName.length() - 4);
                }

                println("delegate debug insert after")
                // 输出文件给一个
                File dest = outputProvider.getContentLocation(
                        destName + "_" + hexName, jarInput.contentTypes,
                        jarInput.scopes,
                        Format.JAR)
                def modifiedJar = AnalyticsClassModifier.modifyJar(jarInput.file, context.getTemporaryDir(), true, extension)
                if (modifiedJar == null) {
                    modifiedJar = jarInput.file;
                }
                FileUtils.copyFile(modifiedJar, dest)
            }
        }

    }

    void _internalPrintMsg() {
        println()
        println("###########################################################")
        println("##########                                      ###########")
        println("##########            FBI  WARNING              ############")
        println("##########          transform 编译插件           #############")
        println("##########  CopyRight @https://github.com/HashWaney.git ######")
        println("########                                                #######")
        println("########                                                ########")
        println("################################################################")
    }
}