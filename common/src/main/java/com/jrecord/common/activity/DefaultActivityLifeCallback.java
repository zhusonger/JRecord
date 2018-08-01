package com.jrecord.common.activity;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

/**
 * Created by zhusong on 17/5/22.
 */

public class DefaultActivityLifeCallback implements Application.ActivityLifecycleCallbacks {

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
        AppManager.getInstance().onCreate(activity);
    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        AppManager.getInstance().onResume(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        AppManager.getInstance().onPause(activity);
    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AppManager.getInstance().onDestroy(activity);
    }
}
