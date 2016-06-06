package com.champion.bankrobot4;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Movie;
import android.media.MediaPlayer;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.champion.bankrobot4.utils.ToastUtils;
import com.champion.bankrobot4.view.videoview.UniversalMediaController;
import com.champion.bankrobot4.view.videoview.UniversalVideoView;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MovieActivity extends AppCompatActivity implements UniversalVideoView.VideoViewCallback,MediaPlayer.OnCompletionListener {

    private static final String TAG = "MovieActivity";
    private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";
    private String VIDEO_URL = "";
    public static final int VideoCode = 11;

    private int mSeekPosition;
    private int cachedHeight;
    private boolean isFullscreen;
    private String mCurrentURI = "";
    private ArrayList<String> videoList;
    private int CurrentVideoNum = 0;
    private int TotalVideoNum = 0;

    @BindView(R.id.video_layout)
    View mVideoLayout;
    @BindView(R.id.videoView)
    UniversalVideoView mVideoView;
    @BindView(R.id.media_controller)
    UniversalMediaController mMediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {
        VIDEO_URL = getSDPath() + "/Movies/test.mp4";
        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
        mVideoView.setOnCompletionListener(this);
        mVideoView.setFullscreen(true);
        mMediaController.show();
        try {
            mVideoView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getSDPath() {
        File sdDir = null;
        boolean sdCardExist = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);   //判断sd卡是否存在
        if (sdCardExist) {//如果SD卡存在，则获取跟目录
            sdDir = Environment.getExternalStorageDirectory();//获取跟目录
        } else {
            Log.d(TAG, "没有检测到SD卡");
        }
        return sdDir.toString();

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause ");
        if (mVideoView != null && mVideoView.isPlaying()) {
            mSeekPosition = mVideoView.getCurrentPosition();
            Log.d(TAG, "onPause mSeekPosition=" + mSeekPosition);
            mVideoView.pause();
        }
    }

    /**
     * 置视频区域大小
     */
    private void setVideoAreaSize() {
        mVideoView.setVideoPath(VIDEO_URL);
        mVideoView.requestFocus();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
        outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
    }

    @Override
    protected void onRestoreInstanceState(Bundle outState) {
        super.onRestoreInstanceState(outState);
        mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
        Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
    }

    @Override
    public void onScaleChange(boolean isFullscreen) {
//        this.isFullscreen = isFullscreen;
//        if (isFullscreen) {
        ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        mVideoLayout.setLayoutParams(layoutParams);

//        } else {
//            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
//            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
//            layoutParams.height = this.cachedHeight;
//            mVideoLayout.setLayoutParams(layoutParams);
//        }

        switchTitleBar(!isFullscreen);
    }

    private void switchTitleBar(boolean show) {
        android.support.v7.app.ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

    @Override
    public void onPause(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onPause UniversalVideoView callback");
    }

    @Override
    public void onStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingStart(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingStart UniversalVideoView callback");
    }

    @Override
    public void onBufferingEnd(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onBufferingEnd UniversalVideoView callback");
    }

    @Override
    public void onMenu() {
        ToastUtils.showShort(MovieActivity.this, "请选择播放文件");
        Intent intent = new Intent(MovieActivity.this, VideoChooseActivity.class);
        startActivityForResult(intent, VideoCode);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMediaController.hideComplete();
        try {
            mVideoView.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBack() {
        this.finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case VideoCode:
                final String mCurrentDir = data.getExtras().getString("dir");
                videoList = data.getStringArrayListExtra("video");
                TotalVideoNum = videoList.size();
                System.out.println("当前目录:" + mCurrentDir);
                for (int i = 0; i < TotalVideoNum; i++) {
                    System.out.println(videoList.get(i));
                }
                if (videoList != null && !mCurrentDir.equals("")) {
                    try {
                        if (mVideoView != null) {
                            if (videoList.isEmpty()) {
                                mVideoView.pause();
                            } else {
                                CurrentVideoNum = 0;
                                String url = videoList.get(CurrentVideoNum).toString();
                                int position = url.lastIndexOf("/");  //记录最后一次/出现的位置,方便提取文件名
                                mCurrentURI = url;
                                mVideoView.setVideoPath(mCurrentURI);
                                mVideoView.requestFocus();
                                mVideoView.start();
                                mMediaController.setTitle(url);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                break;
            default:
                break;
        }
    }
}
