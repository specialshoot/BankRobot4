package com.champion.bankrobot4;

import android.app.Application;
import android.util.DisplayMetrics;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * Created by 轾 on 2016/5/15.
 */
public class SpeechApp extends android_serialport_api.Application {

    private static SpeechApp mApplication;
    private DisplayMetrics displayMetrics = null;

    public SpeechApp(){
        mApplication = this;
    }

    public static SpeechApp getApp() {
        if (mApplication != null && mApplication instanceof SpeechApp) {
            return (SpeechApp) mApplication;
        } else {
            mApplication = new SpeechApp();
            mApplication.onCreate();
            return (SpeechApp) mApplication;
        }
    }

    public float getScreenDensity() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.density;
    }

    public int getScreenHeight() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.heightPixels;
    }

    public int getScreenWidth() {
        if (this.displayMetrics == null) {
            setDisplayMetrics(getResources().getDisplayMetrics());
        }
        return this.displayMetrics.widthPixels;
    }

    public void setDisplayMetrics(DisplayMetrics DisplayMetrics) {
        this.displayMetrics = DisplayMetrics;
    }

    public int dp2px(float f)
    {
        return (int)(0.5F + f * getScreenDensity());
    }

    public int px2dp(float pxValue) {
        return (int) (pxValue / getScreenDensity() + 0.5f);
    }

    @Override
    public void onCreate(){
        super.onCreate();
        mApplication = this;
        initAction();
    }

    private void initAction(){
        //语音设置
        StringBuffer param = new StringBuffer();
        param.append("appid=" + getString(R.string.iflytek_id));
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE + "=" + SpeechConstant.MODE_MSC);
        System.out.println("param -> " + param.toString());
        SpeechUtility.createUtility(SpeechApp.this, param.toString());//科大讯飞
    }

    //获取应用的data/data/....File目录
    public String getFilesDirPath() {
        return getFilesDir().getAbsolutePath();
    }

    //获取应用的data/data/....Cache目录
    public String getCacheDirPath() {
        return getCacheDir().getAbsolutePath();
    }
}
