package com.example.lightdemo.Activity;

import android.os.Bundle;
import android.util.DisplayMetrics;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.lightdemo.R;
import com.example.lightdemo.Widget.MyListView;

public class MainActivity extends AppCompatActivity {
    public int windowWidth;

    MyListView myListView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        initData();
        initWigdet();
    }

    private void initWigdet() {
        myListView = (MyListView)findViewById(R.id.mylistview);
    }

    private void initData() {
        windowWidth = getWindowWidth();

    }

    private int getWindowWidth() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
        int widthPixels = outMetrics.widthPixels;
        return widthPixels;
    }
}
