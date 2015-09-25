package com.phongbm.call;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
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
    private CoordinatorLayout coordinator;
    private boolean canDelete;
    private SwipeToDismissTouchListener<ListViewAdapter> touchListener;
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
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);

        touchListener = new SwipeToDismissTouchListener<>(new ListViewAdapter(listViewCallLog),
                new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListViewAdapter view, int position) {
                        String id = callLogItems.get(position).getId();
                        callLogItems.remove(position);
                        callLogAdapter.notifyDataSetChanged();
                        callLogsDBManager.deleteData(id);
                        if (callLogItems.size() == 0) {
                            canDelete = true;
                            listViewCallLog.setVisibility(RelativeLayout.GONE);
                            layoutNoCallLogs.setVisibility(RelativeLayout.VISIBLE);
                        }
                        Snackbar snackbar = Snackbar.make(coordinator, "Deleted successfully",
                                Snackbar.LENGTH_LONG)
                                .setAction("ACTION", null);
                        View snackbarView = snackbar.getView();
                        snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
                        snackbar.show();
                    }
                });
        listViewCallLog.setOnTouchListener(touchListener);
        listViewCallLog.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listViewCallLog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    final AlertDialog alertDialog = new AlertDialog.Builder(CallLogActivity.this).create();
                    alertDialog.setTitle("Call log");
                    alertDialog.setMessage("Full name: " + callLogItems.get(position).getFullName() + "\n" +
                            "Mobile: " + callLogItems.get(position).getPhoneNumber() + "\n" +
                            "Date: " + callLogItems.get(position).getDate() + "\n" +
                            "State: " + callLogItems.get(position).getState());
                    alertDialog.setCanceledOnTouchOutside(true);
                    alertDialog.setCancelable(true);
                    alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "YES",
                            new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    alertDialog.dismiss();
                                }
                            });
                    alertDialog.show();
                }
            }
        });
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