package com.jrecord.common.base;

import android.app.Application;

import com.jrecord.common.AppUtils;

/**
 * Created by zhusong on 17/8/24.
 */

public class BaseApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppUtils.init(this);
    }
}
