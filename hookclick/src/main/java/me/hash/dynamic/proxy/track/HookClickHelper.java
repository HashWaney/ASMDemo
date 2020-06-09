package me.hash.dynamic.proxy.track;

import android.view.View;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by Hash on 2020/6/10.
 */


public class HookClickHelper {

    public static void hook(View view) throws Exception {
        // 1. 获取ListenerInfo
        Method getListenerInfo = View.class.getDeclaredMethod("getListenerInfo");
        getListenerInfo.setAccessible(true);
        Object listenerInfo = getListenerInfo.invoke(view);
        //2. 得到 原始View的onClickListener事件方法
        Class<?> listenInfo = Class.forName("android.view.View$ListenerInfo");
        Field mOnClickListener = listenInfo.getDeclaredField("mOnClickListener");
        mOnClickListener.setAccessible(true);
        View.OnClickListener originOnClickListener = ((View.OnClickListener) mOnClickListener.get(listenerInfo));
        if (originOnClickListener == null || originOnClickListener instanceof WrapOnClickListener) {
            return;
        }
        //3.替换 listenerInfo 是view中的， wrap 是用来代替view中的onClickListener的
        WrapOnClickListener wrap = new WrapOnClickListener(originOnClickListener);
        mOnClickListener.set(listenerInfo, wrap);
    }
}
