package com.jarry.wechat.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.v7.app.AppCompatActivity;

public abstract class BaseActivity extends AppCompatActivity {
    @LayoutRes
    public abstract int setLayout();

    public abstract void initView();

    public abstract void initData();

    public abstract void initListener();

    public abstract void setMore();

    public BaseActivity self;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
//        new Gson().fromJson("", new TypeToken<List<String>>() {
//        }.getType());
        setContentView(setLayout());
        //注册butterknife
        initData();
        initView();
        initListener();
        setMore();
    }


}
