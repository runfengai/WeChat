package com.jarry.wechat.ui;

import android.app.Activity;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

import com.jarry.wechat.R;
import com.jarry.wechat.utils.Constants;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.chat2.ChatManager;
import org.jivesoftware.smack.chat2.IncomingChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jxmpp.jid.DomainBareJid;
import org.jxmpp.jid.EntityBareJid;
import org.jxmpp.jid.impl.JidCreate;
import org.jxmpp.stringprep.XmppStringprepException;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {
    static final String TAG = "LoginActivity";

    public XMPPConnection connection = null;//连接
    @BindView(R.id.email)
    AutoCompleteTextView textView;

    @BindView(R.id.password)
    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);
        IntentService S;
    }

    public void login(View view) {
        Executors.newScheduledThreadPool(2).execute(XmppThread);
//        new Thread(XmppThread).start();
    }


    private XMPPTCPConnection con;
    private Runnable XmppThread = new Runnable() {

        public void run() {
            boolean tag = false;
            InetAddress addr = null;
            try {
                addr = InetAddress.getByName(Constants.server_host);
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            HostnameVerifier verifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    return false;
                }
            };
            XMPPTCPConnectionConfiguration.Builder config = XMPPTCPConnectionConfiguration.builder();
            config.setHost(Constants.server_host);              //设置ejabberd主机IP
            try {
                DomainBareJid serviceName = JidCreate.domainBareFrom(Constants.server_name);
                config.setServiceName(serviceName);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
            //设置ejabberd服务器名称
            config.setPort(Constants.server_port);                   //设置端口号：默认5222
            try {
                config.setXmppDomain(Constants.server_name);
            } catch (XmppStringprepException e) {
                e.printStackTrace();
            }
            config.setHostnameVerifier(verifier);
            config.setHostAddress(addr);
            config.setUsernameAndPassword(textView.getText().toString(), editText.getText().toString());    //设置用户名与密码
            config.setSecurityMode(ConnectionConfiguration.SecurityMode.disabled);      //禁用SSL连接
            config.setSendPresence(true);
            config.setDebuggerEnabled(true);
            con = new XMPPTCPConnection(config.build());
            try {
                con.connect();
                tag = con.isConnected();
                Roster roster = Roster.getInstanceFor(con);

                con.login();
                Log.e(TAG, "login sucess!!");
                //-------------------
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                    }
                });

                //         设置聊天监听器，监听聊天消息
                ChatManager chatm = ChatManager.getInstanceFor(con);
                chatm.addIncomingListener(new IncomingChatMessageListener() {
                    @Override
                    public void newIncomingMessage(EntityBareJid from, Message message, org.jivesoftware.smack.chat2.Chat chat) {
                        Log.e(TAG, "message:" + message);
                    }
                });

            } catch (Exception e) {
                Log.e("TAG", "connect failed!" + e.toString());
                e.printStackTrace();

            }

        }
    };
}

