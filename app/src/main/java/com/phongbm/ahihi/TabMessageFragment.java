package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.message.MessagesLogDBManager;
import com.phongbm.message.MessagesLogItem;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabMessageFragment extends Fragment {
    private static final String TAG = "TabOneFragment";
    private View view;
    private ListView listViewMessage;
    private ArrayList<MessagesLogItem> messagesLogItems;
    private MessagesLogDBManager messagesLogDBManager;
    private MessageLogAdapter messageLogAdapter;
    private BroadcastMessageLog broadcastMessageLog;
    private ProgressDialog progressDialog;

    public TabMessageFragment(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_message, null);
        initializeComponent();
        messagesLogDBManager = new MessagesLogDBManager(context);

        messagesLogItems = messagesLogDBManager.getData();
        messageLogAdapter = new MessageLogAdapter();
        listViewMessage.setAdapter(messageLogAdapter);
//        GlobalApplication.checkLoginThisId = true;
        TabMessageFragment.this.registerBroadcastMessageLog(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()...TabMessageFragment");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()...");

        return view;
    }

    private void initializeComponent() {
        listViewMessage = (ListView) view.findViewById(R.id.listViewMessage);
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
                // viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
                viewHolder.txtFullName = (TextView) convertView.findViewById(R.id.txtFullName);
                viewHolder.txtConent = (TextView) convertView.findViewById(R.id.txtContent);
                viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.txtFullName.setText(messagesLogItems.get(position).getFullName());
            viewHolder.txtConent.setText(messagesLogItems.get(position).getMessage());
            Picasso.with(parent.getContext())
                    .load(messagesLogItems.get(position).getLinkAvatar())
                    .resize(CommonMethod.getInstance().convertSizeIcon(GlobalApplication.DENSITY_DPI, 48),
                            CommonMethod.getInstance().convertSizeIcon(GlobalApplication.DENSITY_DPI, 48))
                    .placeholder(R.drawable.loading_picture)
                    .error(R.drawable.ic_launcher_ahihi)
                    .centerCrop()
                    .into(viewHolder.imgAvatar);

            return convertView;
        }
    }

    private class ViewHolder {
        // private CircleImageView imgAvatar;
        private TextView txtFullName, txtConent;
        private CircleImageView imgAvatar;
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
            switch (intent.getAction()) {
                case CommonValue.UPDATE_MESSAGE_LOG:
                    Log.i(TAG, "BroadcastTabMessage_ onReceive");
                    String id = intent.getStringExtra(CommonValue.MESSAGE_LOG_ID);
                    String fullName = intent.getStringExtra(CommonValue.MESSAGE_LOG_FULL_NAME);
                    String content = intent.getStringExtra(CommonValue.MESSAGE_LOG_CONTENT);
                    String date = intent.getStringExtra(CommonValue.MESSAGE_LOG_DATE);
                    String linkAvatar = intent.getStringExtra(CommonValue.MESSAGE_LOG_LINK_AVATAR);
                    /*int position = hasIdMessagesLogItem(id);
                    if (position > -1) {
                        messagesLogItems.get(position).setMessage("You: " + content);
                    } else {
                        messagesLogItems.add(0, new MessagesLogItem(id, fullName, content, date, false));
                    }*/
                    int indexSame = messagesLogItems.indexOf(new MessagesLogItem(id, fullName, content, date, false, linkAvatar));
//                    messagesLogItems.add(0, new MessagesLogItem(id, fullName, content, date, false, linkAvatar));
                    if (indexSame < 0) {
                        messagesLogItems.add(0, new MessagesLogItem(id, fullName, content, date, false, linkAvatar));
                    } else {
                        messagesLogItems.get(indexSame).setDate(date);
                        messagesLogItems.get(indexSame).setFullName(fullName);
                        messagesLogItems.get(indexSame).setMessage(content);
                        messagesLogItems.get(indexSame).setLinkAvatar(linkAvatar);
                        messagesLogItems.get(indexSame).setIsRead(false);
                    }
                    messageLogAdapter.notifyDataSetChanged();
                    break;
                case CommonValue.MESSAGE_LOG_STOP:
                    progressDialog = new ProgressDialog(context);
                    progressDialog.setTitle("Running AHihi");
                    progressDialog.setMessage("Your account first loading. Please waitting after 10 second...");
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



    /*private int hasIdMessagesLogItem(String id) {
        for (int i = 0; i < messagesLogItems.size(); i++) {
            if (id.equals(messagesLogItems.get(i).getId())) {
                return i;
            }
        }
        return -1;
    }*/

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy_ tab message");
        messagesLogDBManager.closeDatabase();
        this.getActivity().unregisterReceiver(broadcastMessageLog);
        broadcastMessageLog = null;
        super.onDestroy();
    }

}