package com.jqorz.plantdetection.base;

import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * 基类Activity
 */
public abstract class BaseActivity extends AppCompatActivity {


    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);//禁止横屏
        setContentView(getLayoutResId());


        ButterKnife.bind(this);
        ButterKnife.setDebug(false);

        getIntentData();


        init();

    }


    protected void getIntentData() {//如果有Intent,在这里进行接收数据
    }


    protected abstract void init();

    protected abstract int getLayoutResId();


}
