package com.jrecord.common.data;

import android.app.Application;

import com.jrecord.common.base.Preconditions;

/**
 * Created by zhusong on 17/5/17.
 */

public class BasePreference {
    public static final String PREF_IDENTIFIER = "pref.identifier";
    private static IPreference preference;
    public static void init(Application application) {
        preference = IPreference.prefHolder.getPreference(application);
    }

    public static IPreference Preference() {
        Preconditions.checkNotNull(preference, "must init BasePreference first");
        return preference;
    }
}
