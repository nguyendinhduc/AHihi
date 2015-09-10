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
import android.util.Log;
import android.util.Pair;
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
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
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AHihiService extends Service implements SinchClientListener {
    private static final String TAG = "AHihiService";

    private static final int WITH_SENDER_IMAGE_MAX = 600;
    private static final int HEIGHT_SEND_IMAGE_MAX = 800;

    private Context context;
    private SinchClient sinchClient;
    private Call outGoingCall = null, inComingCall = null;
    private AHihiBroadcast aHihiBroadcast = null;
    private String outGoingId = null;

    private MessageListener messageListener;
    private MessageClient messageClient;
    private SimpleDateFormat dateFormat;
    private MessagesLogDBManager messagesLogDBManager = null;
    // private MessagesLogDBManager messagesLogDBManager;

    @Override
    public void onCreate() {
        super.onCreate();
        dateFormat = new SimpleDateFormat("yyyy/MM/dd hh:mm:ss");
        if (context == null) {
            context = this;
        }
        // messagesLogDBManager = new MessagesLogDBManager(this);
        // messagesLogDBManager.getData();
        this.registerBroadcast();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if ( messagesLogDBManager == null ) messagesLogDBManager = new MessagesLogDBManager(this);
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
    }

    private void startSinchService() {
        Log.i(TAG, "startSinchService....");
        if ( sinchClient != null ) {
            sinchClient.stopListeningOnActiveConnection();
            sinchClient.terminate();
        }
        if (sinchClient == null) {
            sinchClient = Sinch.getSinchClientBuilder()
                    .context(this)
                    .userId(outGoingId)
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
    public void onLogMessage(int i, String s, String s1) {
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

            Log.i(TAG, "onIncomingMessage....");
            Log.i(TAG, "onIncomingMessage_ SenderId: " + message.getSenderId());
            String content = message.getTextBody();
            Intent intentIncoming = new Intent();
            intentIncoming.setAction(CommonValue.STATE_MESSAGE_INCOMING);
            if (!content.contains(CommonValue.AHIHI_KEY)) {
                intentIncoming.putExtra(CommonValue.MESSAGE_CONTENT, content);
                AHihiService.this.sendBroadcast(intentIncoming);
            } else {
                String key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                content = content.substring(CommonValue.KEY_LENGTH + 1);
                switch (key) {
                    case CommonValue.AHIHI_KEY_EMOTICON:
                    case CommonValue.AHIHI_KEY_FILE:
                    case CommonValue.AHIHI_KEY_PICTURE:
                        intentIncoming.putExtra(CommonValue.AHIHI_KEY, key);
                        intentIncoming.putExtra(CommonValue.MESSAGE_CONTENT, content);
                        AHihiService.this.sendBroadcast(intentIncoming);
                        break;
                }
            }
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
        public void onMessageSent(MessageClient messageClient, Message message, String s) {
            Toast.makeText(AHihiService.this, "onMessageSent...", Toast.LENGTH_SHORT).show();
            Log.i(TAG, "onMessageSent....");
            Log.i(TAG, "onMessageSent_ SenderId: " + message.getSenderId());
            String content = message.getTextBody();
            Intent intentSent = new Intent();
            intentSent.setAction(CommonValue.STATE_MESSAGE_SENT);
            String key = CommonValue.AHIHI_KEY;
            if (!content.contains(CommonValue.AHIHI_KEY)) {
                intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
                AHihiService.this.sendBroadcast(intentSent);
            } else {
                key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                content = content.substring(CommonValue.KEY_LENGTH + 1);
                intentSent.putExtra(CommonValue.AHIHI_KEY, key);
                switch (key) {
                    case CommonValue.AHIHI_KEY_EMOTICON:
                    case CommonValue.AHIHI_KEY_FILE:
                        intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
                        AHihiService.this.sendBroadcast(intentSent);
                        break;
                    case CommonValue.AHIHI_KEY_PICTURE:
                        AHihiService.this.sendBroadcast(intentSent);
                        break;
                }
            }
            Map<String, String> map = message.getHeaders();
            updateMessagesLogDBManager(message.getRecipientIds().get(0),
                    map.get("fullName"), content, map.get("date"));

            Intent intentUpdateNearestMessage = new Intent();
            intentUpdateNearestMessage.setAction(CommonValue.UPDATE_NEAREST_MESSAGE);
            intentUpdateNearestMessage.putExtra(CommonValue.UPDATE_NEAREST_USER, CommonValue.UPDATE_NEAAREST_MY);
            intentUpdateNearestMessage.putExtra(CommonValue.UPDATE_NEAREST_TYPE, key);
            intentUpdateNearestMessage.putExtra(CommonValue.ID_NEAREST_MESSAGE, message.getRecipientIds().get(0));
            intentUpdateNearestMessage.putExtra(CommonValue.FULL_NAME_NEAREST_MESSAGE, map.get("fullName"));
            intentUpdateNearestMessage.putExtra(CommonValue.CONTENT_NEAREST_MESSAGE, content);
            intentUpdateNearestMessage.putExtra(CommonValue.DATE_NEAREST_MESSAGE, map.get("date"));
            sendBroadcast(intentUpdateNearestMessage);

        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message,
                                    MessageFailureInfo messageFailureInfo) {
            Log.i(TAG, "onMessageFailed...");
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
            Log.i(TAG, "onMessageDelivered...");
        }

        @Override
        public void onShouldSendPushData(MessageClient messageClient, Message message, List<PushPair> list) {
        }

    }

    private void updateMessagesLogDBManager(String id, String fullName
            ,String message, String date )  {
        ContentValues contentValues = new ContentValues();
        if ( messagesLogDBManager.CheckIsDataAlreadyInDBorNot("id", id) ) {
            contentValues.put("id", id);
            contentValues.put("message", message);
            contentValues.put("date", date);
            messagesLogDBManager.update(contentValues);
            Log.i(TAG, "updateMessagesLogDBManager update");
        }
        else {
            contentValues.put("id", id);
            contentValues.put("fullName", fullName);
            contentValues.put("message", message);
            contentValues.put("date", date);
            messagesLogDBManager.insertData(contentValues);
            Log.i(TAG, "updateMessagesLogDBManager insert");
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
                    break;

                // Message
                case CommonValue.ACTION_SEND_MESSAGE:
                    ((GlobalApplication)AHihiService.this.getApplication())
                            .setInComingFullName(intent.getStringExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME));
                    String id = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_ID);
                    String content = intent.getStringExtra(CommonValue.MESSAGE_CONTENT);
                    if (intent.getStringExtra(CommonValue.AHIHI_KEY) == null) {
                        AHihiService.this.sendMessage(id, content);
                    } else {
                        switch (intent.getStringExtra(CommonValue.AHIHI_KEY)) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                content = CommonValue.AHIHI_KEY_EMOTICON + content;
                                AHihiService.this.sendMessage(id, content);
                                break;
                            case CommonValue.AHIHI_KEY_FILE:
                                AHihiService.this.sendFile(id, content);
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                Log.i(TAG, "ACTION_SEND_MESSAGE...CommonValue.AHIHI_KEY_PICTURE...");
                                AHihiService.this.sendPicture(id, content);
                                break;
                        }
                    }
                    break;
            }
        }
    }

    private synchronized void sendMessage(final String id, final String content) {
        ParseObject message = new ParseObject("Message");
        message.put("senderId", outGoingId);
        message.put("receiverId", id);
        message.put("content", content);
        message.put("date", AHihiService.this.dateFormat.format(System.currentTimeMillis()));
        message.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                    return;
                }
                WritableMessage message = new WritableMessage(id, content);
                message.addHeader("fullName",
                        ((GlobalApplication)AHihiService.this.getApplication()).getInComingFullName());
                message.addHeader("date", dateFormat.format(System.currentTimeMillis()));
                messageClient.send(message);
            }
        });
    }

    private synchronized void sendFile(final String id, final String path) {
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
                                    parseObject.put("date", AHihiService.this.dateFormat.format(System.currentTimeMillis()));
                                    parseObject.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            WritableMessage message = new WritableMessage(id,
                                                    content + "/" + fileName);
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
                            Log.i(TAG, "Percent: " + integer);
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private synchronized void sendPicture(final String id, final String pathPicture) {
        if (messageClient == null) {
            return;
        }
        final Bitmap bitmapSend = this.createSenderBitmap(pathPicture);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        boolean isChangeByte = bitmapSend.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
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
                parseObject.put("date", AHihiService.this.dateFormat.format(System.currentTimeMillis()));
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
                                ((GlobalApplication) getApplication()).setPictureSend(bitmapSend);
                            }
                        });
                        WritableMessage message = new WritableMessage(id, content);
                        AHihiService.this.messageClient.send(message);
                    }
                });
            }
        }, new ProgressCallback() {
            @Override
            public void done(Integer integer) {
                Log.i(TAG, "Percent: " + integer);
            }
        });
    }

    private Bitmap createSenderBitmap(String path) {
        Bitmap bitmapSend = null;
        try {
            bitmapSend = BitmapFactory.decodeFile(path);
            if (bitmapSend.getWidth() > WITH_SENDER_IMAGE_MAX
                    || bitmapSend.getHeight() > HEIGHT_SEND_IMAGE_MAX) {
                Pair<Integer, Integer> pair = CommonMethod.getInstance()
                        .getStandSizeBitmap(bitmapSend.getWidth(), bitmapSend.getHeight(),
                                WITH_SENDER_IMAGE_MAX, HEIGHT_SEND_IMAGE_MAX);
                bitmapSend = Bitmap.createScaledBitmap(bitmapSend, pair.first, pair.second, true);
            }
        } catch (OutOfMemoryError e) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(path, options);
            int width = options.outWidth;
            int height = options.outHeight;
            Pair<Integer, Integer> pair = CommonMethod.getInstance().getStandSizeBitmap(width,
                    height, WITH_SENDER_IMAGE_MAX, HEIGHT_SEND_IMAGE_MAX);
            bitmapSend = CommonMethod.getInstance().decodeSampledBitmapFromResource(path,
                    pair.first, pair.second);
        }
        int orientation = CommonMethod.getInstance().getOrientation(path);
        bitmapSend = CommonMethod.getInstance().getBitmap(orientation, bitmapSend);
        return bitmapSend;
    }


    @Override
    public void onDestroy() {
        this.unregisterReceiver(aHihiBroadcast);
        super.onDestroy();
    }

}