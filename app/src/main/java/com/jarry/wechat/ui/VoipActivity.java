package com.jarry.wechat.ui;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipManager;
import android.net.sip.SipProfile;
import android.net.sip.SipRegistrationListener;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jarry.wechat.R;
import com.jarry.wechat.receivers.IncomingCallReceiver;

import java.text.ParseException;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * voip通话
 */
public class VoipActivity extends BaseActivity {
    public static final String SIP_DOMAIN = "192.168.2.2";
    public static final String SIP_CALL = "android.Sip.INCOMING_CALL";
    public static final String TAG = "VoipActivity";
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.text_state)
    TextView stateTextView;
    @BindView(R.id.sip_account)
    AutoCompleteTextView sipAccountTextView;
    @BindView(R.id.sip_peeraccount)
    AutoCompleteTextView sipPeerAccountTextView;
    @BindView(R.id.sip_pwd)
    AutoCompleteTextView sipPwdTextView;
    @BindView(R.id.call_from)
    TextView callFromTextView;
    @BindView(R.id.lin_receive)
    LinearLayout receiveLinearLayout;
    //为SIP任务提供APIs，比如初始化一个SIP连接
    public SipManager sipManager;
    //定义了SIP的相关属性，包含SIP账户、域名和服务器信息
    private SipProfile meSipProfile;
    private SipProfile peerProfile;

    public SipAudioCall peerCall;
    public SipAudioCall myCall;

    @Override
    public int setLayout() {
        return R.layout.activity_voip;
    }

    private IncomingCallReceiver callReceiver;

    @Override
    public void initView() {
        ButterKnife.bind(this);
        //注册广播
        IntentFilter filter = new IntentFilter();
        filter.addAction(SIP_CALL);
        callReceiver = new IncomingCallReceiver();
        this.registerReceiver(callReceiver, filter);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    @Override
    public void initData() {
        sipManager = SipManager.newInstance(this);
    }

    @Override
    public void initListener() {
        setSupportActionBar(toolbar);
//        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_req:
                        //请求通话
                        Log.e(TAG, "请求通话");

                        call();
                        break;
                    case R.id.action_off:
                        Log.e(TAG, "挂断");
                        if (peerCall != null) {
                            try {
                                peerCall.endCall();
                            } catch (SipException se) {
                                se.printStackTrace();
                            }
                            peerCall.close();
                        }
                        break;
                }
                return false;
            }
        });
    }

    @Override
    public void setMore() {

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @OnClick(R.id.btn_login)
    public void login(View v) {
        String acc = sipAccountTextView.getText().toString();
        String pwd = sipPwdTextView.getText().toString();
        if (TextUtils.isEmpty(acc)) {
            Toast.makeText(VoipActivity.this, "账号为空", Toast.LENGTH_SHORT).show();
            return;
        }
        if (TextUtils.isEmpty(pwd)) {
            Toast.makeText(VoipActivity.this, "密码为空", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            SipProfile.Builder builder = new SipProfile.Builder(acc, SIP_DOMAIN);
            builder.setPassword(pwd);
            meSipProfile = builder.build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        Intent i = new Intent();
        i.setAction(SIP_CALL);
        PendingIntent pi = PendingIntent.getBroadcast(this, 0, i, Intent.FILL_IN_DATA);
        try {
            sipManager.open(meSipProfile, pi, null);
        } catch (SipException e) {
            e.printStackTrace();
        }
        setRegListener();
    }

    private void setRegListener() {

        //监 听器会跟踪SipProfile是否成功的注册到你的SIP服务提供者
        try {
            sipManager.setRegistrationListener(meSipProfile.getUriString(), new SipRegistrationListener() {
                @Override
                public void onRegistering(String localProfileUri) {
                    updateStatus("正在向服务器注册...");
                }

                @Override
                public void onRegistrationDone(String localProfileUri, long expiryTime) {
                    updateStatus("就绪");
                }

                @Override
                public void onRegistrationFailed(String localProfileUri, int errorCode, String errorMessage) {
                    updateStatus("注册失败，请检查配置");
                }
            });
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    //PeerProfile就是目标设备的SipProfile对象,listener是监听器
    private void call() {
        String acc = sipPeerAccountTextView.getText().toString();
        if (TextUtils.isEmpty(acc)) {
            Toast.makeText(VoipActivity.this, "对方账号为空", Toast.LENGTH_SHORT).show();
            return;
        }
        SipAudioCall.Listener listener = new SipAudioCall.Listener() {
            @Override
            public void onCallEstablished(SipAudioCall call) {
                myCall.startAudio(); //启动音频
                myCall.setSpeakerMode(true); //调整为可讲话模式
                myCall.toggleMute(); //触发无声
                Log.e(TAG, "onCallEstablished");
//                updateStatus(myCall);
            }

            @Override
            public void onCallEnded(SipAudioCall call) {
                super.onCallEnded(call);
                Log.e(TAG, "onCallEnded");
            }
        };
        //对方信息
        try {
            SipProfile.Builder builder = new SipProfile.Builder(acc, SIP_DOMAIN);
            peerProfile = builder.build();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            myCall = sipManager.makeAudioCall(meSipProfile, peerProfile, listener, 30);
        } catch (SipException e) {
            e.printStackTrace();
        }
    }

    //收到电话
    public void onReceiveCall(SipAudioCall incomingCall) {
        this.peerCall = incomingCall;
        receiveLinearLayout.setVisibility(View.VISIBLE);
        callFromTextView.setText("" + peerCall.getPeerProfile().getUserName()
                + "\nUriString：" + peerCall.getPeerProfile().getUriString()
        );
        //收到电话
        updateStatus("收到电话来自：" + peerCall.getLocalProfile().getUserName());
    }

    public void updateStatus(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                stateTextView.setText("状态：\n" + msg);
            }
        });
    }

    public void closeLocalProfile() {
        if (sipManager == null) {
            return;
        }
        try {
            if (meSipProfile != null) {
                sipManager.close(meSipProfile.getUriString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        closeLocalProfile();
        if (callReceiver != null)
            unregisterReceiver(callReceiver);
    }


    @OnClick(R.id.btn_accept)
    public void accept(View v) {
        if (peerCall != null) {
            try {
                peerCall.answerCall(30);
                peerCall.startAudio();
                peerCall.setSpeakerMode(true);
                if (peerCall.isMuted()) {
                    peerCall.toggleMute();
                }
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }

    @OnClick(R.id.btn_reject)
    public void reject(View v) {
        if (peerCall != null) {
            try {
                peerCall.endCall();
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }
}
