package com.phongbm.settings;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.widget.EditText;

import com.phongbm.ahihi.R;
import com.phongbm.common.GlobalApplication;

public class AccountActivity extends AppCompatActivity {
    private EditText editFullName, editEmail, editPhoneNumber;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);
        initializeToolbar();
        initComponent();
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle("Account");
        toolbar.setSubtitle("Online");
    }

    private void initComponent() {
        editFullName = (EditText) findViewById(R.id.editFullName);
        editFullName.setText(((GlobalApplication)getApplication()).getFullName());
        editEmail = (EditText) findViewById(R.id.editEmail);
        editEmail.setText(((GlobalApplication)getApplication()).getEmail());
        editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editPhoneNumber.setText(((GlobalApplication)getApplication()).getPhoneNumber());
    }
}
