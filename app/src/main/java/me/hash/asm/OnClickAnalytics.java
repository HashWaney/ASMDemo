package me.hash.asm;

import android.util.Log;
import android.view.View;

import androidx.annotation.Keep;

/**
 * Created by Hash on 2020/6/7.
 */

public class OnClickAnalytics {

    // 为啥这里要用static 方法，因为这个通过类加载的形式，类名的方式调用onViewClick方法
    // 因为这个类
    @Keep
    public static void onViewClick(View view) {
        Log.e(OnClickAnalytics.class.getSimpleName(), "插入成功:" + view);
    }
}
