package me.hash.dynamic.proxy.track;

import android.util.Log;
import android.view.View;

/**
 * Created by Hash on 2020/6/10.
 */


public class WrapOnClickListener implements View.OnClickListener {
    private View.OnClickListener listener;
    public static final String TAG = WrapOnClickListener.class.getSimpleName();

    public WrapOnClickListener(View.OnClickListener originOnClickListener) {
        this.listener = originOnClickListener;
    }

    @Override
    public void onClick(View v) {
        Log.i(TAG, "click before");
        if (listener != null) {
            listener.onClick(v);
        }
        Log.i(TAG, "click after");

    }
}
