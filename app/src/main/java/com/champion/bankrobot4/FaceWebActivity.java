package com.champion.bankrobot4;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;

import com.champion.bankrobot4.utils.ToastUtils;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class FaceWebActivity extends AppCompatActivity {

    private final String TAG="SerialPortTAG";
    @BindView(R.id.webView)
    WebView webView;
    @BindView(R.id.facechatio)
    TextView chat_io;
    private String url = "http://192.168.199.112:3000/";

    //语音合成相关参数Tts
    private SpeechSynthesizer mTts;//语音合成对象
    public static String voicer = "xiaoyan";//默认发言人
    private int mPercentForBuffering = 0;//缓冲进度
    private int mPercentForPlaying = 0;//播放速度
    private SharedPreferences mTtsSharedPreferences;//语音合成sharedpreferences

    //SocketIO
    private Socket mSocket;
    private Boolean isConnected = true;
    private boolean mTyping = false;
    private String mUsername = "robot1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_face_web);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {
        sendBroadcast(new Intent("android.intent.action.DISPLAY_STATUSBAR"));//点击隐藏按钮时发送广播通知隐藏工具栏
        webView.loadUrl(url);
        WebSettings webSettings = webView.getSettings();
        webSettings.setUseWideViewPort(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setJavaScriptEnabled(true);//支持js
        webView.setWebViewClient(new MyWebViewClient());

        /*****************************语音合成初始化*****************************/
        mTts = SpeechSynthesizer.createSynthesizer(this, initListener);
        mTtsSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);

        //SocketIO初始化
        SpeechApp app = (SpeechApp) getApplication();
        mSocket = app.getSocket();
        mSocket.on(Socket.EVENT_CONNECT, onConnect);
        mSocket.on(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.on(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.on("login", onLogin);
        mSocket.on("new message", onNewMessage);
        mSocket.on("user joined", onUserJoined);
        mSocket.on("user left", onUserLeft);
        mSocket.on("typing", onTyping);
        mSocket.on("stop typing", onStopTyping);
        mSocket.connect();
        attemptLogin();
    }

    /**
     * 初始化语音的监听
     */
    private InitListener initListener = new InitListener() {
        @Override
        public void onInit(int code) {

            Log.d(TAG, "init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                Log.v(TAG, "初始化失败，错误码：" + code);
            } else {
                // 如果有语音合成,初始化成功,之后可以调用startSpeaking方法
                // 注:有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成
                // 正确的做法是将onCreate中的startSpeaking调用移至这里
            }
        }
    };

    /**
     * 设置语音合成参数
     */
    private void setTtsParam() {
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
//        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
//            //设置使用云端引擎
//            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
//            //设置发音人
//            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
//        } else {
        //设置使用本地引擎
        mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
        //设置发音人资源路径
        mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
        //设置发音人
        mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
//        }
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, mTtsSharedPreferences.getString("speed_preference", "50"));
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, mTtsSharedPreferences.getString("pitch_preference", "50"));
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, mTtsSharedPreferences.getString("volume_preference", "50"));
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, mTtsSharedPreferences.getString("stream_preference", "3"));

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/tts.wav");
    }

    /**
     * 获取发音人资源路径
     *
     * @return
     */
    private String getResourcePath() {
        StringBuffer tempBuffer = new StringBuffer();
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + ChatActivity.voicer + ".jet"));
        return tempBuffer.toString();
    }

    /**
     * 语音播报
     *
     * @param msg 语音播报的内容
     */
    private void DoSpeak(String msg) {
        Message message = handlervoice.obtainMessage();
        message.obj = msg;
        handlervoice.sendMessage(message);
    }

    private Handler handlervoice = new Handler() {  //语音播放handler
        @Override
        public void handleMessage(Message msg) {
            String detail = (String) msg.obj;
            setTtsParam();
            int code = mTts.startSpeaking(detail, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showShort(FaceWebActivity.this, "语音合成失败,错误码: " + code);
            }
        }
    };

    /**
     * 语音合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
        }

        @Override
        public void onSpeakPaused() {
        }

        @Override
        public void onSpeakResumed() {
        }

        @Override
        public void onBufferProgress(int percent, int beginPos, int endPos,
                                     String info) {
            // 合成进度
            mPercentForBuffering = percent;
//            showToast(String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onSpeakProgress(int percent, int beginPos, int endPos) {
            // 播放进度
            mPercentForPlaying = percent;
        }

        @Override
        public void onCompleted(SpeechError error) {
            if (error == null) {
                //showToast("播放完成");
            } else if (error != null) {
            }
            attemptSend(mUsername + " SPEAK FINISH");
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
//            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
//                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
//                Log.d(TAG, "session id =" + sid);
//            }
        }
    };

    /**
     * 当用户点击webview中的网页链接的时候，安卓系统默认会启动一个新的应用专门成立url的跳转。
     * 如果希望点击链接继续在当前webview中响应,而不是新开Android的系统browser中响应该链接,必须覆盖 WebView的WebViewClient对象.
     * 并重写shouldOverrideUrlLoading方法。
     */
    private class MyWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {

            if (Uri.parse(url).getHost().equals("www.example.com")) {
                // This is my web site, so do not override; let my WebView load the page
                return false;
            }
            // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
            return true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // Check if the key event was the Back button and if there's history
        // 返回上一级菜单
        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
            webView.goBack();
            return true;
        }
        // If it wasn't the Back key or there's no web page history, bubble up to the default
        // system behavior (probably exit the activity)
        return super.onKeyDown(keyCode, event);
    }

    @OnClick(R.id.webBack)
    void back() {
        this.finish();
    }

    @OnClick(R.id.testSpeak)
    void speak(){
        DoSpeak("呵呵呵呵");
    }

    private Emitter.Listener onLogin = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            Log.i(TAG, "Enter onLogin");
            JSONObject data = (JSONObject) args[0];

            int numUsers;
            try {
                numUsers = data.getInt("numUsers");
            } catch (JSONException e) {
                return;
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    chat_io.setText("Login");
                }
            });
            Log.i("onLogin", "onLogin");
            attemptSend(mUsername + " onLogin FINISH .");
        }
    };

    private Emitter.Listener onConnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Enter onConnect");
                    if (!isConnected) {
                        if (null != mUsername) {
                            mSocket.emit("add user", mUsername);
                        }
                        ToastUtils.showShort(getApplicationContext(),
                                R.string.connect);
                        isConnected = true;
                        chat_io.setText("连接成功");
//                        attemptLogin();

                        Log.i("onConnect", "onConnect");
                    }
                }
            });
        }
    };

    private Emitter.Listener onDisconnect = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.i(TAG, "Enter onDisconnect");
                    isConnected = false;
                    ToastUtils.showShort(getApplicationContext(),
                            R.string.disconnect);
                    chat_io.setText("未连接成功，请检查网络");
                    Log.i("onDisconnect", "onDisconnect");
                }
            });
        }
    };

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showShort(getApplicationContext(),
                            R.string.error_connect);
                    Log.i(TAG, "connect error");
                    chat_io.setText("连接错误");
                }
            });
        }
    };

    private Emitter.Listener onNewMessage = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    String message;
                    try {
                        username = data.getString("username");
                        message = data.getString("message");
                    } catch (JSONException e) {
                        return;
                    }
                    if(username.equals("robot_cv_ui") && !message.isEmpty()){
                        DoSpeak(message);
                    }
                }
            });
        }
    };

    private Emitter.Listener onUserJoined = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    Log.i("onUserJoined", username + " : " + numUsers);
                }
            });
        }
    };

    private Emitter.Listener onUserLeft = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    int numUsers;
                    try {
                        username = data.getString("username");
                        numUsers = data.getInt("numUsers");
                    } catch (JSONException e) {
                        return;
                    }
                    Log.i("onUserLeft", username + " : " + numUsers);
                }
            });
        }
    };

    private Emitter.Listener onTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    Log.i("onTyping", username);
                }
            });
        }
    };

    private Emitter.Listener onStopTyping = new Emitter.Listener() {
        @Override
        public void call(final Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    JSONObject data = (JSONObject) args[0];
                    String username;
                    try {
                        username = data.getString("username");
                    } catch (JSONException e) {
                        return;
                    }
                    Log.i("onStopTyping", username);
                }
            });
        }
    };

    private void attemptSend(String message) {
        if (null == mUsername) return;
        if (!mSocket.connected()) return;

        mTyping = false;

        // perform the sending message attempt.
        mSocket.emit("new message", message);
    }

    private Runnable onTypingTimeout = new Runnable() {
        @Override
        public void run() {
            if (!mTyping) {
                return;
            }
            mTyping = false;
            mSocket.emit("stop typing");
        }
    };

    private void attemptLogin() {
        // perform the user login attempt.
        mSocket.emit("add user", mUsername);
        Log.i(TAG, "attempLogin");
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mTts != null && mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mTts != null) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
            mTts = null;
        }
        //SocketIO
        mSocket.disconnect();
        mSocket.off(Socket.EVENT_CONNECT, onConnect);
        mSocket.off(Socket.EVENT_DISCONNECT, onDisconnect);
        mSocket.off(Socket.EVENT_CONNECT_ERROR, onConnectError);
        mSocket.off(Socket.EVENT_CONNECT_TIMEOUT, onConnectError);
        mSocket.off("login", onLogin);
        mSocket.off("new message", onNewMessage);
        mSocket.off("user joined", onUserJoined);
        mSocket.off("user left", onUserLeft);
        mSocket.off("typing", onTyping);
        mSocket.off("stop typing", onStopTyping);
    }

}
