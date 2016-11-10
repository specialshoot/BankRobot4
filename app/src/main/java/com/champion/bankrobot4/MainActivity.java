package com.champion.bankrobot4;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.champion.bankrobot4.utils.ToastUtils;
import com.champion.bankrobot4.view.CircleMenuLayout;

import java.util.Timer;
import java.util.TimerTask;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @BindView(R.id.id_menulayout)
    CircleMenuLayout mCircleMenuLayout;

    private boolean isHide=false;

    private View main;
    private static Boolean isExit = false;    //判断是否第一次点击退出
    public static final int FLAG_HOMEKEY_DISPATCHED = 0x80000000; //需要自己定义标志

    private String[] mItemTexts = new String[]{"视频播放", "画廊", "人脸录入", "聊天"};
    private int[] mItemImgs = new int[]{R.drawable.film, R.drawable.picture,
            R.drawable.introduce, R.drawable.bubbles};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(FLAG_HOMEKEY_DISPATCHED, FLAG_HOMEKEY_DISPATCHED);//关键代码
        main = getLayoutInflater().from(this).inflate(R.layout.activity_main, null);
        setContentView(main);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {

        sendBroadcast(new Intent("android.intent.action.DISPLAY_STATUSBAR"));//进入APK主页时发送广播通知隐藏工具栏
        mCircleMenuLayout.setMenuItemIconsAndTexts(mItemImgs, mItemTexts);
        mCircleMenuLayout.setOnMenuItemClickListener(new CircleMenuLayout.OnMenuItemClickListener() {
            @Override
            public void itemClick(View view, int pos) {
                Intent intent = null;
                switch (pos) {
                    case 0:
                        intent = new Intent(MainActivity.this, MovieActivity.class);
                        startActivity(intent);
                        break;
                    case 1:
                        intent = new Intent(MainActivity.this, PictureActivity.class);
                        startActivity(intent);
                        break;
                    case 2:
                        intent = new Intent(MainActivity.this, FaceWebActivity.class);
                        startActivity(intent);
                        break;
                    case 3:
                        intent = new Intent(MainActivity.this, ChatActivity.class);
                        startActivity(intent);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void itemCenterClick(View view) {

            }
        });
    }

    @OnClick(R.id.trueExit)
    void exitBy2Click() {
        Timer tExit = null;
        if (isExit == false) {
            isExit = true;
            ToastUtils.showShort(MainActivity.this, "再按一次退出程序");
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false;   //取消退出
                }
            }, 2000);    //等待2秒钟
        } else {
            sendBroadcast(new Intent("android.intent.action.HIDE_STATUSBAR"));//点击显示按钮时发送广播通知显示工具栏
            finish();
        }
    }

    @OnClick(R.id.hideNav)
    void hideNav(){
        if(isHide){
            isHide=false;
            sendBroadcast(new Intent("android.intent.action.HIDE_STATUSBAR"));//点击显示按钮时发送广播通知显示工具栏
        }else{
            isHide=true;
            sendBroadcast(new Intent("android.intent.action.DISPLAY_STATUSBAR"));//点击隐藏按钮时发送广播通知隐藏工具栏
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                ToastUtils.showShort(MainActivity.this,getResources().getString(R.string.cannotexit));
                return true;
            case KeyEvent.KEYCODE_BACK:
                ToastUtils.showShort(MainActivity.this,getResources().getString(R.string.cannotexit));
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        sendBroadcast(new Intent("android.intent.action.HIDE_STATUSBAR"));//点击显示按钮时发送广播通知显示工具栏
        super.onPause();
    }

    @Override
    protected void onResume() {
        sendBroadcast(new Intent("android.intent.action.DISPLAY_STATUSBAR"));//点击隐藏按钮时发送广播通知隐藏工具栏
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        sendBroadcast(new Intent("android.intent.action.HIDE_STATUSBAR"));//点击显示按钮时发送广播通知显示工具栏
        super.onDestroy();
    }
}
