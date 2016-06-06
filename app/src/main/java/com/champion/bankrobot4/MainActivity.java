package com.champion.bankrobot4;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import com.champion.bankrobot4.utils.ToastUtils;
import com.champion.bankrobot4.view.CircleMenuLayout;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortActivity;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends SerialPortActivity {

    @BindView(R.id.id_menulayout)
    CircleMenuLayout mCircleMenuLayout;
    //Serial Port
//    protected android_serialport_api.Application mApplication;
    protected SpeechApp mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
//    private InputStream mInputStream;
//    private ReadThread mReadThread;

    private String[] mItemTexts = new String[]{"视频播放", "画廊", "简介", "聊天"};
    private int[] mItemImgs = new int[]{R.drawable.film, R.drawable.picture,
            R.drawable.introduce, R.drawable.bubbles};

    /**
     * 读取流中的数据
     */
//    private class ReadThread extends Thread {
//
//        @Override
//        public void run() {
//            super.run();
//            while (!isInterrupted()) {
//                int size;
//                try {
//                    byte[] buffer = new byte[64];
//                    if (mInputStream == null) return;
//                    size = mInputStream.read(buffer);
//                    if (size > 0) {
//                        onDataReceived(buffer, size);
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    return;
//                }
//            }
//        }
//    }
    protected void onDataReceived(final byte[] buffer, final int size) {
        //处理收到的数据
//        runOnUiThread(new Runnable() {
//            public void run() {
//                ToastUtils.showShort(MainActivity.this, new String(buffer, 0, size));
//                System.out.println(new String(buffer, 0, size));
//            }
//        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {
        mApplication = (SpeechApp) getApplication();
        try {
            mSerialPort = mApplication.getSerialPort();
            mOutputStream = mSerialPort.getOutputStream();
//            mInputStream = mSerialPort.getInputStream();

			/* Create a receiving thread
             * 数据接收 */
//            mReadThread = new ReadThread();
//            mReadThread.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }

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

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ToastUtils.showShort(MainActivity.this, "串口打开失败");
            }
        });
        b.show();
    }
}
