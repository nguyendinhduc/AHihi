package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.message.MessageActivity;
import com.phongbm.message.MessagesLogDBManager;
import com.phongbm.message.MessagesLogItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabMessageFragment extends Fragment implements AdapterView.OnItemClickListener,
        AdapterView.OnItemLongClickListener {
    private static final String TAG = "TabMessageFragment";

    private View view;
    private ListView listViewMessage;
    private ArrayList<MessagesLogItem> messagesLogItems;
    private MessagesLogDBManager messagesLogDBManager;
    private MessageLogAdapter messageLogAdapter;
    private BroadcastMessageLog broadcastMessageLog;
    private ProgressDialog progressDialog;
    private RelativeLayout layoutNoConversations;
    private int positionLongItemClick = -1;

    public TabMessageFragment(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_message, null);
        this.initializeComponent();
        messagesLogDBManager = new MessagesLogDBManager(context);

        messagesLogItems = messagesLogDBManager.getData();
        messageLogAdapter = new MessageLogAdapter();
        listViewMessage.setAdapter(messageLogAdapter);
        this.registerBroadcastMessageLog(context);

        if (messageLogAdapter.getCount() == 0) {
            listViewMessage.setVisibility(View.GONE);
            layoutNoConversations.setVisibility(View.VISIBLE);
        } else {
            listViewMessage.setVisibility(View.VISIBLE);
            layoutNoConversations.setVisibility(View.GONE);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerForContextMenu(listViewMessage);
        Log.i(TAG, "onCreate()...TabMessageFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()...");
        return view;
    }

    private void initializeComponent() {
        listViewMessage = (ListView) view.findViewById(R.id.listViewMessage);
        listViewMessage.setOnItemClickListener(this);
        listViewMessage.setOnItemLongClickListener(this);
        layoutNoConversations = (RelativeLayout) view.findViewById(R.id.layoutNoConversations);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
        final MessagesLogItem messagesLogItem = messagesLogItems.get(position);
        if (messagesLogItems.get(position).isRead() == 0) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    messagesLogItems.get(position).setIsRead(1);
                    messageLogAdapter.notifyDataSetChanged();

                    ContentValues contentValues = new ContentValues();
                    contentValues.put("id", messagesLogItem.getId());
                    contentValues.put("fullName", messagesLogItem.getFullName());
                    contentValues.put("message", messagesLogItem.getMessage());
                    contentValues.put("date", messagesLogItem.getDate());
                    contentValues.put("isRead", true);
                    messagesLogDBManager.update(contentValues);

                    messagesLogDBManager.getData();
                }
            });
        }
        Intent intentChat = new Intent(TabMessageFragment.this.getActivity(), MessageActivity.class);
        intentChat.putExtra(CommonValue.INCOMING_CALL_ID, messagesLogItem.getId());
        intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, messagesLogItem.getFullName());
        TabMessageFragment.this.getActivity().startActivity(intentChat);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        this.getActivity().openContextMenu(parent);
        positionLongItemClick = position;
        return true;
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);
        if (view.getId() == R.id.listViewMessage) {
            menu.setHeaderTitle("Conversation");
            MenuInflater menuInflater = this.getActivity().getMenuInflater();
            menuInflater.inflate(R.menu.menu_more_options, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                Log.i(TAG, "action_delete... " + positionLongItemClick);
                messagesLogDBManager.deleteData(messagesLogItems.get(positionLongItemClick).getId());
                messagesLogItems.remove(positionLongItemClick);
                messageLogAdapter.notifyDataSetChanged();
                positionLongItemClick = -1;
                CoordinatorLayout coordinator = (CoordinatorLayout) TabMessageFragment.this
                        .getActivity().findViewById(R.id.coordinator);
                Snackbar snackbar = Snackbar.make(coordinator, "Delete successfully", Snackbar.LENGTH_LONG)
                        .setAction("ACTION", null);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
                snackbar.show();
                if (messagesLogItems.size() == 0) {
                    listViewMessage.setVisibility(View.GONE);
                    layoutNoConversations.setVisibility(View.VISIBLE);
                }
                break;
            default:
                super.onContextItemSelected(item);
        }
        return true;
    }

    /**
     * MessageLogAdapter
     */
    private class MessageLogAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return messagesLogItems.size();
        }

        @Override
        public MessagesLogItem getItem(int position) {
            return messagesLogItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(TabMessageFragment.this.getContext())
                        .inflate(R.layout.item_nearest_message, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
                viewHolder.txtFullName = (TextView) convertView.findViewById(R.id.txtFullName);
                viewHolder.txtContent = (TextView) convertView.findViewById(R.id.txtContent);
                viewHolder.txtDate = (TextView) convertView.findViewById(R.id.txtDate);
                viewHolder.imgOK = (ImageView) convertView.findViewById(R.id.imgOK);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String id = messagesLogItems.get(position).getId();
            int index = ((GlobalApplication) getActivity().getApplication()).getAllFriendItems()
                    .indexOf(new AllFriendItem(id, 1));
            if (index > -1) {
                viewHolder.imgAvatar.setImageBitmap(((GlobalApplication) getActivity()
                        .getApplication()).getAllFriendItems().get(index).getAvatar());
            } else {
                viewHolder.imgAvatar.setImageResource(R.drawable.ic_avatar_default);
            }
            viewHolder.txtFullName.setText(messagesLogItems.get(position).getFullName());
            viewHolder.txtContent.setText(messagesLogItems.get(position).getMessage());

            String date = messagesLogItems.get(position).getDate();
            String day = date.substring(0, 10);
            String time = date.substring(11);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd", Locale.US);
            String currentDay = simpleDateFormat.format(Calendar.getInstance().getTime());
            if (day.compareTo(currentDay) < 0) {
                viewHolder.txtDate.setText(day);
            } else {
                viewHolder.txtDate.setText(time);
            }
            if (messagesLogItems.get(position).isRead() == 1) {
                viewHolder.imgOK.setVisibility(View.VISIBLE);
                viewHolder.txtFullName.setTypeface(null, Typeface.NORMAL);
                viewHolder.txtContent.setTypeface(null, Typeface.NORMAL);
            } else {
                Log.i(TAG, "Not read");
                viewHolder.imgOK.setVisibility(View.GONE);
                viewHolder.txtFullName.setTypeface(null, Typeface.BOLD);
                viewHolder.txtContent.setTypeface(null, Typeface.BOLD);
            }
            return convertView;
        }
    }

    private class ViewHolder {
        CircleImageView imgAvatar;
        TextView txtFullName, txtContent, txtDate;
        ImageView imgOK;
    }

    private void registerBroadcastMessageLog(Context context) {
        if (broadcastMessageLog == null) {
            broadcastMessageLog = new BroadcastMessageLog();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonValue.UPDATE_MESSAGE_LOG);
            intentFilter.addAction(CommonValue.MESSAGE_LOG_STOP);
            context.registerReceiver(broadcastMessageLog, intentFilter);
        }
    }

    private class BroadcastMessageLog extends BroadcastReceiver {
        @Override
        public synchronized void onReceive(Context context, Intent intent) {
            if (listViewMessage.getVisibility() == View.GONE) {
                listViewMessage.setVisibility(View.VISIBLE);
                layoutNoConversations.setVisibility(View.GONE);
            }
            switch (intent.getAction()) {
                case CommonValue.UPDATE_MESSAGE_LOG:
                    Log.i(TAG, "BroadcastTabMessage... onReceive...");
                    String id = intent.getStringExtra(CommonValue.MESSAGE_LOG_ID);
                    String fullName = intent.getStringExtra(CommonValue.MESSAGE_LOG_FULL_NAME);
                    String content = intent.getStringExtra(CommonValue.MESSAGE_LOG_CONTENT);
                    String date = intent.getStringExtra(CommonValue.MESSAGE_LOG_DATE);
                    int isRead = intent.getIntExtra(CommonValue.MESSAGE_LOG_IS_READ, -1);

                    int indexSame = messagesLogItems.indexOf(new MessagesLogItem(id, null,
                            null, null, -1));
                    if (indexSame >= 0) {
                        messagesLogItems.remove(indexSame);
                    }
                    messagesLogItems.add(0, new MessagesLogItem(id, fullName, content, date, isRead));
                    messageLogAdapter.notifyDataSetChanged();
                    break;
                case CommonValue.MESSAGE_LOG_STOP:
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Welcome");
                    progressDialog.setMessage("Your account first loading. Please waiting after 10 second...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            messagesLogItems = messagesLogDBManager.getData();
                            messageLogAdapter.notifyDataSetChanged();
                            GlobalApplication.checkLoginThisId = true;
                            progressDialog.dismiss();
                        }
                    }, 10000);
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy... tab message...");
        messagesLogDBManager.closeDatabase();
        this.getActivity().unregisterReceiver(broadcastMessageLog);
        this.unregisterForContextMenu(listViewMessage);
        broadcastMessageLog = null;
        super.onDestroy();
    }

}