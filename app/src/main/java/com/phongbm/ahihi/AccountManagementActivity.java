package com.phongbm.ahihi;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;

public class AccountManagementActivity extends Activity implements View.OnClickListener {
    private RelativeLayout menuChangePassword, layoutChangePassword, menuDeleteAccount,
            menuSignout;
    private ImageView imgChangePassword;
    private boolean isOpenLayoutChangePassword = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_account_management);
        this.initializeComponent();
    }

    private void initializeComponent() {
        menuChangePassword = (RelativeLayout) findViewById(R.id.menuChangePassword);
        menuChangePassword.setOnClickListener(this);
        layoutChangePassword = (RelativeLayout) findViewById(R.id.layoutChangePassword);
        imgChangePassword = (ImageView) findViewById(R.id.imgChangePassword);
        menuDeleteAccount = (RelativeLayout) findViewById(R.id.menuDeleteAccount);
        menuDeleteAccount.setOnClickListener(this);
        menuSignout = (RelativeLayout) findViewById(R.id.menuSignout);
        menuSignout.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuChangePassword:
                if (!isOpenLayoutChangePassword) {
                    imgChangePassword.setImageResource(R.drawable.ic_minus);
                    layoutChangePassword.setVisibility(ScrollView.VISIBLE);
                    isOpenLayoutChangePassword = true;
                } else {
                    imgChangePassword.setImageResource(R.drawable.ic_plus);
                    layoutChangePassword.setVisibility(ScrollView.GONE);
                    isOpenLayoutChangePassword = false;
                }
                break;
        }
    }

}