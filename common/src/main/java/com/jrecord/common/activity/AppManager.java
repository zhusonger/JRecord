package com.jrecord.common.activity;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.jrecord.common.ILog;
import com.jrecord.common.base.Preconditions;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Created by zhusong on 17/5/22.
 */

public class AppManager {
    private final String TAG = AppManager.class.getSimpleName();
    private static final AppManager ourInstance = new AppManager();

    public static AppManager getInstance() {
        return ourInstance;
    }

    private AppManager() {
    }

    private Map<String, List<WeakReference<Activity>>> mLifeActivities = new HashMap<>();
    private WeakReference<Activity> mCurrent;

    public void onCreate(@NonNull Activity activity) {
        Preconditions.checkNotNull(activity);
        String name = activity.getClass().getName();
        ILog.d(TAG, "onCreate = " + name);
        List<WeakReference<Activity>> list = mLifeActivities.get(name);
        if (null == list) {
            list = new ArrayList<>();
            mLifeActivities.put(name, list);
        }
        list.add(new WeakReference<Activity>(activity));

    }

    public void onResume(@NonNull Activity activity) {
        mCurrent = new WeakReference<Activity>(activity);
    }

    public void onPause(@NonNull Activity activity) {

    }
    public void onDestroy(@NonNull Activity activity) {
        Preconditions.checkNotNull(activity);
        String name = activity.getClass().getName();
        ILog.d(TAG, "onDestroy = " + name);
        List<WeakReference<Activity>> list = mLifeActivities.get(name);
        if (null == list) {
            return;
        }
        WeakReference<Activity> removeRef = null;
        for (WeakReference<Activity> ref : list) {
            Activity value = ref.get();
            if (value == activity) {
                removeRef = ref;
                break;
            }
        }
        if (null != removeRef) {
            list.remove(removeRef);
        }
    }

    public Activity current() {
        if (null == mCurrent || mCurrent.get() == null) {
            return null;
        }
        ILog.d(TAG, "current : " + mCurrent.get());
        return mCurrent.get();
    }

    public Activity getActivity(@NonNull String key){
        Preconditions.checkNotNull(key);
        List<WeakReference<Activity>> list = mLifeActivities.get(key);
        if (null == list || list.isEmpty()) {
            return null;
        }
        for(WeakReference<Activity> weakActivity :list){
            Activity activity = weakActivity.get();
            if(activity == null){
                continue;
            }
            if(activity.getClass().getName().equals(key)){
                return activity;
            }
        }
        return null;
    }

    public void finishRecent(@NonNull String key) {
        Preconditions.checkNotNull(key);
        List<WeakReference<Activity>> list = mLifeActivities.get(key);
        if (null == list || list.isEmpty()) {
            return;
        }
        WeakReference<Activity> last = list.get(list.size() - 1);
        Activity activity = last.get();
        if (null == activity) {
            return;
        }
        activity.finish();
    }

    public void finishAll(@NonNull String key) {
        Preconditions.checkNotNull(key);
        List<WeakReference<Activity>> list = mLifeActivities.get(key);
        if (null == list || list.isEmpty()) {
            return;
        }
        Activity loginInstance = null;
        for (WeakReference<Activity> ref : list) {
            Activity activity = ref.get();
            if (activity == null) {
                continue;
            }
            activity.finish();
        }
        list.clear();
    }

    public void finishCurrent() {
        if (null != mCurrent && mCurrent.get() != null) {
            Activity activity = mCurrent.get();
            activity.finish();
        }
    }

    public void exit() {
        Set<String> keys = mLifeActivities.keySet();
        for (String key : keys) {
            finishAll(key);
        }
    }
}
