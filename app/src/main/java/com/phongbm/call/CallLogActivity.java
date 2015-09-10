package com.phongbm.call;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;

import java.util.ArrayList;

public class CallLogActivity extends AppCompatActivity {
    private static final int REQUEST_CALL_LOGS = 0;

    private CallLogsDBManager callLogsDBManager;
    private ListView listViewCallLog;
    private CallLogAdapter callLogAdapter;
    private ArrayList<CallLogItem> callLogItems;
    private RelativeLayout layoutNoCallLogs;
    private boolean canDelete;
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonValue.WHAT_CALL_BACK:
                    Intent intentOutGoingCall = new Intent(CallLogActivity.this,
                            OutgoingCallActivity.class);
                    intentOutGoingCall.putExtra(CommonValue.INCOMING_CALL_ID, (String) msg.obj);
                    CallLogActivity.this.startActivityForResult(intentOutGoingCall,
                            REQUEST_CALL_LOGS);
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_call_log);
        this.initializeToolbar();
        this.initializeComponent();
        callLogsDBManager = new CallLogsDBManager(this);
        callLogItems = callLogsDBManager.getData();
        if (callLogItems.size() == 0) {
            canDelete = false;
            listViewCallLog.setVisibility(RelativeLayout.GONE);
            layoutNoCallLogs.setVisibility(RelativeLayout.VISIBLE);
        } else {
            canDelete = true;
            callLogAdapter = new CallLogAdapter(this, callLogItems, handler);
            listViewCallLog.setAdapter(callLogAdapter);
        }
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeComponent() {
        listViewCallLog = (ListView) findViewById(R.id.listViewCallLog);
        layoutNoCallLogs = (RelativeLayout) findViewById(R.id.layoutNoCallLogs);
    }

    @Override
    protected void onDestroy() {
        callLogsDBManager.closeDatabase();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_call_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                CallLogActivity.this.finish();
                return true;
            case R.id.action_delete:
                if (canDelete) {
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Delete");
                    alertDialog.setMessage("All call logs will be deleted. Delete?");
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
                                    canDelete = false;
                                    callLogsDBManager.deleteAllData();
                                    callLogItems.clear();
                                    callLogAdapter.notifyDataSetChanged();
                                    alertDialog.dismiss();
                                    listViewCallLog.setVisibility(RelativeLayout.GONE);
                                    layoutNoCallLogs.setVisibility(RelativeLayout.VISIBLE);
                                }
                            });
                    alertDialog.show();
                } else {
                    final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                    alertDialog.setTitle("Delete");
                    alertDialog.setMessage("No call logs");
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Toast.makeText(this, "OK", Toast.LENGTH_SHORT).show();
        if (requestCode == REQUEST_CALL_LOGS) {
            if (resultCode == Activity.RESULT_OK) {
                if (!canDelete) {
                    canDelete = true;
                }
                callLogAdapter.setCallLogItems(callLogsDBManager.getData());
                callLogAdapter.notifyDataSetChanged();
            }
        }
    }

}