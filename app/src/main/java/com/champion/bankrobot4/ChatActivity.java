package com.champion.bankrobot4;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.champion.bankrobot4.adapter.ChatAdapter;
import com.champion.bankrobot4.utils.ApkInstaller;
import com.champion.bankrobot4.utils.FucUtil;
import com.champion.bankrobot4.utils.JsonParser;
import com.champion.bankrobot4.utils.ToastUtils;
import com.champion.bankrobot4.view.RippleBackground;
import com.champion.bankrobot4.view.WebDialog;
import com.champion.bankrobot4.view.chat.ChatMsgEntity;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.VoiceWakeuper;
import com.iflytek.cloud.WakeuperListener;
import com.iflytek.cloud.WakeuperResult;
import com.iflytek.cloud.util.ResourceUtil;
import com.wangjie.androidbucket.utils.ABTextUtil;
import com.wangjie.androidbucket.utils.imageprocess.ABShape;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class ChatActivity extends SerialPortActivity implements View.OnClickListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {

    private String TAG = "ChatActivity";
    public final static int CAMERANUM = 1;
    List<ChatMsgEntity> data;
    ChatAdapter chatAdapter;
    String robot_name = "Robot";
    String me_name = "Me";
    AnimationDrawable animation = null;
    String hello = "大家好，我是荷福创品机器人小白，欢迎大家来参观。我可以陪您聊天，我还可以告诉您天气、新闻等生活常用信息，是您生活的好助手。我还具有导航壁障的功能，可以在未知环境下建图导航壁障，下面我将展示给大家";
    final String[] motion = {"A", "B", "C", "D", "E"};//A握手B握手复位C再见D再见复位E摇头
    final String[] motiondesc = {"", "握手", "再见", "业务介绍", "朝左指", "朝右指", "加油", "迎宾", "走路", "走路准备", "手臂位置初始化", "抱抱", "颁奖", "牵线左", "牵线右", "向左看", "向右看"};
    final String introduce =
            "现在的我，可以承担银行内自助机的全部功能，同时在银行大堂内自主移动，自主作业，根据空间定位导航，自主避开障碍物选择最优路径达到目的地。我还能完成银行大堂经理的引导功能，当客户站在我面前时，直接跟他对话；我会根据客户的需求，将客户准确带到相关柜台办理业务。不仅如此，我还能自动识别用户脸部，精准推送相关业务功能，遇到外籍用户时还能临时当做机器人翻译。今后，我还会根据主人需要，开发更丰富的服务功能，真正实现我作为小助手的人生目标。";
    final String nongshang = "成都农村商业银行股份有限公司2009年12月经中国银行业监督管理委员会批准设立，已发展成为中国最大的农村商业银行之一，总部设在成都。在山东、江苏、福建、河北、四川、云南、新疆等地发起设立的39家中成村镇银行全部开业。多年来，成都农村商业银行股份有限公司坚持以服务“三农”、中小企业和地方经济建设为使命，不断开拓创新，深化改革，经营实力逐步增强，发展步伐不断加快。在英国《银行家》杂志2015年发布的全球1000家大银行中排名237位。";
    final String POST1 = "$POSTURE,1*";
    final String POST2 = "$POSTURE,2*";
    final String POST3 = "$POSTURE,3*";
    final String POST4 = "$POSTURE,4*";
    final String POST5 = "$POSTURE,5*";
    final String POST6 = "$POSTURE,6*";
    final String POST7 = "$POSTURE,7*";
    final String POST8 = "$POSTURE,8*";
    final String POST9 = "$POSTURE,9*";
    final String POST10 = "$POSTURE,10*";
    final String POST11 = "$POSTURE,11*";
    final String POST12 = "$POSTURE,12*";
    final String POST13 = "$POSTURE,13*";
    final String POST14 = "$POSTURE,14*";
    final String POST15 = "$POSTURE,15*";
    final String POST16 = "$POSTURE,16*";
    private boolean hasPeople = false;
    final String SF = "TTS_FINISH";

    private View main;
    @BindView(R.id.lv_chat)
    ListView lv_chat;
    @BindView(R.id.chat_checklistener)
    ImageView checker;
    @BindView(R.id.chat_test)
    ImageView chat_voice;
    @BindView(R.id.chat_animation)
    ImageView iv_animation;
    @BindView(R.id.voice_layout)
    View voice_layout;
    @BindView(R.id.chat_io)
    TextView chat_io;
    @BindView(R.id.chat_ripple)
    RippleBackground rippleBackground;
    @BindView(R.id.EditTextReception)
    TextView mReception;
    @BindView(R.id.activity_main_rfal)
    RapidFloatingActionLayout rfaLayout;
    @BindView(R.id.activity_main_rfab)
    RapidFloatingActionButton rfaButton;
    private RapidFloatingActionHelper rfabHelper;

    private int noSpeechCount = 0;//无语音输入计数
    private boolean ManMode = true;//是否调整为手动模式

    //SocketIO
    private Socket mSocket;
    private Boolean isConnected = true;
    private boolean mTyping = false;
    private String mUsername = "robot1";

    //串口
    String receiveString = "";
    private String serialResult = "";
    //科大语音
    private String speechResult = "";
    //科大讯飞相应变量
    private String mEngineTypeLocal = SpeechConstant.TYPE_LOCAL; //语音类型,自动(用于语音听写、语音合成)
    private String mEngineTypeCloud = SpeechConstant.TYPE_CLOUD; //语音类型,云端(用于语音听写、语音合成)
    private String mEngineTypeMix = SpeechConstant.TYPE_MIX; //语音类型,混合(用于语音听写、语音合成)
    //语音听写相关参数Iat
    SpeechRecognizer mIat;    //语音听写对象
    //RecognizerDialog mIatDialog;  //语音听写对话框
    private SharedPreferences mIatSharedPreferences;//语音听写sharedpreferences
    private int isPuTongHua = 0;
    //语音合成相关参数Tts
    private SpeechSynthesizer mTts;//语音合成对象
    public static String voicer = "xiaoyan";//默认发言人
    private int mPercentForBuffering = 0;//缓冲进度
    private int mPercentForPlaying = 0;//播放速度
    private SharedPreferences mTtsSharedPreferences;//语音合成sharedpreferences
    // 语记安装助手类
    ApkInstaller mInstaller;

    //语音唤醒相关参数
    private VoiceWakeuper mIvw;
    private String resultString;//语音唤醒结果内容
    public static int curThresh = 10;    //唤醒门限值
    private Handler handlervoice = new Handler() {  //语音播放handler
        @Override
        public void handleMessage(Message msg) {
            String detail = (String) msg.obj;
            setTtsParam();
            int code = mTts.startSpeaking(detail, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showShort(ChatActivity.this, "语音合成失败,错误码: " + code);
            }
        }
    };

    private Handler handlerturing = new Handler() {  //处理图灵
        @Override
        public void handleMessage(Message msg) {
            ChatMsgEntity chatMsgEntity = (ChatMsgEntity) msg.obj;
            data.add(chatMsgEntity);
            while (data.size() >= 10) {
                data.remove(0);
            }
            chatAdapter.notifyDataSetChanged();
            lv_chat.setSelection(data.size() - 1);
            String text = chatMsgEntity.getMsg();
            text = getMsgText(text);
//            setParamSay();
            String tempText = text.replaceAll("br", ",");
            int code = mTts.startSpeaking(text, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showShort(ChatActivity.this, "语音合成失败,错误码: " + code);
            }
        }
    };

    private void replyMsgHandler(String msg) {
        ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, msg, ChatMsgEntity.Type.you, new Date());
        Message message = handlerturing.obtainMessage();
        message.obj = chatMsgEntity;
        handlerturing.sendMessage(message);
    }

    private String getMsgText(String msg) {
        try {
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg);
            String text = jsonObject.getString("text");
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    public void onClick(View v) {
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    }

    /**
     * 串口收到的数据
     *
     * @param buffer
     * @param size
     */
    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        // ignore incoming data
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                serialResult += new String(buffer, 0, size);
//                attemptSend("Received From Serialport -> " + serialResult);
//
//                if (serialResult.contains("SUCCESS!")) {
//                    mReception.setText(serialResult);
//                    serialResult = "";
//                } else {
//                    mReception.setText("");
//                }
            }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        main = getLayoutInflater().from(this).inflate(R.layout.activity_chat, null);
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        main.setOnClickListener(this);
        setContentView(main);
        ButterKnife.bind(this);
        initAction();
    }

    private void initAction() {
        sendBroadcast(new Intent("android.intent.action.DISPLAY_STATUSBAR"));//点击隐藏按钮时发送广播通知隐藏工具栏
        //初始化聊天数据
        data = new ArrayList<ChatMsgEntity>();
        ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix("您好"), ChatMsgEntity.Type.you, new Date());
        data.add(chatMsgEntity);
        chatAdapter = new ChatAdapter(this, data);
        lv_chat.setAdapter(chatAdapter);

        setRfa();
        /*****************************语音听写初始化*****************************/
        /**
         * 选择本地听写 判断是否安装语记,未安装则跳转到提示安装页面
         */
        mInstaller = new ApkInstaller(ChatActivity.this);
        if (!SpeechUtility.getUtility().checkServiceInstalled()) {
            mInstaller.install();
        } else {
            String result = FucUtil.checkLocalResource();
            if (!TextUtils.isEmpty(result)) {
                ToastUtils.showShort(ChatActivity.this, result);
            }
        }
        //初始化语音听写
        mIat = com.iflytek.cloud.SpeechRecognizer.createRecognizer(this, initListener);
        //初始化语音听写对话框
        //mIatDialog = new RecognizerDialog(this, initListener);
        mIatSharedPreferences = getSharedPreferences(IatSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        /*****************************语音合成初始化*****************************/
        mTts = SpeechSynthesizer.createSynthesizer(this, initListener);
        mTtsSharedPreferences = getSharedPreferences(TtsSettings.PREFER_NAME, Activity.MODE_PRIVATE);
        /*****************************语音唤醒初始化*****************************/
        // 初始化唤醒对象
        mIvw = VoiceWakeuper.createWakeuper(this, null);
//        DoWake();   //唤醒打开
        //DoSpeak(hello);

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

    private void setRfa() {

        RapidFloatingActionContentLabelList rfaContent = new RapidFloatingActionContentLabelList(ChatActivity.this);
        rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
        List<RFACLabelItem> items = new ArrayList<>();
        items.add(new RFACLabelItem<Integer>()
                .setLabel("普通话")
                .setResId(R.mipmap.ic_github)
                .setIconNormalColor(0xffd84315)
                .setIconPressedColor(0xffbf360c)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaad84315, ABTextUtil.dip2px(ChatActivity.this, 4)))
                .setWrapper(0)
        );
        items.add(new RFACLabelItem<Integer>()
                        .setLabel("粤语")
                        .setResId(R.mipmap.ic_github)
//                        .setDrawable(getResources().getDrawable(R.mipmap.ico_test_c))
                        .setIconNormalColor(0xff4e342e)
                        .setIconPressedColor(0xff3e2723)
                        .setLabelColor(Color.WHITE)
                        .setLabelSizeSp(14)
                        .setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(ChatActivity.this, 4)))
                        .setWrapper(1)
        );

        items.add(new RFACLabelItem<Integer>()
                        .setLabel("English")
                        .setResId(R.mipmap.ic_github)
//                        .setDrawable(getResources().getDrawable(R.mipmap.ico_test_c))
                        .setIconNormalColor(0xff4e342e)
                        .setIconPressedColor(0xff3e2723)
                        .setLabelColor(Color.WHITE)
                        .setLabelSizeSp(14)
                        .setLabelBackgroundDrawable(ABShape.generateCornerShapeDrawable(0xaa000000, ABTextUtil.dip2px(ChatActivity.this, 4)))
                        .setWrapper(2)
        );

        rfaContent.setItems(items)
                .setIconShadowRadius(ABTextUtil.dip2px(ChatActivity.this, 5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(ABTextUtil.dip2px(ChatActivity.this, 5));

        rfabHelper = new RapidFloatingActionHelper(ChatActivity.this, rfaLayout, rfaButton, rfaContent).build();
    }

    private void DisplayError(int resourceId) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Error");
        b.setMessage(resourceId);
        b.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ChatActivity.this.finish();
            }
        });
        b.show();
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
            attemptSend(mUsername + " onLogin FINISH . Status : " + (ManMode ? "手动" : "自动"));
            attemptSend(SF);
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

                    if (username.equals("FACE") && message.length() > 3 && message.substring(0, 3).equals("@@@")) {
                        message = message.substring(3);
                        try {
                            String[] faceInfo = message.split(",");
                            int x = Integer.parseInt(faceInfo[0]);
                            int y = Integer.parseInt(faceInfo[1]);
                            int w = Integer.parseInt(faceInfo[2]);
                            int h = Integer.parseInt(faceInfo[3]);
                            int centerx = x + w / 2;
                            if (w > 45) {
                                if (w > 150 && !hasPeople) {
                                    hasPeople = true;
                                    faceposition(320);
                                    if (mTts != null && !mTts.isSpeaking() && mIat != null && !mIat.isListening() && noSpeechCount == 0) {
                                        DoSpeak("你好啊");
                                    }
                                }
                                faceposition(centerx);
//                                else {
//                                    hasPeople = false;
//                                    faceposition(centerx);
//                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return;
                    }
                    //处理message
                    Log.i("onNewMessage", username + " : " + message);
                    if (!(username.equals("A") || username.equals("ifly"))) {
                        return;
                    }
                    attemptSend(mUsername + "收到了" + message + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
                    if (message.equals("exit")) {
                        finish();
                        return;
                    }

                    if (message.equals("clear")) {
                        serialResult = "";
                        return;
                    }

                    if (message.length() > 3 && message.substring(0, 3).equals("+++")) {
                        try {
                            if (mOutputStream != null) {
                                String command = message.substring(3);
                                mOutputStream.write(command.getBytes());
//                                attemptSend(mUsername + "Send Command Success ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
                            }
                        } catch (IOException e) {
//                            attemptSend(mUsername + "Send Command Failed ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
                            e.printStackTrace();
                        }
                        return;
                    }

                    if (message.equals("%%%")) {
                        ManMode = true;
                        attemptSend(mUsername + " 改变状态为 : " + (ManMode ? "手动" : "自动") + " FINISH");
                        return;
                    }
                    if (message.equals("###")) {
                        ManMode = false;
                        attemptSend(mUsername + " 改变状态为 : " + (ManMode ? "手动" : "自动") + " FINISH");
                        return;
                    }
                    if (message.equals("***")) {
                        attemptSend(mUsername + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
                        return;
                    }
                    if (message.equals(POST1)) {
                        Log.v(TAG, "enter hand");
                        DoSpeak("您好,祝您生活愉快");
                        if (mSerialPort != null) {
                            dance(1);
                        }
                        return;
                    }
                    if (message.equals(POST2)) {
                        ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix("再见，欢迎您下次再来"), ChatMsgEntity.Type.you, new Date());
                        Message message1 = handler.obtainMessage();
                        message1.obj = chatMsgEntity;
                        handler.sendMessage(message1);
                        DoSpeak("再见，欢迎您下次再来");
                        if (mSerialPort != null) {
                            dance(2);
                        }
                        return;
                    }
                    if (message.equals(POST3)) {
                        ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix(introduce), ChatMsgEntity.Type.you, new Date());
                        Message message2 = handler.obtainMessage();
                        message2.obj = chatMsgEntity;
                        handler.sendMessage(message2);
                        if (mSerialPort != null) {
                            dance(3);
                        }
                        DoSpeak(introduce);
                        return;
                    }
                    if (message.equals(POST4)) {
                        if (mSerialPort != null) {
                            dance(4);
                        }
                        return;
                    }
                    if (message.equals(POST5)) {
                        if (mSerialPort != null) {
                            dance(5);
                        }
                        return;
                    }
                    if (message.equals(POST6)) {
                        if (mSerialPort != null) {
                            dance(6);
                        }
                        return;
                    }
                    if (message.equals(POST7)) {
                        if (mSerialPort != null) {
                            dance(7);
                        }
                        return;
                    }
                    if (message.equals(POST8)) {
                        if (mSerialPort != null) {
                            dance(8);
                        }
                        return;
                    }
                    if (message.equals(POST9)) {
                        if (mSerialPort != null) {
                            dance(9);
                        }
                        return;
                    }
                    if (message.equals(POST10)) {
                        if (mSerialPort != null) {
                            dance(10);
                        }
                        return;
                    }
                    if (message.equals(POST11)) {
                        if (mSerialPort != null) {
                            dance(11);
                        }
                        return;
                    }
                    if (message.equals(POST12)) {
                        if (mSerialPort != null) {
                            dance(12);
                        }
                        return;
                    }
                    if (message.equals(POST13)) {
                        if (mSerialPort != null) {
                            dance(13);
                        }
                        return;
                    }
                    if (message.equals(POST14)) {
                        if (mSerialPort != null) {
                            dance(14);
                        }
                        return;
                    }

                    if (message.equals(POST15)) {
                        if (mSerialPort != null) {
                            dance(15);
                        }
                        return;
                    }
                    if (message.equals(POST16)) {
                        if (mSerialPort != null) {
                            dance(16);
                        }
                        return;
                    }

                    if (message.contains("FINISH")) {
                        return;
                    }
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix(message), ChatMsgEntity.Type.you, new Date());
                    Message back = handler.obtainMessage();
                    back.obj = chatMsgEntity;
                    handler.sendMessage(back);
                    DoSpeak(message);
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

    private String sayNoPrefix(String s) {
        return "{\"code\":100000,\"text\":\"" + s + "\"}";
    }

    /********************
     * 按下后说话识别
     ******************/
    @OnClick(R.id.voice_layout)
    void chat_test() {
        hasPeople = true;
        try {
            if (mTts.isSpeaking()) {
                mTts.stopSpeaking();
            }
            if (mIat.isListening()) {
                mIat.cancel();
            }
            noSpeechCount = 0;
            DoListener();   //语音识别
        } catch (Exception e) {
            e.printStackTrace();
            setFabClickable();
        }
    }

    @OnClick(R.id.hello_layout)
    void chat_hello() {
        main.setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
//        DoSpeak(hello);
        attemptSend(SF);
    }

    @OnClick(R.id.serial_layout)
    void send_RESET() {
        if (mSerialPort != null) {
            dance(1);
        }
    }

    @OnClick(R.id.chatfinish)
    void chatfinish() {
        this.finish();
    }

    public void dance(int i) {
        Log.v(TAG, "enter dance");
        // 之前设置过了的motion = {"A", "B", "C", "D", "E"};//A握手B握手复位C再见D再见复位E摇头
        try {
            if (mOutputStream != null) {
                switch (i) {
                    case 1://
                        mOutputStream.write(POST1.getBytes());
                        break;
                    case 2:
                        mOutputStream.write(POST2.getBytes());
                        break;
                    case 3:
                        mOutputStream.write(POST3.getBytes());
                        break;
                    case 4://
                        mOutputStream.write(POST4.getBytes());
                        break;
                    case 5:
                        mOutputStream.write(POST5.getBytes());
                        break;
                    case 6:
                        mOutputStream.write(POST6.getBytes());
                        break;
                    case 7://
                        mOutputStream.write(POST7.getBytes());
                        break;
                    case 8:
                        mOutputStream.write(POST8.getBytes());
                        break;
                    case 9:
                        mOutputStream.write(POST9.getBytes());
                        break;
                    case 10://reset
                        mOutputStream.write(POST10.getBytes());
                        break;
                    case 11://hug
                        mOutputStream.write(POST11.getBytes());
                        break;
                    case 12://prize
                        mOutputStream.write(POST12.getBytes());
                        break;
                    case 13://lineleft
                        mOutputStream.write(POST13.getBytes());
                        break;
                    case 14://lineright
                        mOutputStream.write(POST14.getBytes());
                        break;
                    case 15://watchleft
                        mOutputStream.write(POST15.getBytes());
                        break;
                    case 16://watchright
                        mOutputStream.write(POST16.getBytes());
                        break;
                    default:
                        break;
                }
                attemptSend(mUsername + "Send " + motiondesc[i] + " Success ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
            }
        } catch (IOException e) {
            attemptSend(mUsername + "Send " + motiondesc[i] + " Dance Failed ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
            e.printStackTrace();
        }
    }

    public void faceposition(int i) {
        Log.v(TAG, "enter dance");
        // 之前设置过了的motion = {"A", "B", "C", "D", "E"};//A握手B握手复位C再见D再见复位E摇头
        try {
            if (mOutputStream != null) {
                String facestring = "$FACE," + i + "*";
                mOutputStream.write(facestring.getBytes());
//                attemptSend(mUsername + "Send FACELOCATION Success ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
            }
        } catch (IOException e) {
//            attemptSend(mUsername + "Send FACELOCATION Failed ;" + " 当前状态为 " + (ManMode ? "手动" : "自动") + " FINISH");
            e.printStackTrace();
        }
    }

    public static byte[] intToByteArray1(int i) {
        byte[] result = new byte[4];
        result[0] = (byte) ((i >> 24) & 0xFF);
        result[1] = (byte) ((i >> 16) & 0xFF);
        result[2] = (byte) ((i >> 8) & 0xFF);
        result[3] = (byte) (i & 0xFF);
        return result;
    }

    private void setAnimation() {
        if (noSpeechCount == 0) {
            rippleBackground.stopRippleAnimation();
            rippleBackground.startRippleAnimation();
        }
        chat_voice.setVisibility(View.GONE);
        iv_animation.setVisibility(View.VISIBLE);
        iv_animation.setBackgroundResource(R.drawable.animation_list);
        animation = (AnimationDrawable) iv_animation.getBackground();
        if (animation != null) {
            animation.start();
        }
    }

    private void setAnimationFan() {
        if (noSpeechCount == 0) {
            rippleBackground.stopRippleAnimation();
            rippleBackground.startRippleAnimationFan();
        }
        chat_voice.setVisibility(View.GONE);
        iv_animation.setVisibility(View.VISIBLE);
        iv_animation.setBackgroundResource(R.drawable.animation_list);
        animation = (AnimationDrawable) iv_animation.getBackground();
        if (animation != null) {
            animation.start();
        }
    }

    private void unSetAnimation() {
        rippleBackground.stopRippleAnimation();
        chat_voice.setVisibility(View.VISIBLE);
        iv_animation.setVisibility(View.GONE);
        if (animation != null) {
            animation.stop();
        }
    }

    private void setFabClickable() {
        voice_layout.setClickable(true);
    }

    private void setFabUnClickable() {
        voice_layout.setClickable(false);
    }
    /***************************************************************/

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
     * 设置语音听写参数
     */
    private void setIatParams() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
//        mIat.setParameter(SpeechConstant.ENGINE_MODE,SpeechConstant.MODE_AUTO);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineTypeCloud);
//        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineTypeMix);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
//        String lag = mIatSharedPreferences.getString("iat_language_preference", "mandarin");//普通话
//        String lag = mIatSharedPreferences.getString("iat_language_preference", "lmz");//四川话
        String lag = "mandarin";
        switch (isPuTongHua) {
            case 0:
                lag = "mandarin";//普通话
            case 1:
                lag = "cantonese";//粤语
            case 2:
                lag = "en_us";
            default:
                lag = "mandarin";
        }
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");
        // 设置语音后端点
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");
        // 设置标点符号
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");
        // 设置音频保存路径
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/iflytek/wavaudio.pcm");
    }

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
     * 不带UI听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {

        /**
         * 开始说话
         */
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            Log.v(TAG, "RecognizerListener开始说话");
            setFabUnClickable();
            setAnimationFan();
            checker.setImageResource(R.color.md_yellow_900);
        }

        /**
         * 返回错误
         */
        @Override
        public void onError(SpeechError error) {
            Log.v(TAG, "onError -> " + error.toString());
            Log.i(TAG, noSpeechCount + "");
            if (noSpeechCount > 3) {
                noSpeechCount = 0;
                Log.i(TAG, "noSpeechCount清0");
//                DoWake();
                unSetAnimation();
                setFabClickable();
//                send_RESET();//关闭接收声音
                checker.setImageResource(R.color.md_grey_600);
                mIvw = VoiceWakeuper.getWakeuper();
                hasPeople = false;
//                DoWake();
            } else {
                noSpeechCount++;
                Log.i(TAG, "计数DoListener");
                DoListener();
            }
        }

        /**
         * 结束说话
         */
        @Override
        public void onEndOfSpeech() {
            Log.v(TAG, "onEndOfSpeech");
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            if (noSpeechCount > 3) {
                showToast("结束说话");
            }
            checker.setImageResource(R.color.md_grey_600);
        }

        /**
         * 语音识别结果.过程是累加的,当last为true的时候代表真正说话完成
         * @param results
         * @param isLast
         */
        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
            String text = JsonParser.parseIatResult(results.getResultString());
            speechResult += text;
            if (isLast) {
                mReception.setText("说了 -> " + speechResult);
                if (speechResult.equals(".") || speechResult.equals("。") || speechResult.equals(",") || speechResult.equals("，") || speechResult.equals("?") || speechResult.equals("？")) {

                    Log.i(TAG, noSpeechCount + "");
                    if (noSpeechCount > 3) {
                        hasPeople = false;
                        noSpeechCount = 0;
                        Log.i(TAG, "noSpeechCount清0");
//                        send_RESET();
                        DoWake();
                        unSetAnimation();
                        setFabClickable();
                        checker.setImageResource(R.color.md_grey_600);
                        mIvw = VoiceWakeuper.getWakeuper();
                        DoWake();
                    } else {
                        noSpeechCount++;
                        Log.i(TAG, "计数DoListener");
                        DoListener();
                    }
                    return;
                }
                if (speechResult.contains("你好") || speechResult.contains("您好")) {
                    DoSpeak("您好,祝您生活愉快");
                    if (mSerialPort != null) {
                        dance(1);
                    }
                } else if (speechResult.contains("再见")) {
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix("再见，欢迎您下次再来"), ChatMsgEntity.Type.you, new Date());
                    Message message = handler.obtainMessage();
                    message.obj = chatMsgEntity;
                    handler.sendMessage(message);
                    DoSpeak("再见，欢迎您下次再来");
                    if (mSerialPort != null) {
                        dance(2);
                    }
                } else if (speechResult.contains("农商")) {
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix(nongshang), ChatMsgEntity.Type.you, new Date());
                    Message message = handler.obtainMessage();
                    message.obj = chatMsgEntity;
                    handler.sendMessage(message);
                    DoSpeak(nongshang);
                } else if (speechResult.contains("做什么") || speechResult.contains("干什么")) {
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, sayNoPrefix(introduce), ChatMsgEntity.Type.you, new Date());
                    Message message = handler.obtainMessage();
                    message.obj = chatMsgEntity;
                    handler.sendMessage(message);
                    dance(3);
                    DoSpeak(introduce);
                } else {
                    sendMsg(speechResult);
                    attemptSend(mUsername + " SAY " + speechResult + " . Status : " + (ManMode ? "手动" : "自动") + " NO FINISH");
                    unSetAnimation();
                    setFabClickable();
                    checker.setImageResource(R.color.md_grey_600);
                    mIvw = VoiceWakeuper.getWakeuper();
                    if (ManMode) {
                        DoWake();
                    }
                }
            }
        }

        /**
         * 音量改变
         * @param volume
         * @param data
         */
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            //showToast("当前正在说话，音量大小：" + volume);
            Log.d("onVolumeChanged", "返回音频数据：" + data.length);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
            // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
            // 若使用本地能力，会话id为null
            if (SpeechEvent.EVENT_SESSION_ID == eventType) {
                String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
                Log.d(TAG, "session id =" + sid);
            }
        }
    };

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            ChatMsgEntity chatMsgEntity = (ChatMsgEntity) msg.obj;
            data.add(chatMsgEntity);
            chatAdapter.notifyDataSetChanged();
            lv_chat.setSelection(data.size() - 1);
        }
    };

    /**
     * 发送消息的方法
     */
    private void sendMsg(String msg) {
//        ChatMsgEntity chatMsgEntity = new ChatMsgEntity(me_name, msg, ChatMsgEntity.Type.me, new Date());
//        Message message = handler.obtainMessage();
//        message.obj = chatMsgEntity;
//        handler.sendMessage(message);
        if (!ManMode) {
            DoTuring(msg);
        }
    }

    /**
     * 语音合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            //showToast("开始播放");
            setAnimation();
        }

        @Override
        public void onSpeakPaused() {
            //showToast("暂停播放");
        }

        @Override
        public void onSpeakResumed() {
            //showToast("继续播放");
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
//            showToast(String.format(getString(R.string.tts_toast_format), mPercentForBuffering, mPercentForPlaying));
        }

        @Override
        public void onCompleted(SpeechError error) {
            unSetAnimation();
            if (error == null) {
                //showToast("播放完成");
            } else if (error != null) {
                showToast(error.getPlainDescription(true));
            }
            noSpeechCount = 0;
            attemptSend(mUsername + " SPEAK FINISH . Status : " + (ManMode ? "手动" : "自动"));
            attemptSend(SF);
            DoListener();
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
     * 设置语音唤醒监听
     */
    private WakeuperListener mWakeuperListener = new WakeuperListener() {

        @Override
        public void onResult(WakeuperResult result) {
            try {
                String text = result.getResultString();
                JSONObject object;
                object = new JSONObject(text);
                StringBuffer buffer = new StringBuffer();
                buffer.append("【RAW】 " + text);
                buffer.append("\n");
                buffer.append("【操作类型】" + object.optString("sst"));
                buffer.append("\n");
                buffer.append("【唤醒词id】" + object.optString("id"));
                buffer.append("\n");
                buffer.append("【得分】" + object.optString("score"));
                buffer.append("\n");
                buffer.append("【前端点】" + object.optString("bos"));
                buffer.append("\n");
                buffer.append("【尾端点】" + object.optString("eos"));
                resultString = buffer.toString();
                try {
                    //先停止说话
                    mTts.stopSpeaking();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                DoListener();
            } catch (JSONException e) {
                resultString = "结果解析出错";
                e.printStackTrace();
            }
            Log.v(TAG, resultString);
        }

        @Override
        public void onError(SpeechError error) {
            Log.d(TAG, "DoWake Error -> " + error.getPlainDescription(true));
            DoWake();
        }

        @Override
        public void onBeginOfSpeech() {
            Log.d(TAG, "WakeuperListener开始说话");
        }

        @Override
        public void onEvent(int eventType, int isLast, int arg2, Bundle obj) {

        }

        @Override
        public void onVolumeChanged(int volume) {
            // TODO Auto-generated method stub

        }
    };

    /**
     * 语音识别
     */
    private void DoListener() {
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.destroy();
        }
        speechResult = "";
        setIatParams();
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            showToast("听写失败,错误码：" + ret);
        } else {
            if (noSpeechCount == 0) {
                showToast(getString(R.string.text_begin));
            }
        }
    }

    /**
     * 唤醒操作
     */
    private void DoWake() {
        Log.d(TAG, "enter dowake");
        // 加载识唤醒地资源，resPath为本地识别资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(ChatActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.iflytek_id) + ".jet");
        param.append(SpeechConstant.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        boolean ret = SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START, param.toString());
        if (!ret) {
            Log.d(TAG, "启动本地引擎失败！");
        }
        try {
            //非空判断，防止因空指针使程序崩溃
            mIvw = VoiceWakeuper.getWakeuper();
            if (mIvw == null) {
                mIvw = VoiceWakeuper.createWakeuper(this, null);
            }
            resultString = "";
            // 清空参数
            mIvw.setParameter(SpeechConstant.PARAMS, null);
            /**
             * 唤醒门限值，根据资源携带的唤醒词个数按照“id:门限;id:门限”的格式传入
             * 示例demo默认设置第一个唤醒词，建议开发者根据定制资源中唤醒词个数进行设置
             */
            mIvw.setParameter(SpeechConstant.IVW_THRESHOLD, "0:"
                    + curThresh);
            // 设置唤醒模式
            mIvw.setParameter(SpeechConstant.IVW_SST, "wakeup");
            // 设置持续进行唤醒
            mIvw.setParameter(SpeechConstant.KEEP_ALIVE, "1");
            // 设置闭环优化网络模式
            mIvw.setParameter(SpeechConstant.IVW_NET_MODE, "0");
            // 设置唤醒资源路径
            mIvw.setParameter(SpeechConstant.IVW_RES_PATH, getIvwResource());
            mIvw.startListening(mWakeuperListener);
            Log.d(TAG, "唤醒初始化完成");
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "唤醒未初始化");
        }
    }

    private String getIvwResource() {
        return ResourceUtil.generateResourcePath(ChatActivity.this,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.iflytek_id) + ".jet");
    }

    /**
     * 语音播报
     *
     * @param msg 语音播报的内容
     */
    private void DoSpeak(String msg) {

        checker.setImageResource(R.color.md_grey_600);
        unSetAnimation();
        setFabClickable();
        if (mIvw != null) {
            mIvw.cancel();
        }
        if (mIat != null) {
            mIat.stopListening();
            mIat.cancel();
        }

        Message message = handlervoice.obtainMessage();
        message.obj = msg;
        handlervoice.sendMessage(message);
    }

    /**
     * 图灵机器人识别
     *
     * @param msg 传入的字符串
     */
    private void DoTuring(String msg) {
        try {
            String INFO = URLEncoder.encode(msg, "utf-8");
            System.out.println("进入DoTuring -> " + INFO);
            String url = getString(R.string.turing_api_address) + "?key=" + getString(R.string.turing_api_key) + "&info=" + INFO + "&userid=" + getString(R.string.turing_api_userid);
            RequestQueue queue = Volley.newRequestQueue(this);
            queue.add(new StringRequest(url, new Response.Listener<String>() {
                @Override
                public void onResponse(String s) {
                    replyMsgHandler(s);
                    String url = getMsgUrl(s);
                    if (url != null && !url.equals("")) {
                        showAlertDialog(url);
                    }
                    String response = getTuringText(s);
                    if (response.contains("志哥哥")) {
                        response = response.replace("志哥哥", "");
                    }
                    attemptSend(mUsername + " XUNFEI SAY " + response + " . Status : " + (ManMode ? "手动" : "自动") + " NO FINISH");
                    DoSpeak(response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, getTuringText("您的问题好深奥,我无法回答"), ChatMsgEntity.Type.you, new Date());
                    Message message = handler.obtainMessage();
                    message.obj = chatMsgEntity;
                    handler.sendMessage(message);
                    DoSpeak("您的问题好深奥,我无法回答");
                }
            }));
        } catch (UnsupportedEncodingException e) {
            ChatMsgEntity chatMsgEntity = new ChatMsgEntity(robot_name, getTuringText("您的问题好深奥,我无法回答"), ChatMsgEntity.Type.you, new Date());
            Message message = handler.obtainMessage();
            message.obj = chatMsgEntity;
            handler.sendMessage(message);
            DoSpeak("您的问题好深奥,我无法回答");
        }
    }

    private String getMsgUrl(String msg) {
        try {
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg);
            String url = jsonObject.getString("url");
            return url;
        } catch (Exception e) {
            return "";
        }
    }

    /**
     * 得到图灵返回的text字段
     * 图灵返回json字符串信息见http://www.tuling123.com/html/doc/api.html#jiekoudizhi
     *
     * @param msg
     * @return
     */
    private String getTuringText(String msg) {
        try {
            com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(msg);
            String text = jsonObject.getString("text");
            return text;
        } catch (Exception e) {
            return "";
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (resultCode) {
            case CAMERANUM:
                noSpeechCount = 0;
                chat_test();
//                DoSpeak("和我聊聊天吧");
                break;
            default:
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mIat != null && mIat.isListening()) {
            mIat.stopListening();
        }
        if (mIvw != null && mIvw.isListening()) {
            mIvw.stopListening();
        }
        if (mTts != null && mTts.isSpeaking()) {
            mTts.stopSpeaking();
        }
        unSetAnimation();
        setFabClickable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIvw != null && !mIvw.isListening()) {
            DoWake();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // 释放语音合成
        if (mIat != null) {
            mIat.cancel();
            mIat.destroy();
            mIat = null;
        }
        if (mTts != null) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
            mTts = null;
        }
        mIvw = VoiceWakeuper.getWakeuper();
        if (mIvw != null) {
            mIvw.cancel();
            mIvw.destroy();
            mIvw = null;
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

    public void showAlertDialog(String url) {

        final WebDialog.Builder builder = new WebDialog.Builder(ChatActivity.this);
        builder.setWebsite(url);
        builder.setDeleteListener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        WebDialog dialog = builder.create();
        dialog.show();
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = 1200;
        params.height = 700;
        dialog.getWindow().setAttributes(params);
    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem rfacLabelItem) {

        rfabHelper.toggleContent();
        switch (position) {
            case 0:
                isPuTongHua = 0;
                break;
            case 1:
                isPuTongHua = 1;
                break;
            case 2:
                isPuTongHua = 2;
                break;
            default:
                break;
        }
    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem rfacLabelItem) {

        rfabHelper.toggleContent();
        switch (position) {
            case 0:
                isPuTongHua = 0;
                break;
            case 1:
                isPuTongHua = 1;
                break;
            case 2:
                isPuTongHua = 2;
                break;
            default:
                break;
        }
    }

    public void showToast(String toast) {
        ToastUtils.showShort(getApplicationContext(), toast);
    }
}
