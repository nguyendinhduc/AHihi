package com.phongbm.common;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
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
import com.parse.ParseUser;
import com.parse.ProgressCallback;
import com.parse.SaveCallback;
import com.phongbm.ahihi.AllFriendItem;
import com.phongbm.ahihi.R;
import com.phongbm.message.MessageActivity;
import com.phongbm.message.MessagesLogDBManager;
import com.phongbm.music.Sound;
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
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
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
    private String outGoingId = null, senderName = null;

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
    private Sound sound;

    @Override
    public void onCreate() {
        super.onCreate();
        if (context == null) {
            context = this;
        }
        this.registerBroadcast();
        commonMethod = CommonMethod.getInstance();
        if (messagesLogDBManager == null) {
            messagesLogDBManager = new MessagesLogDBManager(this);
        }
        sound = new Sound(this, 10);
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

        ParseUser currentUser = ParseUser.getCurrentUser();
        outGoingId = currentUser != null ? currentUser.getObjectId() : null;
        senderName = currentUser != null ? currentUser.getString("fullName") : null;

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
        public void onIncomingMessage(final MessageClient messageClient, final Message message) {
            sound.playMessageTone();

            if (message.getHeaders().get("MAP") != null) {
                String body = message.getTextBody();
                if (body.equals("MAP_OK")) {
                    Log.i(TAG, "MAP_OK");
                    if (!isGPSOn()) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                    double latitude = Double.parseDouble(message.getHeaders().get("LATITUDE"));
                    double longitude = Double.parseDouble(message.getHeaders().get("LONGITUDE"));
                    Uri gmmIntentUri = Uri.parse("google.navigation:q=" + latitude + "," + longitude);
                    Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                    mapIntent.setFlags(mapIntent.getFlags() | Intent.FLAG_ACTIVITY_NO_ANIMATION
                            | Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_TASK);
                    mapIntent.setPackage("com.google.android.apps.maps");
                    if (mapIntent.resolveActivity(AHihiService.this.getPackageManager()) != null) {
                        AHihiService.this.startActivity(mapIntent);
                    }
                    return;
                }
                final AlertDialog alertDialog = new AlertDialog.Builder(AHihiService.this).create();
                alertDialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                alertDialog.setTitle("Confirm");
                alertDialog.setMessage(body + " want to take your current address?");
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "NO",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, "Get location... OK");
                                LocationManager locationManager = (LocationManager)
                                        context.getSystemService(Context.LOCATION_SERVICE);
                                if (!isGPSOn()) {
                                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                    context.startActivity(intent);
                                }
                                Criteria criteria = new Criteria();
                                criteria.setPowerRequirement(Criteria.ACCURACY_LOW);
                                String provider = locationManager.getBestProvider(criteria, true);

                                if (ContextCompat.checkSelfPermission(AHihiService.this,
                                        Manifest.permission.ACCESS_FINE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED
                                        && ContextCompat.checkSelfPermission(AHihiService.this,
                                        Manifest.permission.ACCESS_COARSE_LOCATION)
                                        != PackageManager.PERMISSION_GRANTED) {
                                    return;
                                }
                                Location location = locationManager.getLastKnownLocation(provider);
                                if (location != null) {
                                    double latitude = location.getLatitude();
                                    double longitude = location.getLongitude();
                                    Log.i(TAG, "Location: " + latitude + ", " + longitude);
                                    WritableMessage writableMessage = new
                                            WritableMessage(message.getSenderId(), "MAP_OK");
                                    writableMessage.addHeader("MAP", "GET_LOCATION");
                                    writableMessage.addHeader("LATITUDE", latitude + "");
                                    writableMessage.addHeader("LONGITUDE", longitude + "");
                                    messageClient.send(writableMessage);
                                }
                            }
                        });
                alertDialog.show();
                return;
            }
            ActivityManager activityManager = (ActivityManager) AHihiService.this
                    .getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningAppProcessInfo> tasks = activityManager.getRunningAppProcesses();

            String content = message.getTextBody();
            String key = null;
            if (content.contains(CommonValue.AHIHI_KEY)) {
                key = content.substring(0, CommonValue.KEY_LENGTH + 1);
                content = content.substring(CommonValue.KEY_LENGTH + 1);
            }

            if (GlobalApplication.startActivityMessage) {
                Intent intentSent = new Intent();
                intentSent.setAction(CommonValue.STATE_MESSAGE_INCOMING);
                intentSent.putExtra(CommonValue.AHIHI_KEY_DATE, message.getHeaders().get("date"));
                intentSent.putExtra(CommonValue.AHIHI_KEY, key);
                intentSent.putExtra(CommonValue.MESSAGE_CONTENT, content);
                AHihiService.this.sendBroadcast(intentSent);
            }
            String id = message.getSenderId();
            Map<String, String> header = message.getHeaders();
            String fullName = header.get("senderName");
            String name;
            if (fullName.contains(" ")) {
                name = fullName.substring(0, fullName.indexOf(" ") + 1);
            } else {
                name = fullName;
            }
            String date = header.get("date");
            if (key != null) {
                switch (key) {
                    case CommonValue.AHIHI_KEY_EMOTICON:
                        content = name + " sent a sticker";
                        break;
                    case CommonValue.AHIHI_KEY_FILE:
                        content = name + " sent a file";
                        break;
                    case CommonValue.AHIHI_KEY_PICTURE:
                        content = name + " sent a picture";
                        break;
                }
            }
            int isRead;
            if (tasks.get(0).processName.equals(CommonValue.PACKAGE_NAME_MAIN)) {
                isRead = 1;
            } else {
                isRead = 0;
            }

            if (tasks.get(0).processName.equals(CommonValue.PACKAGE_NAME_MAIN)) {
            } else {
                if (!open) {
                    open = true;
                    AHihiService.this.openChatHead(message.getSenderId(),
                            message.getHeaders().get("senderName"), content, date);
                }
            }

            AHihiService.this.updateMessagesLogDBManager(id, fullName, content, date, isRead);
            if (!GlobalApplication.startWaitingAHihi) {
                GlobalApplication.startWaitingAHihi = true;
                GlobalApplication.checkLoginThisId = false;
                Intent intent = new Intent();
                intent.setAction(CommonValue.MESSAGE_LOG_STOP);
                AHihiService.this.sendBroadcast(intent);
            }
            if (GlobalApplication.checkLoginThisId) {
                Intent intentIncoming = new Intent();
                intentIncoming.setAction(CommonValue.UPDATE_MESSAGE_LOG);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_ID, id);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_FULL_NAME, fullName);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_CONTENT, content);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_DATE, date);
                intentIncoming.putExtra(CommonValue.MESSAGE_LOG_IS_READ, isRead);
                AHihiService.this.sendBroadcast(intentIncoming);
            }
        }

        @Override
        public void onMessageSent(MessageClient messageClient, final Message message, String s) {
            if (message.getHeaders().get("MAP") != null) {
                Log.i(TAG, "onMessageSent... MAP");
            } else {
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
                }
                String id = message.getRecipientIds().get(0);
                Map<String, String> header = message.getHeaders();
                String fullName = header.get("fullName");
                String date = header.get("date");
                if (key != null) {
                    switch (key) {
                        case CommonValue.AHIHI_KEY_EMOTICON:
                            content = "Sent a sticker";
                            break;
                        case CommonValue.AHIHI_KEY_FILE:
                            content = "Sent a file";
                            break;
                        case CommonValue.AHIHI_KEY_PICTURE:
                            content = "Sent a picture";
                            break;
                    }
                }
                AHihiService.this.updateMessagesLogDBManager(id, fullName, "You:  " + content, date, 1);
                if (!GlobalApplication.startWaitingAHihi) {
                    GlobalApplication.startWaitingAHihi = true;
                    GlobalApplication.checkLoginThisId = false;
                    Intent intent = new Intent();
                    intent.setAction(CommonValue.MESSAGE_LOG_STOP);
                    sendBroadcast(intent);
                }
                if (GlobalApplication.checkLoginThisId) {
                    Intent intentMessage = new Intent();
                    intentMessage.setAction(CommonValue.UPDATE_MESSAGE_LOG);
                    intentMessage.putExtra(CommonValue.MESSAGE_LOG_ID, id);
                    intentMessage.putExtra(CommonValue.MESSAGE_LOG_FULL_NAME, fullName);
                    intentMessage.putExtra(CommonValue.MESSAGE_LOG_CONTENT, "You: " + content);
                    intentMessage.putExtra(CommonValue.MESSAGE_LOG_DATE, date);
                    intentMessage.putExtra(CommonValue.MESSAGE_LOG_IS_READ, 1);
                    AHihiService.this.sendBroadcast(intentMessage);
                }
            }
        }

        @Override
        public void onMessageFailed(MessageClient messageClient, Message message,
                                    MessageFailureInfo messageFailureInfo) {
        }

        @Override
        public void onMessageDelivered(MessageClient messageClient, MessageDeliveryInfo messageDeliveryInfo) {
            Intent intentDelivered = new Intent();
            intentDelivered.setAction(CommonValue.STATE_MESSAGE_DELIVERED);
            AHihiService.this.sendBroadcast(intentDelivered);
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
            // Map
            intentFilter.addAction(CommonValue.ACTION_MAP);
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
                    GlobalApplication.startWaitingAHihi = false;
                    break;

                // Message
                case CommonValue.ACTION_SEND_MESSAGE:
                    String id = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_ID);
                    String fullName = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME);
                    String content = intent.getStringExtra(CommonValue.MESSAGE_CONTENT);
                    date = intent.getStringExtra(CommonValue.AHIHI_KEY_DATE);
                    if (intent.getStringExtra(CommonValue.AHIHI_KEY) == null) {
                        AHihiService.this.sendMessage(id, content, fullName, date);
                    } else {
                        switch (intent.getStringExtra(CommonValue.AHIHI_KEY)) {
                            case CommonValue.AHIHI_KEY_EMOTICON:
                                content = CommonValue.AHIHI_KEY_EMOTICON + content;
                                AHihiService.this.sendMessage(id, content, fullName, date);
                                break;
                            case CommonValue.AHIHI_KEY_FILE:
                                AHihiService.this.sendFile(id, content, fullName, date);
                                break;
                            case CommonValue.AHIHI_KEY_PICTURE:
                                AHihiService.this.sendPicture(id, content, fullName, date);
                                break;
                        }
                    }
                    break;
                case CommonValue.ACTION_MAP:
                    Log.i(TAG, "ACTION_MAP...");
                    String userId = intent.getStringExtra(CommonValue.USER_ID);
                    String name = intent.getStringExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME);
                    WritableMessage message = new WritableMessage(userId, name);
                    message.addHeader("MAP", "GET_LOCATION");
                    messageClient.send(message);
                    break;
            }
        }
    }

    private synchronized void sendMessage(final String id, final String content,
                                          final String fullName, final String date) {
        ParseObject message = new ParseObject("Message");
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
                message.addHeader("senderName", senderName);
                messageClient.send(message);
            }
        });
    }

    private synchronized void sendFile(final String id, final String path,
                                       final String fullName, final String date) {
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
                                            message.addHeader("senderName", senderName);
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

    private synchronized void sendPicture(final String id, final String pathPicture,
                                          final String fullName, final String date) {
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
                        message.addHeader("senderName", senderName);
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
            if (bitmapSend.getWidth() > WIDTH_IMAGE_MAX
                    || bitmapSend.getHeight() > HEIGHT_IMAGE_MAX) {
                Pair<Integer, Integer> pair = CommonMethod.getInstance()
                        .getStandSizeBitmap(bitmapSend.getWidth(), bitmapSend.getHeight(),
                                WIDTH_IMAGE_MAX, HEIGHT_IMAGE_MAX);
                bitmapSend = Bitmap.createScaledBitmap(bitmapSend, pair.first, pair.second, true);
                Log.i(TAG, "IF ELSE");
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

    private void openChatHead(final String id, final String fullName, final String content, final String date) {
        layoutInflater = LayoutInflater.from(this);
        widget = (RelativeLayout) layoutInflater.inflate(R.layout.widget_chathead, null, false);
        CircleImageView imgAvatar = (CircleImageView) widget.findViewById(R.id.imgAvatar);
        int index = ((GlobalApplication) AHihiService.this.getApplication()).getAllFriendItems()
                .indexOf(new AllFriendItem(id, 1));
        if (index > -1) {
            imgAvatar.setImageBitmap(((GlobalApplication) AHihiService.this
                    .getApplication()).getAllFriendItems().get(index).getAvatar());
        } else {
            imgAvatar.setImageResource(R.drawable.ic_avatar_default);
        }
        widget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intentChat = new Intent(AHihiService.this, MessageActivity.class);
                intentChat.putExtra(CommonValue.INCOMING_CALL_ID, id);
                intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, fullName);
                intentChat.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                AHihiService.this.startActivity(intentChat);

                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        AHihiService.this.updateMessagesLogDBManager(id, fullName, content, date, 1);
                        Intent intentIncoming = new Intent();
                        intentIncoming.setAction(CommonValue.UPDATE_MESSAGE_LOG);
                        intentIncoming.putExtra(CommonValue.MESSAGE_LOG_ID, id);
                        intentIncoming.putExtra(CommonValue.MESSAGE_LOG_FULL_NAME, fullName);
                        intentIncoming.putExtra(CommonValue.MESSAGE_LOG_CONTENT, content);
                        intentIncoming.putExtra(CommonValue.MESSAGE_LOG_DATE, date);
                        intentIncoming.putExtra(CommonValue.MESSAGE_LOG_IS_READ, 1);
                        AHihiService.this.sendBroadcast(intentIncoming);
                    }
                });

                floatingViewManager.removeAllViewToWindow();
            }
        });

        floatingViewManager = new FloatingViewManager(this, this);
        floatingViewManager.setFixedTrashIconImage(R.drawable.ic_trash_fixed);
        floatingViewManager.setActionTrashIconImage(R.drawable.ic_trash_action);
        floatingViewManager.addViewToWindow(widget, FloatingViewManager.SHAPE_CIRCLE,
                (int) (16 * displayMetrics.density));
    }

    private synchronized void updateMessagesLogDBManager(String id, String fullName, String message,
                                                         String date, int isRead) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("id", id);
        contentValues.put("fullName", fullName);
        contentValues.put("message", message);
        contentValues.put("date", date);
        contentValues.put("isRead", isRead);
        if (messagesLogDBManager.conversationExist(id)) {
            messagesLogDBManager.update(contentValues);
        } else {
            messagesLogDBManager.insertData(contentValues);
        }
    }

    public boolean isGPSOn() {
        LocationManager locationManager = (LocationManager)
                context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
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