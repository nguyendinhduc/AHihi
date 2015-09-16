package com.phongbm.common;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.phongbm.ahihi.R;
import com.phongbm.message.MessagesLogDBManager;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClient;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.messaging.Message;
import com.sinch.android.rtc.messaging.MessageClient;
import com.sinch.android.rtc.messaging.MessageClientListener;
import com.sinch.android.rtc.messaging.MessageDeliveryInfo;
import com.sinch.android.rtc.messaging.MessageFailureInfo;
import com.sinch.android.rtc.messaging.WritableMessage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import jp.co.recruit_lifestyle.android.floatingview.FloatingViewListener;
import jp.co.recruit_lifestyle.android.floatingview.FloatingViewManager;

public class AHihiService extends Service implements SinchClientListener,
        FloatingViewListener {
    private static final String TAG = "AHihiService";

    private static final int WIDTH_IMAGE_MAX = 600;
    private static final int HEIGHT_IMAGE_MAX = 800;

    private Context context;
    private SinchClient sinchClient;
    private Call outGoingCall = null, inComingCall = null;
    private AHihiBroadcast aHihiBroadcast = null;
    private String outGoingId = null;

    private MessageListener messageListener;
    private MessageClient messageClient;
    private MessagesLogDBManager messagesLogDBManager = null;
    private CommonMethod commonMethod;
    private String date;

    private FloatingViewManager floatingViewManager;
    private LayoutInflater layoutInflater;
    private RelativeLayout widget;
    private DisplayMetrics displayMetrics;
    private boolean open;

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            context = this;
        }
        // messagesLogDBManager = new MessagesLogDBManager(this);
        // messagesLogDBManager.getData();
        this.registerBroadcast();
        commonMethod = CommonMethod.getInstance();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        open = false;
        displayMetrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);

        if (messagesLogDBManager == null) {
            messagesLogDBManager = new MessagesLogDBManager(this);
        }
        ParseUser currentUser = ParseUser.getCurrentUser();
        outGoingId = currentUser != null ? currentUser.getObjectId() : null;
        if (intent == null) {
            if (outGoingId != null) {
                this.startSinchService();
            }
            return START_STICKY;
        }
        this.startSinchService();
        return Service.START_STICKY;
//        outGoingId = ParseUser.getCurrentUser().getObjectId();
//        loadNearestMessage();
//        return START_NOT_STICKY;
    }

    private void startSinchService() {
        if (sinchClient == null && outGoingId != null) {
            sinchClient = Sinch.getSinchClientBuilder()
                    .context(this)
                    .userId(outGoingId)
                    .applicationKey(ServerInfo.SINCH_APPLICATION_KEY)
                    .applicationSecret(ServerInfo.SINCH_SECRET)
                    .environmentHost(ServerInfo.SINCH_ENVIROMENT)
                    .callerIdentifier(outGoingId)
                    .build();
            sinchClient.setSupportCalling(true);
            sinchClient.setSupportMessaging(true);
            sinchClient.setSupportActiveConnectionInBackground(true);
            sinchClient.addSinchClientListener(this);
            sinchClient.checkManifest();
            sinchClient.start();
        }
    }

    @Override
    public void onClientStarted(SinchClient sinchClient) {
        Log.i(TAG, "onClientStarted...");
        if (messageClient == null) {
            messageListener = new MessageListener();
            messageClient = this.sinchClient.getMessageClient();
            messageClient.addMessageClientListener(messageListener);
            this.sinchClient.startListeningOnActiveConnection();
            this.sinchClient.getCallClient().addCallClientListener(new InComingCallListener());
        }
    }

    @Override
    public void onClientStopped(SinchClient sinchClient) {
    }

    @Override
    public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
    }

    @Override
    public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {

    }

    @Override
    public void onLogMessage(int level, String area, String message) {
    }

    private class OutGoingCallListener implements CallListener {
        @Override
        public void onCallProgressing(Call call) {
        }

        @Override
        public void onCallEstablished(Call call) {
            Intent intentPickUp = new Intent();
            intentPickUp.setAction(CommonValue.STATE_PICK_UP);
            AHihiService.this.sendBroadcast(intentPickUp);
        }

        @Override
        public void onCallEnded(Call call) {
            Intent intentEndCall = new Intent();
            intentEndCall.setAction(CommonValue.STATE_END_CALL);
            AHihiService.this.sendBroadcast(intentEndCall);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    }

    private class InComingCallListener implements CallClientListener, CallListener {
        @Override
        public void onIncomingCall(CallClient callClient, Call call) {
            inComingCall = call;
            inComingCall.addCallListener(this);
            Intent intentInComingCall = new Intent();
            intentInComingCall.setClassName(CommonValue.PACKAGE_NAME_MAIN,
                    CommonValue.PACKAGE_NAME_CALL + ".InComingCallActivity");
            intentInComingCall.putExtra(CommonValue.OUTGOING_CALL_ID, call.getRemoteUserId());
            intentInComingCall.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intentInComingCall);
        }

        @Override
        public void onCallProgressing(Call call) {
        }

        @Override
        public void onCallEstablished(Call call) {
            Intent intentAnswer = new Intent();
            intentAnswer.setAction(CommonValue.STATE_ANSWER);
            AHihiService.this.sendBroadcast(intentAnswer);
        }

        @Override
        public void onCallEnded(Call call) {
            Intent intentEndCall = new Intent();
            intentEndCall.setAction(CommonValue.STATE_END_CALL);
            AHihiService.this.sendBroadcast(intentEndCall);
        }

        @Override
        public void onShouldSendPushNotification(Call call, List<PushPair> list) {
        }
    }

    private class MessageListener implements MessageClientListener {
        @Override
        public void onIncomingMessage(MessageClient messageClient, Message message) {
            /*ActivityManager activityManager = (ActivityManager) AHihiService.this
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();
            Log.i(TAG, tasks.get(0).processName);
            if (tasks.get(0).processName.equals(CommonValue.PACKAGE_NAME_MAIN)) {
            } else {
                if (!open) {
                    open = true;
                    AHihiService.this.openChatHead();
                }
            }*/
//            Log.i(TAG, "onIncomingMessage....");
            String content = message.getTextBody();
            String key = null;
            if (content.contains(CommonValue.AHIHI_KEY)) {
                key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                content = content.substring(CommonValue.KEY_LENGTH + 1);
            }

            if (GlobalApplication.startActivityMessage) {
                Intent intentSent = new Intent();
                intentSent.setAction(CommonValue.STATE_MESSAGE_SENT);
                intentSent.putExtra(CommonValue.AHIHI_KEY_DATE, message.getHeaders().get("date"));
                intentSent.putExtra(CommonValue.AHIHI_KEY, key);
                intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
                AHihiService.this.sendBroadcast(intentSent);
            }
            String id = message.getSenderId();
            Map<String, String> mapheader = message.getHeaders();
            String fullName = mapheader.get("fullName");
            String date = mapheader.get("date");
            String linkAvatar = mapheader.get("linkAvatarSender");
            if ( key != null ) {
                switch (key) {
                    case CommonValue.AHIHI_KEY_EMOTICON:
                        content = "Has emotion";
                        break;
                    case CommonValue.AHIHI_KEY_FILE:
                        content = "Has file";
                        break;
                    case CommonValue.AHIHI_KEY_PICTURE:
                        content = "Has picture";
                        break;
                }
            }
            AHihiService.this.updateMessagesLogDBManager(id, fullName, content, date, linkAvatar);
            if ( !GlobalApplication.startWaittingAHihi ) {
                GlobalApplication.startWaittingAHihi =  true;
                GlobalApplication.checkLoginThisId = false;
                Intent intent = new Intent();
                intent.setAction(CommonValue.MESSAGE_LOG_STOP);
                sendBroadcast(intent);
            }
            if ( GlobalApplication.checkLoginThisId ) {
                Intent intentIncoming = new Intent();
                intentIncoming.setAction(CommonValue.UPDATE_MESSAGE_LOG);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_ID, id);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_FULL_NAME, fullName);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_CONTENT, content);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_DATE, date);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_LINK_AVATAR, linkAvatar);
                AHihiService.this.sendBroadcast(intentIncoming);
            }
//            Intent intentIncoming = new Intent();
//            intentIncoming.setAction(CommonValue.STATE_MESSAGE_INCOMING);
//            intentIncoming.putExtra(CommonValue.AHIHI_KEY_DATE, message.getHeaders().get("date"));
//            if (!content.contains(CommonValue.AHIHI_KEY)) {
//                intentIncoming.putExtra(CommonValue.MESSAGE_CONTENT, content);
//                AHihiService.this.sendBroadcast(intentIncoming);
//            } else {
//                String key = content.substring(0, CommonValue.KEY_LENGTH + 1);
//                content = content.substring(CommonValue.KEY_LENGTH + 1);
//                switch (key) {
//                    case CommonValue.AHIHI_KEY_EMOTICON:
//                    case CommonValue.AHIHI_KEY_FILE:
//                    case CommonValue.AHIHI_KEY_PICTURE:
//                        intentIncoming.putExtra(CommonValue.AHIHI_KEY, key);
//                        intentIncoming.putExtra(CommonValue.MESSAGE_CONTENT, content);
//                        AHihiService.this.sendBroadcast(intentIncoming);
//                        break;
//                }
//            }

            /*final String c = content;
            final String senderId = message.getSenderId();
            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.getInBackground(senderId, new GetCallback<ParseUser>() {
                @Override
                public void done(ParseUser parseUser, ParseException e) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", senderId);
                    contentValues.put("fullName", parseUser.getString("fullName"));
                    contentValues.put("message", c);
                    contentValues.put("date", "26/08/2015");
                    if (!messagesLogDBManager.checkMessagesLogExists(senderId)) {
                        messagesLogDBManager.insertData(contentValues);
                    } else {
                        messagesLogDBManager.update(contentValues);
                    }
                }
            });*/
        }

        @Override
        public void onMessageSent(MessageClient messageClient, final Message message, String s) {
//            Log.i(TAG, "onMessageSent.......");

//            Toast.makeText(AHihiService.this, "onMessageSent...", Toast.LENGTH_SHORT).show();
            String content = message.getTextBody();
            String key = null;
            if (content.contains(CommonValue.AHIHI_KEY)) {
                key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                content = content.substring(CommonValue.KEY_LENGTH + 1);
            }

            if (GlobalApplication.startActivityMessage) {
                Intent intentSent = new Intent();
                intentSent.setAction(CommonValue.STATE_MESSAGE_SENT);
                intentSent.putExtra(CommonValue.AHIHI_KEY_DATE, date);
                intentSent.putExtra(CommonValue.AHIHI_KEY, key);
                intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
                AHihiService.this.sendBroadcast(intentSent);
//                if (!content.contains(CommonValue.AHIHI_KEY)) {
//                    intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
//                    AHihiService.this.sendBroadcast(intentSent);
//                } else {
//                    key = content.substring(0, CommonValue.KEY_LENGTH + 1);
//                    content = content.substring(CommonValue.KEY_LENGTH + 1);
//                    intentSent.putExtra(CommonValue.AHIHI_KEY, key);
//                    switch (key) {
//                        case CommonValue.AHIHI_KEY_EMOTICON:
//                        case CommonValue.AHIHI_KEY_FILE:
//                        case CommonValue.AHIHI_KEY_PICTURE:
//                            intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
//                            AHihiService.this.sendBroadcast(intentSent);
//                            break;
//                    }
//                }
            }

            // AHihiService.this.openChatHead();

            String id = message.getRecipientIds().get(0);
            Map<String, String> mapheader = message.getHeaders();
            String fullName = mapheader.get("fullName");
            String date = mapheader.get("date");
            String linkAvatar = mapheader.get("linkAvatarReceiver");
            if ( key != null ) {
                switch (key) {
                    case CommonValue.AHIHI_KEY_EMOTICON:
                        content = "Has emotion";
                        break;
                    case CommonValue.AHIHI_KEY_FILE:
                        content = "Has file";
                        break;
                    case CommonValue.AHIHI_KEY_PICTURE:
                        content = "Has picture";
                        break;
                }
            }
            AHihiService.this.updateMessagesLogDBManager(id, fullName, "You:  " + content, date, linkAvatar);
            if ( !GlobalApplication.startWaittingAHihi ) {
                GlobalApplication.startWaittingAHihi =  true;
                GlobalApplication.checkLoginThisId = false;
                Intent intent = new Intent();
                intent.setAction(CommonValue.MESSAGE_LOG_STOP);
                sendBroadcast(intent);
            }
            if ( GlobalApplication.checkLoginThisId ) {
                Intent intentMessage = new Intent();
                intentMessage.setAction(CommonValue.UPDATE_MESSAGE_LOG);
                intentMessage.putExtra(CommonValue.MESSAGE_LOG_ID, id);
                intentMessage.putExtra(CommonValue.MESSAGE_LOG_FULL_NAME, fullName);
                intentMessage.putExtra(CommonValue.MESSAGE_LOG_CONTENT, "You: " + content);
                intentMessage.putExtra(CommonValue.MESSAGE_LOG_DATE, date);
                intentMessage.putExtra(CommonValue.MESSAGE_LOG_LINK_AVATAR, linkAvatar);
                AHihiService.this.sendBroadcast(intentMessage);
            }
        }


        @Override
        public void onMessageFailed(MessageClient messageClient, Message message,
                                    MessageFailureInfo messageFailureInfo) {
//            Log.i(TAG, "onMessageFailed...");
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
//            Log.i(TAG, "onMessageDelivered...");
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {
        }

    }

    private void registerBroadcast() {
        if (aHihiBroadcast == null) {
            aHihiBroadcast = new AHihiBroadcast();
            IntentFilter intentFilter = new IntentFilter();
            // Call
            intentFilter.addAction(CommonValue.ACTION_OUTGOING_CALL);
            intentFilter.addAction(CommonValue.ACTION_END_CALL);
            intentFilter.addAction(CommonValue.ACTION_ANSWER);
            intentFilter.addAction(CommonValue.ACTION_LOGOUT);
            // Message
            intentFilter.addAction(CommonValue.ACTION_SEND_MESSAGE);
            context.registerReceiver(aHihiBroadcast, intentFilter);
        }
    }

    private class AHihiBroadcast extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                // Call
                case CommonValue.ACTION_OUTGOING_CALL:
                    String inComingCallId = intent.getStringExtra(CommonValue.INCOMING_CALL_ID);
                    outGoingCall = sinchClient.getCallClient().callUser(inComingCallId);
                    outGoingCall.addCallListener(new OutGoingCallListener());
                    break;
                case CommonValue.ACTION_END_CALL:
                    if (outGoingCall != null) {
                        outGoingCall.hangup();
                        outGoingCall = null;
                    }
                    if (inComingCall != null) {
                        inComingCall.hangup();
                        inComingCall = null;
                    }
                    break;
                case CommonValue.ACTION_ANSWER:
                    if (inComingCall != null) {
                        inComingCall.answer();
                    }
                    break;
                case CommonValue.ACTION_LOGOUT:
                    messageClient.removeMessageClientListener(messageListener);
                    messageClient = null;

                    sinchClient.stopListeningOnActiveConnection();
                    sinchClient.terminate();
                    sinchClient = null;
                    messagesLogDBManager.deleteAllData();
                    GlobalApplication.checkLoginThisId = false;
                    GlobalApplication.startActivityMessage = false;
                    GlobalApplication.startWaittingAHihi = false;
                    break;

                // Message
                case CommonValue.ACTION_SEND_MESSAGE:
                    String id = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_ID);
                    String fullName = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME);
                    String content = intent.getStringExtra(CommonValue.MESSAGE_CONTENT);
                    date = intent.getStringExtra(CommonValue.AHIHI_KEY_DATE);
                    String linkAvatarReceiver = intent.getStringExtra(CommonValue.MESSAGE_LOG_LINK_AVATAR_RECEVER);
                    if (intent.getStringExtra(CommonValue.AHIHI_KEY) == null) {
                        AHihiService.this.sendMessage(id, content, fullName, date, linkAvatarReceiver);
                    } else {
                        switch (intent.getStringExtra(CommonValue.AHIHI_KEY)) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                content = CommonValue.AHIHI_KEY_EMOTICON + content;
                                AHihiService.this.sendMessage(id, content, fullName, date, linkAvatarReceiver);
                                break;
                            case CommonValue.AHIHI_KEY_FILE:
                                AHihiService.this.sendFile(id, content, fullName, date, linkAvatarReceiver);
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                AHihiService.this.sendPicture(id, content, fullName, date, linkAvatarReceiver);
                                break;
                        }
                    }
                    break;
            }
        }
    }

    private synchronized void sendMessage(final String id, final String content,
                                          final String fullName, final String date, final String linkAvatarReceiver) {
        final ParseObject message = new ParseObject("Message");
        message.put("senderId", outGoingId);
        message.put("receiverId", id);
        message.put("content", content);
        message.put("date", date);
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                WritableMessage message = new WritableMessage(id, content);
                message.addHeader("date", date);
                message.addHeader("fullName", fullName);
                message.addHeader("linkAvatarSender", GlobalApplication.linkAvatarSender);
                message.addHeader("linkAvatarReceiver", linkAvatarReceiver);
                messageClient.send(message);
            }
        });
    }

    private synchronized void sendFile(final String id, final String path,
                                       final String fullName, final String date, final String linkAvatarReceiver) {
        if (messageClient == null) {
            return;
        }
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                final String fileName = path.substring(path.lastIndexOf("/") + 1);
                final File file = new File(path);
                if (!file.exists() || !file.isFile() || file.length() > CommonValue.MAX_FILE_SIZE) {
                    return;
                }
                byte[] bytes = new byte[(int) file.length()];
                try {
                    FileInputStream fileInputStream = new FileInputStream(file);
                    fileInputStream.read(bytes);
                    fileInputStream.close();
                    final ParseFile parseFile = new ParseFile(fileName, bytes);
                    parseFile.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (e != null) {
                                Toast.makeText(AHihiService.this, "Missing send file",
                                        Toast.LENGTH_SHORT).show();
                                return;
                            }
                            final ParseObject parseObject = new ParseObject("Message");
                            parseObject.put("file", parseFile);
                            parseObject.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        Log.i(TAG, "ERROR: " + e.getMessage());
                                        Toast.makeText(AHihiService.this, "Error send file",
                                                Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    parseObject.put("senderId", outGoingId);
                                    parseObject.put("receiverId", id);
                                    final String content = CommonValue.AHIHI_KEY_FILE +
                                            parseObject.getObjectId();
                                    parseObject.put("content", content + "/" + fileName);
                                    parseObject.put("date", date);
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            WritableMessage message = new WritableMessage(id,
                                                    content + "/" + fileName);
                                            message.addHeader("date", date);
                                            message.addHeader("fullName", fullName);
                                            message.addHeader("linkAvatarSender", GlobalApplication.linkAvatarSender);
                                            message.addHeader("linkAvatarReceiver", linkAvatarReceiver);
                                            messageClient.send(message);
                                        }
                                    });
                                }
                            });
                        }
                    }, new ProgressCallback() {
                        @Override
                        public void done(Integer integer) {
                            if (integer > 50) parseFile.cancel();
//                            Log.i(TAG, "Percent: " + integer);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized void sendPicture(final String id, final String pathPicture,
                                          final String fullName, final String date, final String linkAvatarReceiver) {
        if (messageClient == null) {
            return;
        }
        final Bitmap[] bitmapSend = {this.createSenderBitmap(pathPicture)};
        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean isChangeByte = bitmapSend[0].compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        if (!isChangeByte) {
            Toast.makeText(AHihiService.this, "Send image not success!!!", Toast.LENGTH_SHORT).show();
            return;
        }
        final byte[] bytes = byteArrayOutputStream.toByteArray();
        final ParseFile parseFile = new ParseFile(bytes);
        parseFile.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    Toast.makeText(AHihiService.this, "Send image not success!!!", Toast.LENGTH_SHORT).show();
                    Log.i(TAG, "sendImage_ERROR parsefile: " + e.getMessage());
                    return;
                }
                final ParseObject parseObject = new ParseObject("Message");
                parseObject.put("file", parseFile);
                parseObject.put("senderId", outGoingId);
                parseObject.put("receiverId", id);
                final String content = CommonValue.AHIHI_KEY_PICTURE + parseFile.getUrl();
                parseObject.put("content", content);
                parseObject.put("date", date);
                parseObject.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e != null) {
                            Toast.makeText(AHihiService.this, "Send image not success!!!",
                                    Toast.LENGTH_SHORT).show();
                            Log.i(TAG, "sendImage_ERROR parseObject 1: " + e.getMessage());
                            return;
                        }
                        new Handler().post(new Runnable() {
                            @Override
                            public void run() {
                                bitmapSend[0] = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                ((GlobalApplication) getApplication()).setPictureSend(bitmapSend[0]);
                            }
                        });
                        WritableMessage message = new WritableMessage(id, content);
                        message.addHeader("date", date);
                        message.addHeader("fullName", fullName);
                        message.addHeader("linkAvatarSender", GlobalApplication.linkAvatarSender);
                        message.addHeader("linkAvatarReceiver", linkAvatarReceiver);
                        AHihiService.this.messageClient.send(message);
                    }
                });
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
//                Log.i(TAG, "Percent: " + integer);
            }
        });
    }

    private Bitmap createSenderBitmap(String path) {
        Bitmap bitmapSend = null;
        try {
            bitmapSend = BitmapFactory.decodeFile(path);
            if (bitmapSend.getWidth() > WIDTH_IMAGE_MAX
                    || bitmapSend.getHeight() > HEIGHT_IMAGE_MAX) {
                Pair<Integer, Integer> pair = CommonMethod.getInstance()
                        .getStandSizeBitmap(bitmapSend.getWidth(), bitmapSend.getHeight(),
                                WIDTH_IMAGE_MAX, HEIGHT_IMAGE_MAX);
                bitmapSend = Bitmap.createScaledBitmap(bitmapSend, pair.first, pair.second, true);
                /*bitmapSend = CommonMethod.getInstance().decodeSampledBitmapFromResource(path,
                        pair.first, pair.second);*/
                Toast.makeText(this, "IF ELSE", Toast.LENGTH_SHORT).show();
            }
        } catch (OutOfMemoryError e) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int width = options.outWidth;
            int height = options.outHeight;
            Pair<Integer, Integer> pair = CommonMethod.getInstance().getStandSizeBitmap(width,
                    height, WIDTH_IMAGE_MAX, HEIGHT_IMAGE_MAX);
            bitmapSend = Bitmap.createScaledBitmap(bitmapSend, pair.first, pair.second, true);
            /*bitmapSend = CommonMethod.getInstance().decodeSampledBitmapFromResource(path,
                    pair.first, pair.second);*/
            Toast.makeText(this, "OutOfMemoryError...", Toast.LENGTH_SHORT).show();
        }
        int orientation = CommonMethod.getInstance().getOrientation(path);
        bitmapSend = CommonMethod.getInstance().getBitmap(orientation, bitmapSend);
        return bitmapSend;
    }

    @Override
    public void onFinishFloatingView() {
        open = false;
    }

    private void openChatHead() {
        layoutInflater = LayoutInflater.from(this);
        widget = (RelativeLayout) layoutInflater.inflate(R.layout.widget_chathead, null, false);
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(AHihiService.this, "OK", Toast.LENGTH_SHORT).show();
            }
        });

        floatingViewManager = new FloatingViewManager(this, this);
        floatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        floatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        floatingViewManager.addViewToWindow(widget, FloatingViewManager.SHAPE_CIRCLE,
                (int) (16 * displayMetrics.density));
    }

    private synchronized void updateMessagesLogDBManager(String id, String fullName, String message,
                                            String date, String linkAvatar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("fullName", fullName);
        contentValues.put("message", message);
        contentValues.put("date", date);
        contentValues.put("isRead", false);
        contentValues.put("linkAvatar", linkAvatar);
        if (messagesLogDBManager.conversationExist(id)) {
            messagesLogDBManager.update(contentValues);
//            Log.i(TAG, "updateMessagesLogDBManager update");
        } else {
            messagesLogDBManager.insertData(contentValues);
//            Log.i(TAG, "updateMessagesLogDBManager insert");
        }
    }

    @Override
    public void onDestroy() {
        if (floatingViewManager != null) {
            floatingViewManager.removeAllViewToWindow();
            floatingViewManager = null;
        }
        messagesLogDBManager.closeDatabase();
        this.unregisterReceiver(aHihiBroadcast);
        super.onDestroy();
    }

}