package com.phongbm.call;

import android.app.Activity;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.ahihi.MainActivity;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.libs.CallingRippleView;

import de.hdodenhof.circleimageview.CircleImageView;

public class OutgoingCallActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int UPDATE_TIME_CALL = 1000;
    private static final int NOTIFICATION_CALLING = 0;

    private ImageView btnEndCall, btnRingtone, btnMicrophone;
    private boolean isSpeaker = false;
    private TextView txtTime, txtFullName, txtPhoneNumber;
    private CircleImageView imgAvatar;
    private CallingRippleView callingRipple;
    private BroadcastOutgoingCall broadcastOutgoingCall;
    private int timeCall = 0;
    private String id, time, fullName, phoneNumber, date = null;
    private boolean isCalling = false, isPressBtnEndCall = false;
    private Thread threadTimeCall;
    private CommonMethod commonMethod;
    private CallLogsDBManager callLogsDBManager;
    private AudioManager audioManager = null;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case UPDATE_TIME_CALL:
                    txtTime.setText("Time call: " + time);
                    break;
            }
            return;
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_outgoing_call);
        this.initializeComponent();
        this.registerBroadcastOutgoingCall();
        Intent intent = new Intent(CommonValue.ACTION_OUTGOING_CALL);
        intent.putExtra(CommonValue.INCOMING_CALL_ID, id);
        this.sendBroadcast(intent);
        callLogsDBManager = new CallLogsDBManager(this);
        commonMethod = CommonMethod.getInstance();
        commonMethod.pushNotification(this, MainActivity.class, "Calling...",
                NOTIFICATION_CALLING, R.drawable.ic_notification_calling, true);
    }

    private void initializeComponent() {
        btnEndCall = (ImageView) findViewById(R.id.btnEndCall);
        btnEndCall.setOnClickListener(this);
        btnRingtone = (ImageView) findViewById(R.id.btnRingtone);
        btnRingtone.setOnClickListener(this);
        btnMicrophone = (ImageView)findViewById(R.id.btnMicrophone);
        btnMicrophone.setOnClickListener(this);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtFullName = (TextView) findViewById(R.id.txtFullName);
        txtPhoneNumber = (TextView) findViewById(R.id.txtPhoneNumber);
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        callingRipple = (CallingRippleView) findViewById(R.id.callingRipple);

        audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);

        Intent intent = this.getIntent();
        id = intent.getStringExtra(CommonValue.INCOMING_CALL_ID);
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.whereEqualTo("objectId", id);
        parseQuery.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (parseUser == null) {
                    return;
                }
                fullName = (String) parseUser.get("fullName");
                txtFullName.setText(fullName);
                phoneNumber = parseUser.getUsername();
                txtPhoneNumber.setText("Mobile " + phoneNumber);
                ParseFile parseFile = (ParseFile) parseUser.get("avatar");
                if (parseFile == null) {
                    return;
                }
                parseFile.getDataInBackground(new GetDataCallback() {
                    @Override
                    public void done(byte[] bytes, ParseException e) {
                        if (e != null) {
                            return;
                        }
                        Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgAvatar.setImageBitmap(avatar);
                    }
                });
            }
        });
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnEndCall:
                isCalling = false;
                date = commonMethod.getCurrentDateTime();
                isPressBtnEndCall = true;
                btnEndCall.setEnabled(false);
                Intent intentEndCall = new Intent(CommonValue.ACTION_END_CALL);
                this.sendBroadcast(intentEndCall);
                break;
            case R.id.btnRingtone:
                AudioManager audioManager = (AudioManager) this
                        .getSystemService(Context.AUDIO_SERVICE);
                audioManager.adjustStreamVolume(AudioManager.STREAM_RING,
                        AudioManager.ADJUST_SAME, AudioManager.FLAG_SHOW_UI);
                break;
            case R.id.btnMicrophone:
                isSpeaker = !isSpeaker;
                if ( isSpeaker ) {
                    btnMicrophone.setImageResource(R.drawable.ic_message_microphone_on);
                    OutgoingCallActivity.this.audioManager.setSpeakerphoneOn(true);
                }
                else {
                    btnMicrophone.setImageResource(R.drawable.ic_message_microphone_off);
                    OutgoingCallActivity.this.audioManager.setSpeakerphoneOn(false);
                }
                break;
        }
    }

    private void registerBroadcastOutgoingCall() {
        if (broadcastOutgoingCall == null) {
            broadcastOutgoingCall = new BroadcastOutgoingCall();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonValue.STATE_END_CALL);
            intentFilter.addAction(CommonValue.STATE_PICK_UP);
            this.registerReceiver(broadcastOutgoingCall, intentFilter);
        }
    }

    private class BroadcastOutgoingCall extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CommonValue.STATE_PICK_UP:
                    isCalling = true;
                    threadTimeCall = new Thread(runnableTimeCall);
                    threadTimeCall.start();
                    OutgoingCallActivity.this.setVolumeControlStream(
                            AudioManager.STREAM_VOICE_CALL);
                    break;
                case CommonValue.STATE_END_CALL:
                    if (timeCall != 0) {
                        isCalling = false;
                        txtTime.setText("End Call: " + time);
                    } else {
                        if (isPressBtnEndCall) {
                            txtTime.setText("Call Ended");
                        } else {
                            txtTime.setText("Missed Call");
                        }
                    }
                    if (date == null) {
                        date = commonMethod.getCurrentDateTime();
                    }
                    btnEndCall.setEnabled(false);
                    txtTime.setBackgroundColor(ContextCompat.getColor(OutgoingCallActivity.this, R.color.red_500));
                    callingRipple.setVisibility(RelativeLayout.GONE);
                    OutgoingCallActivity.this.setVolumeControlStream(
                            AudioManager.USE_DEFAULT_STREAM_TYPE);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", id);
                    contentValues.put("fullName", fullName);
                    contentValues.put("phoneNumber", phoneNumber);
                    contentValues.put("date", date);
                    contentValues.put("state", "outGoingCall");
                    callLogsDBManager.insertData(contentValues);

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            OutgoingCallActivity.this.finish();
                        }
                    }, 3000);
                    break;
            }
        }
    }

    private Runnable runnableTimeCall = new Runnable() {
        @Override
        public void run() {
            while (isCalling) {
                timeCall += 1000;
                time = commonMethod.convertTimeToString(timeCall);
                handler.sendEmptyMessage(UPDATE_TIME_CALL);
                SystemClock.sleep(1000);
            }
        }
    };

    @Override
    protected void onDestroy() {
        this.unregisterReceiver(broadcastOutgoingCall);
        callLogsDBManager.closeDatabase();
        ((NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE))
                .cancel(NOTIFICATION_CALLING);

        if ( audioManager != null ) {
            audioManager.setMode(AudioManager.MODE_NORMAL);
            audioManager.setSpeakerphoneOn(false);
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        this.setResult(Activity.RESULT_OK);
        super.finish();
    }

}