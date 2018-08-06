package com.jrecord.common.base;

import android.Manifest;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

/**
 * Created by zhusong on 17/5/15.
 * permission : https://github.com/yanzhenjie/AndPermission/blob/master/README-CN.md
 */


public class BaseActivity extends AppCompatActivity implements View.OnClickListener {
    protected final String TAG = getClass().getSimpleName();
    protected final int REQUEST_PERMISSION = 1;
    protected final String[] DEFAULT_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityCompat.requestPermissions(this, needPermissions(), REQUEST_PERMISSION);
    }

    protected @NonNull String[] needPermissions() {
        return DEFAULT_PERMISSION;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onClick(View v) {

    }
}
