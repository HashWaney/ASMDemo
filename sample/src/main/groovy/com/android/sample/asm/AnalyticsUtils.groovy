package com.android.sample.asm;

class AnalyticsUtils {

    static void logD(String message) {
        println(message)
        if (AnalyticsExtension.isShowDebugInfo) {
        }
    }
}