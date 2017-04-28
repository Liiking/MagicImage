package com.qwy.magic.magicimage;

import android.app.Application;

import org.xutils.x;

/**
 * Created by qwy on 2016/10/19.
 * 自定义Application
 */
public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        x.Ext.init(this);
    }

}
