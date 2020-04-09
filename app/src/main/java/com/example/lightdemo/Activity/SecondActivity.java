package com.example.lightdemo.Activity;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.lightdemo.Bean.BookBitmap;
import com.example.lightdemo.R;
import com.example.lightdemo.Widget.MyListView;
import com.example.lightdemo.tools.HttpUtils;

import java.util.ArrayList;
import java.util.List;

public class SecondActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener{

    private MyListView myListView;
    private List<BookBitmap> bookBitmaps;
    private SwipeRefreshLayout srLayout;
    private int page = 1;

    private Messenger mService;
    private boolean isConn;

    private final int FIRST_CONNECT = 0x111;
    private final int DATA_OK = 0x121;

    //加载状态常量
    private final int INIT = 0;//初始状态
    private final int UP_PULL = 1;//上拉加载
    private final int DOWN_SCROLL = 2;//下滑加载
    private final int IDLE = 4;//空闲状态

    //加载状态
    private int loadStatus = INIT;
    //窗口高宽
    int widthPixels,heightPixels;
    @RequiresApi(api = Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setTitle("全部");
        }
        setContentView(R.layout.activity_second);
        //开始绑定服务
        bindServiceInvoked();
        initData();

        initWidget();
    }

    private void initData() {
        loadStatus=INIT;
        bookBitmaps = new ArrayList<>();
        new Thread(new Runnable(){
            @Override
            public void run() {
                HttpUtils.sendPostMessage("utf-8", page,getApplicationContext());
            }
        }).start();

    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void initWidget() {
            srLayout = (SwipeRefreshLayout)findViewById(R.id.swiperefresh);
            srLayout.setOnRefreshListener(this);

            myListView = (MyListView)findViewById(R.id.rv_book);
            getWindowWidth();
            myListView.setWindowWidth(widthPixels,heightPixels);
            myListView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
                @Override
                public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                    loadStatus=DOWN_SCROLL;
                }
            });


        }
/**
 * 获取窗口宽度
 * */

    private void getWindowWidth() {
        DisplayMetrics outMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(outMetrics);
         widthPixels = outMetrics.widthPixels;
        heightPixels = outMetrics.heightPixels;
    }

            int i = 0;
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        private void refreshAdapter(){

            switch (loadStatus){
                case IDLE:break;
                case UP_PULL:{//上拉刷新完毕
                    loadStatus = IDLE;
                    bookBitmaps.clear();
                    bookBitmaps.addAll(HttpUtils.getBookBitmaps());
                    srLayout.setRefreshing(false);
                    myListView.clearData();
                    myListView.refreshData(bookBitmaps);
                    myListView.requestLayout();
                    myListView.invalidate();
                    i++;
                }break;
                case DOWN_SCROLL: {//下滑刷新完毕
                    loadStatus = IDLE;
                    bookBitmaps.addAll(HttpUtils.getBookBitmaps());
                    myListView.addData(HttpUtils.getBookBitmaps());
                    i++;
                }break;
                case INIT:{//初始化完毕
                    loadStatus = IDLE;
                    bookBitmaps.clear();
                    bookBitmaps.addAll(HttpUtils.getBookBitmaps());
//                  adapter.notifyDataSetChanged();
                    myListView.init(bookBitmaps);
                    myListView.requestLayout();
                    myListView.invalidate();
                    i++;
                }
            }

        }

        //绑定service
        private void bindServiceInvoked()
        {
            Intent intent = new Intent();
            intent.setAction("com.zhy.aidl.calc");
            intent.setPackage("com.example.lightdemo");
            bindService(intent, mConn, Context.BIND_AUTO_CREATE);
            Log.e("Messeage", "bindService invoked !");
        }


            //client端handler
            @SuppressLint("HandlerLeak")
            private Messenger mMessenger = new Messenger(new Handler()
            {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
                @Override
                public void handleMessage(Message msgFromServer)
                {
                    switch (msgFromServer.what)
                    {
                        case FIRST_CONNECT:
                            Log.e("测试","客户端收到");
                            this.removeMessages(msgFromServer.what);
                            break;
                        case DATA_OK:
                            Log.e("测试","数据已更新");
                            //更新UI
                            refreshAdapter();
                            this.removeMessages(msgFromServer.what);
                            break;
                    }
                    super.handleMessage(msgFromServer);
                }
            });


    private ServiceConnection mConn = new ServiceConnection()
    {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service)
        {
            mService = new Messenger(service);
            isConn = true;
            //初始鏈接
            Message msgFromClient = Message.obtain(null, FIRST_CONNECT);
            msgFromClient.replyTo = mMessenger;
            if (isConn)
            {
                //往服务端发送消息
                try {
                    mService.send(msgFromClient);
                } catch (RemoteException ex) {
                    ex.printStackTrace();
                }
            }
    }

        @Override
        public void onServiceDisconnected(ComponentName name)
        {
            mService = null;
            isConn = false;
        }
    };

    //client端Message
    @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case android.R.id.home:
            //  this.finish();
                    return true;
            }
            return super.onOptionsItemSelected(item);
        }


    @Override
    public void onRefresh() {
        page = 1;
        new Thread(new Runnable(){
            @Override
            public void run() {
                HttpUtils.sendPostMessage("utf-8", page,getApplicationContext());
                loadStatus = UP_PULL;
            }
        }).start();

    }

         }





