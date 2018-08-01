package com.jrecord.common.base;

import android.Manifest;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by zhusong on 17/5/15.
 * permission : https://github.com/yanzhenjie/AndPermission/blob/master/README-CN.md
 */


public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected final String TAG = getClass().getSimpleName();

    protected @NonNull String[] needPermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE};
    }

    @Override
    public void onClick(View v) {

    }
}
