package com.phongbm.call;

import android.app.KeyguardManager;
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
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import com.phongbm.music.RingtoneManager;

import de.hdodenhof.circleimageview.CircleImageView;

public class InComingCallActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int UPDATE_TIME_CALL = 1000;
    private static final int NOTIFICATION_CALLING = 0;
    private static final int NOTIFICATION_MISSED_CALL = 1;

    private ImageView btnAnswer, btnEndCall;
    private TextView txtTime, txtFullName, txtPhoneNumber;
    private CircleImageView imgAvatar;
    private CallingRippleView callingRipple;
    private RingtoneManager ringtoneManager;
    private BroadcastInComingCall broadcastInComingCall;
    private int timeCall = 0;
    private String state, id, fullName, phoneNumber, time, date = null;
    private boolean isCalling = false;
    private Thread threadTimeCall;
    private CommonMethod commonMethod;
    private CallLogsDBManager callLogsDBManager;
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
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.setContentView(R.layout.activity_incoming_call);
        this.initializeComponent();
        this.registerBroadcastInComingCall();
        this.setVolumeControlStream(AudioManager.STREAM_RING);
        ringtoneManager = new RingtoneManager(this);
        ringtoneManager.playRingtone();
        callLogsDBManager = new CallLogsDBManager(this);
        commonMethod = CommonMethod.getInstance();
        commonMethod.pushNotification(this, MainActivity.class, "Calling...",
                NOTIFICATION_CALLING, R.drawable.ic_notification_calling, true);
    }

    private void initializeComponent() {
        btnAnswer = (ImageView) findViewById(R.id.btnAnswer);
        btnAnswer.setOnClickListener(this);
        btnEndCall = (ImageView) findViewById(R.id.btnEndCall);
        btnEndCall.setOnClickListener(this);
        txtTime = (TextView) findViewById(R.id.txtTime);
        txtFullName = (TextView) findViewById(R.id.txtFullName);
        txtPhoneNumber = (TextView) findViewById(R.id.txtPhoneNumber);
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        callingRipple = (CallingRippleView) findViewById(R.id.callingRipple);

        id = this.getIntent().getStringExtra(CommonValue.OUTGOING_CALL_ID);
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
            case R.id.btnAnswer:
                isCalling = true;
                btnAnswer.setEnabled(false);
                ringtoneManager.stopRingtone();
                this.setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
                Intent intentAnswer = new Intent(CommonValue.ACTION_ANSWER);
                this.sendBroadcast(intentAnswer);
                break;
            case R.id.btnEndCall:
                isCalling = false;
                date = commonMethod.getCurrentDateTime();
                btnAnswer.setEnabled(false);
                ringtoneManager.stopRingtone();
                this.setVolumeControlStream(AudioManager.USE_DEFAULT_STREAM_TYPE);
                Intent intentHangup = new Intent(CommonValue.ACTION_END_CALL);
                this.sendBroadcast(intentHangup);
                break;
        }
    }

    private void registerBroadcastInComingCall() {
        if (broadcastInComingCall == null) {
            broadcastInComingCall = new BroadcastInComingCall();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonValue.STATE_ANSWER);
            intentFilter.addAction(CommonValue.STATE_END_CALL);
            this.registerReceiver(broadcastInComingCall, intentFilter);
        }
    }

    private class BroadcastInComingCall extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CommonValue.STATE_ANSWER:
                    threadTimeCall = new Thread(runnableTimeCall);
                    threadTimeCall.start();
                    InComingCallActivity.this.setVolumeControlStream(
                            AudioManager.STREAM_VOICE_CALL);
                    break;
                case CommonValue.STATE_END_CALL:
                    if (timeCall != 0) {
                        isCalling = false;
                        txtTime.setText("End Call: " + time);
                        state = "inComingCall";
                    } else {
                        ringtoneManager.stopRingtone();
                        commonMethod.pushNotification(InComingCallActivity.this, CallLogActivity.class,
                                "Missed Call", NOTIFICATION_MISSED_CALL,
                                R.drawable.ic_notification_missed_call, false);
                        txtTime.setText("Missed Call");
                        state = "missedCall";
                    }
                    if (date == null) {
                        date = commonMethod.getCurrentDateTime();
                    }
                    txtTime.setBackgroundColor(InComingCallActivity.this.getResources()
                            .getColor(R.color.red_500));
                    callingRipple.setVisibility(RelativeLayout.GONE);
                    InComingCallActivity.this.setVolumeControlStream(
                            AudioManager.USE_DEFAULT_STREAM_TYPE);

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", id);
                    contentValues.put("fullName", fullName);
                    contentValues.put("phoneNumber", phoneNumber);
                    contentValues.put("date", date);
                    contentValues.put("state", state);
                    callLogsDBManager.insertData(contentValues);

                    (new Handler()).postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            InComingCallActivity.this.finish();
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
        this.unregisterReceiver(broadcastInComingCall);
        callLogsDBManager.closeDatabase();
        ((NotificationManager) InComingCallActivity.this.
                getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NOTIFICATION_CALLING);
        super.onDestroy();
    }

}