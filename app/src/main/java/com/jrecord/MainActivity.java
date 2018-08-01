package com.jrecord;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.jrecord.common.ILog;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ILog.d("MainActivity", "test");
    }
}
