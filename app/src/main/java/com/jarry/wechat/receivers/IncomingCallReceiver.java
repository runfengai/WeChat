package com.jarry.wechat.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.sip.SipAudioCall;
import android.net.sip.SipException;
import android.net.sip.SipProfile;
import android.widget.Toast;

import com.jarry.wechat.ui.VoipActivity;

/**
 * Created by Administrator on 2017/12/3.
 */

public class IncomingCallReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (intent.getAction().equals(VoipActivity.SIP_CALL)) {
            Toast.makeText(context, "来电话拉！", Toast.LENGTH_SHORT).show();
            SipAudioCall incomingCall = null;
            final SipAudioCall.Listener listener = new SipAudioCall.Listener() {
                @Override
                public void onRinging(SipAudioCall call, SipProfile caller) {
                    super.onRinging(call, caller);
                    try {
                        call.answerCall(30);
                    } catch (SipException e) {
                        e.printStackTrace();
                    }
                }
            };
            VoipActivity voipActivity = (VoipActivity) context;
            //收到电话
            try {
                incomingCall = voipActivity.sipManager.takeAudioCall(intent, listener);
                voipActivity.onReceiveCall(incomingCall);
            } catch (SipException e) {
                e.printStackTrace();
            }
        }
    }
}
