package com.lingju.emokitsdkdemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

import com.emokit.sdk.InitListener;
import com.emokit.sdk.heartrate.EmoRateListener;
import com.emokit.sdk.heartrate.RateDetect;
import com.emokit.sdk.record.SpeechEmotionDetect;
import com.emokit.sdk.record.SpeechEmotionListener;
import com.emokit.sdk.senseface.ExpressionDetect;
import com.emokit.sdk.senseface.ExpressionListener;
import com.emokit.sdk.util.SDKAppInit;
import com.emokit.sdk.util.SDKConstant;
import com.google.gson.Gson;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String PLATFLAG = "Emokit_sdkdemo";
    public static final String USERNAME = "freestyle_ken@163.com";
    public static final String PASSWORD = "freestyle_ken@163.com";
    private static final String XFYUN_APPID = "581b1a0c";
    private SpeechEmotionDetect mSpeechDetect;
    private RateDetect mRateDetect;
    private ExpressionDetect mExpressionDetect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /* 初始化EmokitSDK */
        SDKAppInit.createInstance(this);
        SDKAppInit.setDebugMode(false);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_voice).setOnClickListener(this);
        findViewById(R.id.btn_face).setOnClickListener(this);
        findViewById(R.id.btn_expression).setOnClickListener(this);
        findViewById(R.id.btn_hand).setOnClickListener(this);
        findViewById(R.id.btn_finger).setOnClickListener(this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 语音识别（情绪+文本）
     **/
    public void recognizeBySpeech() {
        /* 获取情绪识别器对象 */
        if (mSpeechDetect == null) {
            mSpeechDetect = SpeechEmotionDetect.createRecognizer(this, mInitListener);
        }
        /* 启动识别监听 */
        mSpeechDetect.startListening(mRecognizerListener, SDKConstant.RC_TYPE_5, false, XFYUN_APPID);

    }

    /**
     * 脸部扫描识别
     **/
    public void recognizeByFace() {
        if (mRateDetect == null) {
            mRateDetect = RateDetect.createRecognizer(this, mInitListener);
        }
        // 设置摄像头参数
        mRateDetect.setParameter(SDKConstant.FACING, SDKConstant.CAMERA_FACING_FRONT);
        // 开始监听
        mRateDetect.startRateListening(mEmoRateListener);

    }

    /**
     * 表情扫描识别
     **/
    public void recognizeByExpression() {
        if (mExpressionDetect == null) {
            mExpressionDetect = ExpressionDetect.createRecognizer(this, mInitListener);
        }
        // 设置摄像头参数
        mExpressionDetect.setParameter(SDKConstant.FACING,
                SDKConstant.CAMERA_FACING_FRONT);
        // 开始监听
        mExpressionDetect.startRateListening(mExpressionListener);

    }

    /**
     * 表情识别监听
     **/
    ExpressionListener mExpressionListener = new ExpressionListener() {
        @Override
        public void beginDetect() {
            // 开始监测
        }

        @Override
        public void endDetect(String result, String savePath) {
            // 结束监测,返回情绪结果,和照片保存路径
            parse(result);
        }
    };

    /**
     * 脸部识别监听器
     **/
    EmoRateListener mEmoRateListener = new EmoRateListener() {
        @Override
        public void beginDetect() {
            // 开始监测
        }

        @Override
        public void endDetect(String result) {
            // 结束监测,返回情绪结果
            parse(result);
        }

        @Override
        public void monitor(double v) {
            // 实时红光色值变换,可用作显示心率波形
            Log.i("LingJu", "心率波形" + v);
        }

    };


    /**
     * 语音识别监听器(运行在子线程)
     **/
    SpeechEmotionListener mRecognizerListener = new SpeechEmotionListener() {
        /** 音量改变回调 **/
        @Override
        public void onVolumeChanged(int i) {

        }

        /** 开始说话 **/
        @Override
        public void onBeginOfSpeech() {
            Log.i("LingJu", "MainActivity onBeginOfSpeech()");
        }

        /** 结束说话 **/
        @Override
        public void onEndOfSpeech() {
            Log.i("LingJu", "MainActivity onEndOfSpeech()");
        }

        /** 情绪识别结果回调 **/
        @Override
        public void onEmotionResult(String emotionResult) {
            parse(emotionResult);
        }

        /** 语音识别文本回调 **/
        @Override
        public void onSpeechResult(String text) {
            Log.i("LingJu", "识别文本：" + text);
        }

    };

    /**
     * 解析识别结果
     **/
    private void parse(String result) {
        Gson gson = new Gson();
        if (result.contains("&&")) {
            String[] emotions = result.split("&&");
            for (String emotion : emotions) {
                parseDetail(gson, emotion);
            }
        } else {
            parseDetail(gson, result);
        }
    }

    private void parseDetail(Gson gson, String emotion) {
        EmotionBean emotionBean = gson.fromJson(emotion, EmotionBean.class);
        if (emotionBean.resultcode.equals("200")) {
            Log.i("LingJu", "情绪：" + emotionBean.rc_main);
        } else {
            Log.i("LingJu", "描述：" + emotionBean.reason + "，结果码：" + emotionBean.resultcode);
        }
    }

    /**
     * 初始化用户信息
     **/
    private InitListener mInitListener = new InitListener() {
        @Override
        public void onInit(int code) {
            // 注册用户信息: platflag 应用名; userName 用户名或设备 ID;
            // password 用户登录密码(可为空)
            SDKAppInit.registerforuid(PLATFLAG, USERNAME, PASSWORD);
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_voice:
                recognizeBySpeech();
                break;
            case R.id.btn_face:
                recognizeByFace();
                break;
            case R.id.btn_expression:
                recognizeByExpression();
                break;
            case R.id.btn_hand:
                Intent intent = new Intent(this, DrawWithTouchActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_finger:
                break;
        }
    }
}
