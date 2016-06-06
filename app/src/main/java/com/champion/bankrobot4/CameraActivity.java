package com.champion.bankrobot4;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.champion.bankrobot4.utils.DistanceUtil;
import com.champion.bankrobot4.utils.FileUtils;
import com.champion.bankrobot4.utils.IOUtil;
import com.champion.bankrobot4.utils.JsonParser;
import com.champion.bankrobot4.utils.ToastUtils;
import com.champion.bankrobot4.utils.camera.CameraHelper;
import com.champion.bankrobot4.utils.camera.ImageUtils;
import com.champion.bankrobot4.view.camera.CameraGrid;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraActivity extends AppCompatActivity {

    //照相
    private CameraHelper mCameraHelper;
    private Camera.Parameters parameters = null;
    private Camera cameraInst = null;
    private Bundle bundle = null;
    private int photoWidth = DistanceUtil.getCameraPhotoWidth();
    private int photoNumber = 4;
    private int photoMargin = SpeechApp.getApp().dp2px(1);
    private float pointX, pointY;
    static final int FOCUS = 1;            // 聚焦
    static final int ZOOM = 2;            // 缩放
    private int mode;                      //0是聚焦 1是放大
    private float dist;
    private int PHOTO_SIZE = 2000;
    private int mCurrentCameraId = 0;  //1是前置 0是后置
    private Handler handler = new Handler();
    private boolean isPreview = false;

    //语音
    //科大语音
    private String speechResult = "";
    //科大讯飞相应变量
    private String mEngineType = SpeechConstant.TYPE_CLOUD; //语音类型,云端(用于语音听写、语音合成)
    //语音听写相关参数Iat
    SpeechRecognizer mIat;    //语音听写对象
    //RecognizerDialog mIatDialog;  //语音听写对话框
    private SharedPreferences mIatSharedPreferences;//语音听写sharedpreferences
    //语音合成相关参数Tts
    private SpeechSynthesizer mTts;//语音合成对象
    public static String voicer = "xiaoyan";//默认发言人
    private int mPercentForBuffering = 0;//缓冲进度
    private int mPercentForPlaying = 0;//播放速度
    //语音唤醒相关参数
    private VoiceWakeuper mIvw;
    private String resultString;//语音唤醒结果内容
    public static int curThresh = 10;    //唤醒门限值
    private SharedPreferences mTtsSharedPreferences;//语音合成sharedpreferences
    private Handler handlervoice = new Handler() {  //语音播放handler
        @Override
        public void handleMessage(Message msg) {
            String detail = (String) msg.obj;
            setTtsParam();
            int code = mTts.startSpeaking(detail, mTtsListener);
            if (code != ErrorCode.SUCCESS) {
                ToastUtils.showShort(CameraActivity.this, "语音合成失败,错误码: " + code);
            }
        }
    };

    /**
     * 设置语音听写参数
     */
    private void setIatParams() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);
        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");
        // 设置语言
        String lag = mIatSharedPreferences.getString("iat_language_preference", "mandarin");
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
        if (mEngineType.equals(SpeechConstant.TYPE_CLOUD)) {
            //设置使用云端引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        } else {
            //设置使用本地引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            //设置发音人资源路径
            mTts.setParameter(ResourceUtil.TTS_RES_PATH, getResourcePath());
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
        }
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
        tempBuffer.append(ResourceUtil.generateResourcePath(this, ResourceUtil.RESOURCE_TYPE.assets, "tts/" + CameraActivity.voicer + ".jet"));
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
        }

        /**
         * 返回错误
         */
        @Override
        public void onError(SpeechError error) {
            DoWake();
        }

        /**
         * 结束说话
         */
        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            ToastUtils.showShort(CameraActivity.this,"结束");
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
                ToastUtils.showShort(CameraActivity.this,speechResult);
                if (speechResult.contains("关闭")) {
                    Intent data = new Intent();
                    data.putExtra("hehe", "hehe");
                    setResult(ChatActivity.CAMERANUM, data);
                    finish();
                } else if (speechResult.contains("继续")) {
                    try {
                        cameraInst.startPreview();
                        cameraGrid.setVisibility(View.VISIBLE);
                        isPreview = true;
                    } catch (Exception e) {
                        e.printStackTrace();
                        isPreview = false;
                    }
                } else if (speechResult.contains("茄子") || speechResult.contains("照")) {
                    takePhoto();
                }
                DoWake();
            }
        }

        /**
         * 音量改变
         * @param volume
         * @param data
         */
        @Override
        public void onVolumeChanged(int volume, byte[] data) {
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

    /**
     * 语音合成回调监听。
     */
    private SynthesizerListener mTtsListener = new SynthesizerListener() {

        @Override
        public void onSpeakBegin() {
            //showToast("开始播放");
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
                ToastUtils.showShort(CameraActivity.this, error.getPlainDescription(true));
            }
            DoWake();
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
                //destroy是为了下面的说话，语音识别时必须禁用唤醒功能
                mIvw = VoiceWakeuper.getWakeuper();
                if (mIvw != null) {
                    mIvw.destroy();
                }
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
            Log.d(TAG, error.getPlainDescription(true));
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
        speechResult = "";
        setIatParams();
//        mIatDialog.setListener(mRecognizerDialogListener);
//        mIatDialog.show();
        int ret = mIat.startListening(mRecognizerListener);
        if (ret != ErrorCode.SUCCESS) {
            ToastUtils.showShort(CameraActivity.this, "听写失败,错误码：" + ret);
        } else {
            ToastUtils.showShort(CameraActivity.this, getString(R.string.text_begin));
        }
    }

    /**
     * 唤醒操作
     */
    private void DoWake() {
        Log.d(TAG, "enter dowake");
        // 加载识唤醒地资源，resPath为本地识别资源路径
        StringBuffer param = new StringBuffer();
        String resPath = ResourceUtil.generateResourcePath(CameraActivity.this, ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.iflytek_id) + ".jet");
        param.append(SpeechConstant.IVW_RES_PATH + "=" + resPath);
        param.append("," + ResourceUtil.ENGINE_START + "=" + SpeechConstant.ENG_IVW);
        boolean ret = SpeechUtility.getUtility().setParameter(ResourceUtil.ENGINE_START, param.toString());
        if (!ret) {
            Log.d(TAG, "启动本地引擎失败！");
        }
        try {
            //非空判断，防止因空指针使程序崩溃
            mIvw = VoiceWakeuper.getWakeuper();
            if (mIvw != null) {
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
            } else {
                Log.d(TAG, "唤醒未初始化");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.d(TAG, "唤醒未初始化");
        }
    }

    private String getIvwResource() {
        return ResourceUtil.generateResourcePath(CameraActivity.this,
                ResourceUtil.RESOURCE_TYPE.assets, "ivw/" + getString(R.string.iflytek_id) + ".jet");
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

    @BindView(R.id.masking)
    CameraGrid cameraGrid;
    @BindView(R.id.panel_take_photo)
    View takePhotoPanel;
    @BindView(R.id.takepicture)
    Button takePicture;
    @BindView(R.id.flashBtn)
    ImageView flashBtn;
    @BindView(R.id.camera_back)
    ImageView backBtn;
    @BindView(R.id.next)
    ImageView galleryBtn;
    @BindView(R.id.focus_index)
    View focusIndex;
    @BindView(R.id.surfaceView)
    SurfaceView surfaceView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        initView();
        initEvent();
    }

    private void initView() {
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.setKeepScreenOn(true);
        surfaceView.setFocusable(true);
        surfaceView.setBackgroundColor(TRIM_MEMORY_BACKGROUND);
        surfaceView.getHolder().addCallback(new SurfaceCallback());//为SurfaceView的句柄添加一个回调函数

        //设置相机界面,照片列表,以及拍照布局的高度(保证相机预览为正方形)
        ViewGroup.LayoutParams layout = cameraGrid.getLayoutParams();
        layout.height = SpeechApp.getApp().getScreenWidth();
        layout = takePhotoPanel.getLayoutParams();
        layout.height = SpeechApp.getApp().getScreenHeight()
                - SpeechApp.getApp().getScreenWidth()
                - DistanceUtil.getCameraPhotoAreaHeight();
    }

    private void initEvent() {
        //拍照
        takePicture.setOnClickListener(v -> {
            takePhoto();
        });
        //闪光灯
        //flashBtn.setOnClickListener(v -> turnLight(cameraInst));
        //跳转相册
        //galleryBtn.setOnClickListener(v -> startActivity(new Intent(CameraActivity.this, AlbumActivity.class)));
        //返回按钮
        backBtn.setOnClickListener(v -> finish());
        surfaceView.setOnTouchListener((v, event) -> {
            switch (event.getAction() & MotionEvent.ACTION_MASK) {
                // 主点按下
                case MotionEvent.ACTION_DOWN:
                    pointX = event.getX();
                    pointY = event.getY();
                    mode = FOCUS;
                    break;
                // 副点按下
                case MotionEvent.ACTION_POINTER_DOWN:
                    dist = spacing(event);
                    // 如果连续两点距离大于10，则判定为多点模式
                    if (spacing(event) > 10f) {
                        mode = ZOOM;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_POINTER_UP:
                    mode = FOCUS;
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (mode == FOCUS) {
                        //pointFocus((int) event.getRawX(), (int) event.getRawY());
                    } else if (mode == ZOOM) {
                        float newDist = spacing(event);
                        if (newDist > 10f) {
                            float tScale = (newDist - dist) / dist;
                            if (tScale < 0) {
                                tScale = tScale * 10;
                            }
                            addZoomIn((int) tScale);
                        }
                    }
                    break;
            }
            return false;
        });

        surfaceView.setOnClickListener(v -> {
            try {
                pointFocus((int) pointX, (int) pointY);
            } catch (Exception e) {
                e.printStackTrace();
            }
            RelativeLayout.LayoutParams layout = new RelativeLayout.LayoutParams(focusIndex.getLayoutParams());
            layout.setMargins((int) pointX - 60, (int) pointY - 60, 0, 0);
            focusIndex.setLayoutParams(layout);
            focusIndex.setVisibility(View.VISIBLE);
            ScaleAnimation sa = new ScaleAnimation(3f, 1f, 3f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f, ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            sa.setDuration(800);
            focusIndex.startAnimation(sa);
            handler.postDelayed(() -> focusIndex.setVisibility(View.INVISIBLE), 800);
        });

        takePhotoPanel.setOnClickListener(v -> {
            //doNothing 防止聚焦框出现在拍照区域
        });
        /*****************************语音听写初始化*****************************/
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
        DoWake();   //唤醒打开
    }

    private void takePhoto() {
        try {
            cameraInst.takePicture(null, null, new MyPictureCallback());
            isPreview = false;
        } catch (Throwable t) {
            t.printStackTrace();
            ToastUtils.showShort(CameraActivity.this, "拍照失败，请重试！");
            try {
                cameraInst.startPreview();
                isPreview = true;
            } catch (Throwable e) {
                e.printStackTrace();
                isPreview = false;
            }
        }
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

//        @Override
//        protected void onActivityResult ( final int requestCode, final int resultCode,
//        final Intent result){
//            if (requestCode == AppConstants.REQUEST_PICK && resultCode == RESULT_OK) {
//                CameraManager.getInst().processPhotoItem(
//                        CameraActivity.this,
//                        new PhotoItem(result.getData().getPath(), System
//                                .currentTimeMillis()));
//            } else if (requestCode == AppConstants.REQUEST_CROP && resultCode == RESULT_OK) {
//                Intent newIntent = new Intent(this, PhotoProcessActivity.class);
//                newIntent.setData(result.getData());
//                startActivity(newIntent);
//            }
//        }

    /**
     * 两点的距离
     */

    private float spacing(MotionEvent event) {
        if (event == null) {
            return 0;
        }
        float x = event.getX(0) - event.getX(1);
        float y = event.getY(0) - event.getY(1);
        return (float) (Math.sqrt(x * x + y * y));
    }

    //放大缩小
    int curZoomValue = 0;

    private void addZoomIn(int delta) {

        try {
            Camera.Parameters params = cameraInst.getParameters();
            Log.d("Camera", "Is support Zoom " + params.isZoomSupported());
            if (!params.isZoomSupported()) {
                return;
            }
            curZoomValue += delta;
            if (curZoomValue < 0) {
                curZoomValue = 0;
            } else if (curZoomValue > params.getMaxZoom()) {
                curZoomValue = params.getMaxZoom();
            }

            if (!params.isSmoothZoomSupported()) {
                params.setZoom(curZoomValue);
                cameraInst.setParameters(params);
                return;
            } else {
                cameraInst.startSmoothZoom(curZoomValue);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //定点对焦的代码
    private void pointFocus(int x, int y) {
        cameraInst.cancelAutoFocus();
        parameters = cameraInst.getParameters();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            showPoint(x, y);
        }
        cameraInst.setParameters(parameters);
        autoFocus();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void showPoint(int x, int y) {
        if (parameters.getMaxNumMeteringAreas() > 0) {
            List<Camera.Area> areas = new ArrayList<Camera.Area>();
            //xy变换了
            int rectY = -x * 2000 / SpeechApp.getApp().getScreenWidth() + 1000;
            int rectX = y * 2000 / SpeechApp.getApp().getScreenHeight() - 1000;

            int left = rectX < -900 ? -1000 : rectX - 100;
            int top = rectY < -900 ? -1000 : rectY - 100;
            int right = rectX > 900 ? 1000 : rectX + 100;
            int bottom = rectY > 900 ? 1000 : rectY + 100;
            Rect area1 = new Rect(left, top, right, bottom);
            areas.add(new Camera.Area(area1, 800));
            parameters.setMeteringAreas(areas);
        }

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
    }

    private final class MyPictureCallback implements Camera.PictureCallback {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            bundle = new Bundle();
            bundle.putByteArray("bytes", data); //将图片字节数据保存在bundle当中，实现数据交换
            new SavePicTask(data).execute();
            //camera.startPreview(); // 拍完照后，重新开始预览
            cameraInst.stopPreview();
            cameraGrid.setVisibility(View.GONE);
        }
    }

    private class SavePicTask extends AsyncTask<Void, Void, String> {
        private byte[] data;

        protected void onPreExecute() {
            //showProgressDialog("处理中");
        }

        SavePicTask(byte[] data) {
            this.data = data;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return saveToSDCard(data);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
//
//            if (StringUtils.isNotEmpty(result)) {
//                dismissProgressDialog();
//                CameraManager.getInst().processPhotoItem(CameraActivity.this,
//                        new PhotoItem(result, System.currentTimeMillis()));
//            } else {
//                ToastUtils.showShort(CameraActivity.this,"拍照失败，请稍后重试！");
//            }
        }
    }

    /*SurfaceCallback*/
    private final class SurfaceCallback implements SurfaceHolder.Callback {

        public void surfaceDestroyed(SurfaceHolder holder) {
            try {
                if (cameraInst != null) {
                    cameraInst.stopPreview();
                    cameraInst.release();
                    cameraInst = null;
                }
            } catch (Exception e) {
                //相机已经关了
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            if (null == cameraInst) {
                try {
                    cameraInst = Camera.open();
                    cameraInst.setPreviewDisplay(holder);
                    initCamera();
                    cameraInst.startPreview();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            autoFocus();
        }
    }

    //实现自动对焦
    private void autoFocus() {
        new Thread() {
            @Override
            public void run() {
                try {
                    sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (cameraInst == null) {
                    return;
                }
                cameraInst.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            initCamera();//实现相机的参数初始化
                        }
                    }
                });
            }
        };
    }

    private Camera.Size adapterSize = null;
    private Camera.Size previewSize = null;

    private void initCamera() {
        parameters = cameraInst.getParameters();
        parameters.setPictureFormat(PixelFormat.JPEG);
        //if (adapterSize == null) {
        setUpPicSize(parameters);
        setUpPreviewSize(parameters);
        //}
        if (adapterSize != null) {
            parameters.setPictureSize(adapterSize.width, adapterSize.height);
        }
        if (previewSize != null) {
            parameters.setPreviewSize(previewSize.width, previewSize.height);
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);//1连续对焦
        } else {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }
        //setDispaly(parameters, cameraInst);
        try {
            cameraInst.setParameters(parameters);
        } catch (Exception e) {
            e.printStackTrace();
        }
        cameraInst.startPreview();
        cameraInst.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
    }

    private void setUpPicSize(Camera.Parameters parameters) {

        if (adapterSize != null) {
            return;
        } else {
            adapterSize = findBestPictureResolution();
            return;
        }
    }

    private void setUpPreviewSize(Camera.Parameters parameters) {

        if (previewSize != null) {
            return;
        } else {
            previewSize = findBestPreviewResolution();
        }
    }

    /**
     * 最小预览界面的分辨率
     */
    private static final int MIN_PREVIEW_PIXELS = 480 * 320;
    /**
     * 最大宽高比差
     */
    private static final double MAX_ASPECT_DISTORTION = 0.15;
    private static final String TAG = "CameraActivity";

    /**
     * 找出最适合的预览界面分辨率
     *
     * @return
     */
    private Camera.Size findBestPreviewResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        Camera.Size defaultPreviewResolution = cameraParameters.getPreviewSize();

        List<Camera.Size> rawSupportedSizes = cameraParameters.getSupportedPreviewSizes();
        if (rawSupportedSizes == null) {
            return defaultPreviewResolution;
        }

        // 按照分辨率从大到小排序
        List<Camera.Size> supportedPreviewResolutions = new ArrayList<Camera.Size>(rawSupportedSizes);
        Collections.sort(supportedPreviewResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        StringBuilder previewResolutionSb = new StringBuilder();
        for (Camera.Size supportedPreviewResolution : supportedPreviewResolutions) {
            previewResolutionSb.append(supportedPreviewResolution.width).append('x').append(supportedPreviewResolution.height)
                    .append(' ');
        }
        Log.v(TAG, "Supported preview resolutions: " + previewResolutionSb);


        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) SpeechApp.getApp().getScreenWidth()
                / (double) SpeechApp.getApp().getScreenHeight();
        Iterator<Camera.Size> it = supportedPreviewResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 移除低于下限的分辨率，尽可能取高分辨率
            if (width * height < MIN_PREVIEW_PIXELS) {
                it.remove();
                continue;
            }

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然preview宽高比后在比较
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }

            // 找到与屏幕分辨率完全匹配的预览界面分辨率直接返回
            if (maybeFlippedWidth == SpeechApp.getApp().getScreenWidth()
                    && maybeFlippedHeight == SpeechApp.getApp().getScreenHeight()) {
                return supportedPreviewResolution;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，则设置其中最大比例的，对于配置比较低的机器不太合适
        if (!supportedPreviewResolutions.isEmpty()) {
            Camera.Size largestPreview = supportedPreviewResolutions.get(0);
            return largestPreview;
        }

        // 没有找到合适的，就返回默认的

        return defaultPreviewResolution;
    }

    private Camera.Size findBestPictureResolution() {
        Camera.Parameters cameraParameters = cameraInst.getParameters();
        List<Camera.Size> supportedPicResolutions = cameraParameters.getSupportedPictureSizes(); // 至少会返回一个值

        StringBuilder picResolutionSb = new StringBuilder();
        for (Camera.Size supportedPicResolution : supportedPicResolutions) {
            picResolutionSb.append(supportedPicResolution.width).append('x')
                    .append(supportedPicResolution.height).append(" ");
        }
        Log.d(TAG, "Supported picture resolutions: " + picResolutionSb);

        Camera.Size defaultPictureResolution = cameraParameters.getPictureSize();
        Log.d(TAG, "default picture resolution " + defaultPictureResolution.width + "x"
                + defaultPictureResolution.height);

        // 排序
        List<Camera.Size> sortedSupportedPicResolutions = new ArrayList<Camera.Size>(
                supportedPicResolutions);
        Collections.sort(sortedSupportedPicResolutions, new Comparator<Camera.Size>() {
            @Override
            public int compare(Camera.Size a, Camera.Size b) {
                int aPixels = a.height * a.width;
                int bPixels = b.height * b.width;
                if (bPixels < aPixels) {
                    return -1;
                }
                if (bPixels > aPixels) {
                    return 1;
                }
                return 0;
            }
        });

        // 移除不符合条件的分辨率
        double screenAspectRatio = (double) SpeechApp.getApp().getScreenWidth()
                / (double) SpeechApp.getApp().getScreenHeight();
        Iterator<Camera.Size> it = sortedSupportedPicResolutions.iterator();
        while (it.hasNext()) {
            Camera.Size supportedPreviewResolution = it.next();
            int width = supportedPreviewResolution.width;
            int height = supportedPreviewResolution.height;

            // 在camera分辨率与屏幕分辨率宽高比不相等的情况下，找出差距最小的一组分辨率
            // 由于camera的分辨率是width>height，我们设置的portrait模式中，width<height
            // 因此这里要先交换然后在比较宽高比
            boolean isCandidatePortrait = width > height;
            int maybeFlippedWidth = isCandidatePortrait ? height : width;
            int maybeFlippedHeight = isCandidatePortrait ? width : height;
            double aspectRatio = (double) maybeFlippedWidth / (double) maybeFlippedHeight;
            double distortion = Math.abs(aspectRatio - screenAspectRatio);
            if (distortion > MAX_ASPECT_DISTORTION) {
                it.remove();
                continue;
            }
        }

        // 如果没有找到合适的，并且还有候选的像素，对于照片，则取其中最大比例的，而不是选择与屏幕分辨率相同的
        if (!sortedSupportedPicResolutions.isEmpty()) {
            return sortedSupportedPicResolutions.get(0);
        }

        // 没有找到合适的，就返回默认的
        return defaultPictureResolution;
    }


    //控制图像的正确显示方向
    private void setDispaly(Camera.Parameters parameters, Camera camera) {
        if (Build.VERSION.SDK_INT >= 8) {
            setDisplayOrientation(camera, 90);
        } else {
            parameters.setRotation(90);
        }
    }

    //实现的图像的正确显示
    private void setDisplayOrientation(Camera camera, int i) {
        Method downPolymorphic;
        try {
            downPolymorphic = camera.getClass().getMethod("setDisplayOrientation",
                    new Class[]{int.class});
            if (downPolymorphic != null) {
                downPolymorphic.invoke(camera, new Object[]{i});
            }
        } catch (Exception e) {
            Log.e("Came_e", "图像出错");
        }
    }


    /**
     * 将拍下来的照片存放在SD卡中
     *
     * @param data
     * @throws IOException
     */
    public String saveToSDCard(byte[] data) throws IOException {
        Bitmap croppedImage;

        //获得图片大小
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);

        PHOTO_SIZE = options.outHeight > options.outWidth ? options.outWidth : options.outHeight;
        int height = options.outHeight > options.outWidth ? options.outHeight : options.outWidth;
        options.inJustDecodeBounds = false;
        Rect r;
        if (mCurrentCameraId == 1) {
            r = new Rect(height - PHOTO_SIZE, 0, height, PHOTO_SIZE);
        } else {
            r = new Rect(0, 0, PHOTO_SIZE, PHOTO_SIZE);
        }
        try {
            croppedImage = decodeRegionCrop(data, r);
        } catch (Exception e) {
            return null;
        }
        String imagePath = ImageUtils.saveToFile(FileUtils.getInst().getSystemPhotoPath(), true, croppedImage);
        croppedImage.recycle();
        return imagePath;
    }

    private Bitmap decodeRegionCrop(byte[] data, Rect rect) {

        InputStream is = null;
        System.gc();
        Bitmap croppedImage = null;
        try {
            is = new ByteArrayInputStream(data);
            BitmapRegionDecoder decoder = BitmapRegionDecoder.newInstance(is, false);

            try {
                croppedImage = decoder.decodeRegion(rect, new BitmapFactory.Options());
            } catch (IllegalArgumentException e) {
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            IOUtil.closeStream(is);
        }
        Matrix m = new Matrix();
        m.setRotate(90, PHOTO_SIZE / 2, PHOTO_SIZE / 2);
        if (mCurrentCameraId == 1) {
            m.postScale(1, -1);
        }
        Bitmap rotatedImage = Bitmap.createBitmap(croppedImage, 0, 0, PHOTO_SIZE, PHOTO_SIZE, m, true);
        if (rotatedImage != croppedImage)
            croppedImage.recycle();
        return rotatedImage;
    }

    /**
     * 闪光灯开关   开->关->自动
     *
     * @param mCamera
     */
//    private void turnLight(Camera mCamera) {
//        if (mCamera == null || mCamera.getParameters() == null
//                || mCamera.getParameters().getSupportedFlashModes() == null) {
//            return;
//        }
//        Camera.Parameters parameters = mCamera.getParameters();
//        String flashMode = mCamera.getParameters().getFlashMode();
//        List<String> supportedModes = mCamera.getParameters().getSupportedFlashModes();
//        if (Camera.Parameters.FLASH_MODE_OFF.equals(flashMode)
//                && supportedModes.contains(Camera.Parameters.FLASH_MODE_ON)) {//关闭状态
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
//            mCamera.setParameters(parameters);
//            flashBtn.setImageResource(R.drawable.camera_flash_on);
//        } else if (Camera.Parameters.FLASH_MODE_ON.equals(flashMode)) {//开启状态
//            if (supportedModes.contains(Camera.Parameters.FLASH_MODE_AUTO)) {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_AUTO);
//                flashBtn.setImageResource(R.drawable.camera_flash_auto);
//                mCamera.setParameters(parameters);
//            } else if (supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
//                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//                flashBtn.setImageResource(R.drawable.camera_flash_off);
//                mCamera.setParameters(parameters);
//            }
//        } else if (Camera.Parameters.FLASH_MODE_AUTO.equals(flashMode)
//                && supportedModes.contains(Camera.Parameters.FLASH_MODE_OFF)) {
//            parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
//            mCamera.setParameters(parameters);
//            flashBtn.setImageResource(R.drawable.camera_flash_off);
//        }
//    }

    //切换前后置摄像头
    private void switchCamera() {
        mCurrentCameraId = (mCurrentCameraId + 1) % mCameraHelper.getNumberOfCameras();
        releaseCamera();
        Log.d("DDDD", "DDDD----mCurrentCameraId" + mCurrentCameraId);
        setUpCamera(mCurrentCameraId);
    }

    private void releaseCamera() {
        if (cameraInst != null) {
            cameraInst.setPreviewCallback(null);
            cameraInst.release();
            cameraInst = null;
        }
        adapterSize = null;
        previewSize = null;
    }

    /**
     * @param mCurrentCameraId2
     */
    private void setUpCamera(int mCurrentCameraId2) {
        cameraInst = getCameraInstance(mCurrentCameraId2);
        if (cameraInst != null) {
            try {
                cameraInst.setPreviewDisplay(surfaceView.getHolder());
                initCamera();
                cameraInst.startPreview();
                isPreview = true;
            } catch (IOException e) {
                e.printStackTrace();
                isPreview = false;
            }
        } else {
            isPreview = false;
            ToastUtils.showShort(CameraActivity.this, "切换失败，请重试！");
        }
    }

    private Camera getCameraInstance(final int id) {
        Camera c = null;
        try {
            c = mCameraHelper.openCamera(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return c;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 释放语音合成
        if (mIat != null) {
            mIat.cancel();
            mIat.destroy();
        }
        if (mTts != null) {
            mTts.stopSpeaking();
            // 退出时释放连接
            mTts.destroy();
        }
//        mIvw = VoiceWakeuper.getWakeuper();
//        if (mIvw != null) {
//            mIvw.cancel();
//            mIvw.destroy();
//        }
    }
}
