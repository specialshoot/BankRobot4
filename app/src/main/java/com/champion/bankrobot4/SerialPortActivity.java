package com.champion.bankrobot4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import android_serialport_api.Application;
import android_serialport_api.SerialPort;

public abstract class SerialPortActivity extends Activity {

    protected Application mApplication;
    protected SerialPort mSerialPort;
    protected OutputStream mOutputStream;
    private InputStream mInputStream;
    private ReadThread mReadThread;
    protected SerialPort mSerialPortZeng;
    protected OutputStream mOutputStreamZeng;
    private InputStream mInputStreamZeng;
    private ReadThreadZeng mReadThreadZeng;

    /**
     * 读取流中的数据
     */
    private class ReadThread extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStream == null) return;
                    size = mInputStream.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    /**
     * 读取流中的数据
     */
    private class ReadThreadZeng extends Thread {

        @Override
        public void run() {
            super.run();
            while (!isInterrupted()) {
                int size;
                try {
                    byte[] buffer = new byte[64];
                    if (mInputStreamZeng == null) return;
                    size = mInputStreamZeng.read(buffer);
                    if (size > 0) {
                        onDataReceived(buffer, size);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
//                SerialPortActivity.this.finish();
                mOutputStream=null;
                mOutputStreamZeng=null;
                mInputStream=null;
                mInputStreamZeng=null;
            }
        });
        b.show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (SpeechApp) getApplication();
        try {
            mSerialPort = mApplication.getSerialPort();
            mSerialPortZeng = mApplication.getSerialPortZeng();
            mOutputStream = mSerialPort.getOutputStream();
            mOutputStreamZeng = mSerialPortZeng.getOutputStream();
            mInputStream = mSerialPort.getInputStream();
            mInputStreamZeng = mSerialPortZeng.getInputStream();

			/* Create a receiving thread
             * 数据接收 */
            mReadThread = new ReadThread();
            mReadThread.start();
            mReadThreadZeng=new ReadThreadZeng();
            mReadThreadZeng.start();
        } catch (SecurityException e) {
            DisplayError(R.string.error_security);
        } catch (IOException e) {
            DisplayError(R.string.error_unknown);
        } catch (InvalidParameterException e) {
            DisplayError(R.string.error_configuration);
        }
    }

    protected abstract void onDataReceived(final byte[] buffer, final int size);

    @Override
    protected void onDestroy() {
        if (mReadThread != null)
            mReadThread.interrupt();
        if (mReadThreadZeng != null)
            mReadThreadZeng.interrupt();
        mApplication.closeSerialPort();
        mSerialPort = null;
        mSerialPortZeng = null;
        super.onDestroy();
    }
}
