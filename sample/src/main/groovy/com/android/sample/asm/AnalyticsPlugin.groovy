package com.android.sample.asm

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project;

/**
 * 这应该是写完修改原始字节码然后写入到相应的文件，或者jar文件中
 * 最后一步，就是通过一个plugin插件，让我们的功能能够被参与编译
 * 可以通过配置主工程目录的gradle.properties中的asmPluginSwitch.disablePlugin字段来决定是否开启此插件
 *
 * Plugin 的泛型是一个Project 意味着该插件是参与项目编译的
 */
class AnalyticsPlugin implements Plugin<Project> {

    @Override
    void apply(Project project) {

        AnalyticsExtension extension = project.extensions.create("asmPluginSwitch", AnalyticsExtension)

        boolean disableAnalyticsPlugin = false

        Properties properties = new Properties()
        // 其实就是读取gradle.properties中内容
        if (project.rootProject.file('gradle.properties').exists()) {
            properties.load(project.rootProject.file('gradle.properties').newDataInputStream())
            disableAnalyticsPlugin = Boolean.parseBoolean(properties.getProperty("disablePlugin", "false"))
        }
        if (!disableAnalyticsPlugin) { // 开启
            println("---------------开启全埋点插桩插件---------------------")
            //注册我们的Transform
            AppExtension appExtension = project.extensions.findByType(AppExtension.class)
            appExtension.registerTransform(new com.android.sample.asm.AnalyticsTransform(project, extension))

        } else { // 关闭
            println("---------------关闭全埋点插桩插件---------------------")
        }


    }
}