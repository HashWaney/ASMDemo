package com.android.sample.asm;

/**
 * 通过此配置，配置是否开启点击事件
 * 在需要控制的module下面gradle配置
 * 1. 事件插桩功能 开启
 * TODO
 */

class AnalyticsExtension {

    /**
     * 是否展示调试信息
     */
    public static boolean isShowDebugInfo = false


    /**
     * 需要排除插桩的包名
     */

    public ArrayList<String> excludePackageList = []

    /**
     * 需要插桩的包名
     */
    public ArrayList<String> needModifyPackageList = []

}