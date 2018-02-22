package com.jarry.wechat.ui;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.jarry.wechat.R;

import java.util.List;

import butterknife.ButterKnife;

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
