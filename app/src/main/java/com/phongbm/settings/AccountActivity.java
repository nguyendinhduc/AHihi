package com.phongbm.settings;

import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.phongbm.ahihi.R;
import com.phongbm.common.GlobalApplication;

public class AccountActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editFullName, editEmail, editPhoneNumber;
    private Button btnCancel, btnOk, btnApply;
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
        toolbar.setSubtitle("");
    }

    private void initComponent() {
        editFullName = (EditText) findViewById(R.id.editFullName);
        editFullName.setText(((GlobalApplication)getApplication()).getFullName());
        editEmail = (EditText) findViewById(R.id.editEmail);
        editEmail.setText(((GlobalApplication)getApplication()).getEmail());
        editPhoneNumber = (EditText) findViewById(R.id.editPhoneNumber);
        editPhoneNumber.setText(((GlobalApplication)getApplication()).getPhoneNumber());

        btnOk = (Button) findViewById(R.id.btnOk);
        btnOk.setOnClickListener(this);
        btnApply = (Button) findViewById(R.id.btnApply);
        btnApply.setOnClickListener(this);
        btnCancel = (Button) findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOK:
                break;
            case R.id.btnApply:
                break;
            case R.id.btnCancel:
                finish();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }
}
