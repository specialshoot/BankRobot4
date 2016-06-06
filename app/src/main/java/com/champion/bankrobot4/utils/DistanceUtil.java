package com.champion.bankrobot4.utils;

import com.champion.bankrobot4.SpeechApp;

public class DistanceUtil {

    public static int getCameraAlbumWidth() {
        return (SpeechApp.getApp().getScreenWidth() - SpeechApp.getApp().dp2px(10)) / 4 - SpeechApp.getApp().dp2px(4);
    }
    
    // 相机照片列表高度计算 
    public static int getCameraPhotoAreaHeight() {
        return getCameraPhotoWidth() + SpeechApp.getApp().dp2px(4);
    }
    
    public static int getCameraPhotoWidth() {
        return SpeechApp.getApp().getScreenWidth() / 4 - SpeechApp.getApp().dp2px(2);
    }

    //活动标签页grid图片高度
    public static int getActivityHeight() {
        return (SpeechApp.getApp().getScreenWidth() - SpeechApp.getApp().dp2px(24)) / 3;
    }
}
