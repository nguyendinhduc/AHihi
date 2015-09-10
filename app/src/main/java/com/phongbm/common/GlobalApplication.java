package com.phongbm.common;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.parse.Parse;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.util.ArrayList;
import java.util.List;

public class GlobalApplication extends Application implements SinchClientListener, MessageClientListener {
    public static int WIDTH_SCREEN, HEIGHT_SCREEN;
    public static float DENSITY;

    private Bitmap avatar;
    private Bitmap pictureSend;
    private String inComingFullName = null;

    private SinchClient sinchClient;
    private MessageClient messageClient;

    private int cout = -1;

    private ArrayList<String> idUsers;

    private BroadcastGlobalApplication broadcastGlobalApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        registerBroadcastGlobalApplication();
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, ServerInfo.PARSE_APPLICATION_ID, ServerInfo.PARSE_CLIENT_KEY);
        initializeComponent();
        idUsers = new ArrayList<>();
    }

    private void initializeComponent() {
        WindowManager windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        WIDTH_SCREEN = metrics.widthPixels;
        HEIGHT_SCREEN = metrics.heightPixels;
        DENSITY = metrics.densityDpi;
    }

    public Bitmap getAvatar() {
        return avatar;
    }

    public void setAvatar(Bitmap avatar) {
        this.avatar = avatar;
    }

    public Bitmap getPictureSend() {
        return pictureSend;
    }

    public void setPictureSend(Bitmap pictureSend) {
        this.pictureSend = pictureSend;
    }

    public void setInComingFullName( String inComingFullName ) {
        this.inComingFullName = inComingFullName;
    }
    public String getInComingFullName() {
        return inComingFullName;
    }
    private synchronized void startSinchService(String idUser) {
        if (sinchClient != null) {
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
            sinchClient = null;
        }
        sinchClient = Sinch.getSinchClientBuilder()
                .context(this)
                .userId(idUser)
                .applicationKey(ServerInfo.SINCH_APPLICATION_KEY)
                .applicationSecret(ServerInfo.SINCH_SECRET)
                .environmentHost(ServerInfo.SINCH_ENVIROMENT)
                .build();
        sinchClient.setSupportCalling(true);
        sinchClient.setSupportMessaging(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.addSinchClientListener(this);
        sinchClient.checkManifest();
        sinchClient.start();
    }

    @Override
    public synchronized void onClientStarted(SinchClient sinchClient) {
        messageClient = this.sinchClient.getMessageClient();
        messageClient.addMessageClientListener(this);
        this.sinchClient.startListeningOnActiveConnection();
        WritableMessage message = new WritableMessage("1cSWKDINiZ", "HELLO ANDROID");
        messageClient.send(message);
    }

    @Override
    public synchronized void onClientStopped(SinchClient sinchClient) {

    }

    @Override
    public synchronized void onClientFailed(SinchClient sinchClient, SinchError sinchError) {

    }

    @Override
    public synchronized void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

    }

    @Override
    public synchronized void onLogMessage(int i, String s, String s1) {

    }

    @Override
    public synchronized void onIncomingMessage(MessageClient messageClient, Message message) {
    }

    @Override
    public synchronized void onMessageSent(MessageClient messageClient, Message message, String s) {
       synchronized (this){
           Log.i("Application", "onMessageSent...");
           if ( cout %2 == 0 ) {
               if ( cout >= 100 ) cout = 0;
               cout ++;
               if (sinchClient != null) {
                   sinchClient.stopListeningOnActiveConnection();
                   sinchClient.terminate();
                   sinchClient = null;
               }
               // messageClient.removeMessageClientListener(this);
               Intent i = new Intent();
               i.setAction(CommonValue.RESULT_START_SERVICE);
               sendBroadcast(i);
           }
           Log.i("Application", "onMessageSent...finish");
       }
    }

    @Override
    public synchronized void onMessageFailed(MessageClient messageClient, Message message, MessageFailureInfo messageFailureInfo) {

    }

    @Override
    public synchronized void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {

    }

    @Override
    public synchronized void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {

    }

    private void registerBroadcastGlobalApplication() {
        if (broadcastGlobalApplication == null) {
            broadcastGlobalApplication = new BroadcastGlobalApplication();
            IntentFilter filter = new IntentFilter();
            filter.addAction(CommonValue.START_FIRST_SINCH);
            registerReceiver(broadcastGlobalApplication, filter);
        }
    }

    private class BroadcastGlobalApplication extends BroadcastReceiver {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            String id = intent.getStringExtra(CommonValue.ID_START_FIRST_SINCH);
            if (!idUsers.contains(id)) {
                cout++;
                Log.i("GlobalApplication", "BroadcastGlobalApplication...");
                idUsers.add(id);
                GlobalApplication.this.startSinchService(id);
            } else {
                Intent i = new Intent();
                i.setAction(CommonValue.RESULT_START_SERVICE);
                sendBroadcast(i);
            }
        }
    }

    public void addIdUser( String idUser ) {
        idUsers.add(idUser);
    }

    @Override
    public void onTerminate() {
        if (broadcastGlobalApplication != null) {
            unregisterReceiver(broadcastGlobalApplication);
            broadcastGlobalApplication = null;
        }
        super.onTerminate();
    }
}