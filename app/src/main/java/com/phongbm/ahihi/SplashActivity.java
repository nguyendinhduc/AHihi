package com.phongbm.ahihi;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.parse.ParseUser;
import com.phongbm.common.GlobalApplication;
import com.phongbm.loginsignup.MainFragment;

public class SplashActivity extends AppCompatActivity {
    private final int SPLASH_DISPLAY_LENGHT = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                ParseUser currentUser = ParseUser.getCurrentUser();
                if (currentUser != null) {
                    GlobalApplication.startWaitingAHihi = true;
                    GlobalApplication.startActivityMessage = false;
                    GlobalApplication.checkLoginThisId = true;
                    currentUser.put("isOnline", true);
                    currentUser.saveInBackground();
                    Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                    SplashActivity.this.startActivity(intent);
                } else {
                    Intent intent = new Intent(SplashActivity.this, MainFragment.class);
                    SplashActivity.this.startActivity(intent);
                }
                SplashActivity.this.finish();
            }
        }, SPLASH_DISPLAY_LENGHT);
    }

}