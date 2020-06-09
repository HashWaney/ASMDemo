package me.hash.dynamic.proxy.track;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Created by Hash on 2020/6/9.
 */


public class MyApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private View mDecorView;

    private ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;

    @Override
    public void onCreate() {
        super.onCreate();
        this.registerActivityLifecycleCallbacks(this);
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                setAllViewsProxy((ViewGroup) mDecorView);
            }
        };
    }

    private void setAllViewsProxy(ViewGroup mDecorView) {
        int childCount = mDecorView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = mDecorView.getChildAt(i);
            if (view instanceof ViewGroup) {
                setAllViewsProxy(((ViewGroup) view));
            } else {
                if (view.hasOnClickListeners()) {
                    try {
                        HookClickHelper.hook(view);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }


    }


    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {

    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        // Why  这个时间。 onResume 这个时间和view的关系，
        mDecorView = activity.getWindow().getDecorView();
        setAllViewsProxy((ViewGroup) mDecorView);

    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {

    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        mDecorView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {

    }
}
