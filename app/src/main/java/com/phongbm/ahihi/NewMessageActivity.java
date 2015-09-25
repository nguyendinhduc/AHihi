package com.phongbm.ahihi;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.message.MessageActivity;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener,
        View.OnClickListener {
    private static final String TAG = "NewMessageActivity";

    private Toolbar toolbar;
    private ListView listViewAllFriend;
    private ArrayList<AllFriendItem> allFriendItems;
    private NewMessageAdapter newMessageAdapter;
    private EditText edtTo;
    private RelativeLayout layoutOK;
    private TextView btnOK, txtReceiver;
    private boolean isClickItem = false;
    private int positionItem = -1;
    private View view = null;
    private String id = null, fullName = null;
    private RelativeLayout layoutNoFriends;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_new_message);
        this.initializeToolbar();
        this.initializeComponent();

        allFriendItems = ((GlobalApplication) this.getApplication()).getAllFriendItems();
        newMessageAdapter = new NewMessageAdapter(this, allFriendItems);
        listViewAllFriend.setAdapter(newMessageAdapter);

        if (allFriendItems.size() == 0) {
            listViewAllFriend.setVisibility(View.GONE);
            layoutNoFriends.setVisibility(View.VISIBLE);
        }
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle(R.string.activity_new_message);
    }

    private void initializeComponent() {
        listViewAllFriend = (ListView) findViewById(R.id.listViewAllFriend);
        listViewAllFriend.setOnItemClickListener(this);
        edtTo = (EditText) findViewById(R.id.edtTo);
        edtTo.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!isClickItem) {
                    Log.i(TAG, "onTextChanged");
                    newMessageAdapter.getFilter().filter(s);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        layoutOK = (RelativeLayout) findViewById(R.id.layoutOK);
        btnOK = (TextView) findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        txtReceiver = (TextView) findViewById(R.id.txtReceiver);
        layoutNoFriends = (RelativeLayout) findViewById(R.id.layoutNoFriends);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        TextView txtFullName = (TextView) view.findViewById(R.id.txtFullName);
        CheckBox checkBoxOK = (CheckBox) view.findViewById(R.id.checkBoxOK);
        if (this.view != null) {
            TextView txtFullNameView = (TextView) this.view.findViewById(R.id.txtFullName);
            CheckBox checkBoxOKView = (CheckBox) this.view.findViewById(R.id.checkBoxOK);
            txtFullNameView.setTextColor(Color.parseColor("#666666"));
            checkBoxOKView.setChecked(false);
        }
        this.view = view;
        isClickItem = true;
        if (positionItem != position) {
            isClickItem = true;
            positionItem = position;
            edtTo.setText(null);
            edtTo.setVisibility(View.GONE);
            txtReceiver.setVisibility(View.VISIBLE);
            txtReceiver.setText(allFriendItems.get(position).getFullName());
            txtFullName.setTextColor(ContextCompat.getColor(this, R.color.colorPrimary));
            checkBoxOK.setChecked(true);
            this.id = allFriendItems.get(position).getId();
            fullName = allFriendItems.get(position).getFullName();
            layoutOK.setVisibility(View.VISIBLE);
        } else {
            isClickItem = false;
            positionItem = -1;
            edtTo.setText(null);
            edtTo.setVisibility(View.VISIBLE);
            txtReceiver.setText(null);
            txtReceiver.setVisibility(View.GONE);
            txtFullName.setTextColor(Color.parseColor("#666666"));
            checkBoxOK.setChecked(false);
            this.id = null;
            fullName = null;
            layoutOK.setVisibility(View.GONE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOK:
                Intent intentChat = new Intent(this, MessageActivity.class);
                intentChat.putExtra(CommonValue.INCOMING_CALL_ID, id);
                intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, fullName);
                this.startActivity(intentChat);
                this.finish();
                break;
        }
    }

}