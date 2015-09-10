package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.message.MessageAdapter;
import com.phongbm.message.MessageItem;
import com.phongbm.message.MessagesLogDBManager;
import com.phongbm.message.MessagesLogItem;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabMessageFragment extends Fragment {
    private static final String TAG = "TabOneFragment";
    private View view;
    private ListView listViewMessage;
    private ArrayList<MessagesLogItem> messagesLogItems;
    private NearestMessageAdapter nearestMessageAdapter;
    private MessagesLogDBManager messagesLogDBManager;

    private BroadcastTabMessage broadcastTabMessage;


    public TabMessageFragment(Context context, ViewGroup viewGroup) {
        super();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_message, viewGroup, false);
        messagesLogDBManager = new MessagesLogDBManager(context);
        initializeComponent();
        registerBroadcastTabMessage(viewGroup.getContext());
    }

    public static TabMessageFragment newInstance(String address, Context context, ViewGroup viewGroup) {
        TabMessageFragment myFragment = new TabMessageFragment(context, viewGroup);
        Bundle args = new Bundle();
        args.putString("address", address);
        myFragment.setArguments(args);

        return myFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate()...");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView()...");

        return view;
    }


    private void initializeComponent() {
        messagesLogItems = messagesLogDBManager.getData();
        if (messagesLogItems == null) messagesLogItems = new ArrayList<>();
        listViewMessage = (ListView) view.findViewById(R.id.listViewMessage);
        nearestMessageAdapter = new NearestMessageAdapter();
        listViewMessage.setAdapter(nearestMessageAdapter);

    }

    private void registerBroadcastTabMessage(Context context) {
        if (broadcastTabMessage == null) {
            broadcastTabMessage = new BroadcastTabMessage();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(CommonValue.UPDATE_NEAREST_MESSAGE);
            context.registerReceiver(broadcastTabMessage, intentFilter);
        }
    }

    private class BroadcastTabMessage extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case CommonValue.UPDATE_NEAREST_MESSAGE:
                    Log.i(TAG, "BroadcastTabMessage_ onReceive");
                    String id = intent.getStringExtra(CommonValue.ID_NEAREST_MESSAGE);
                    String user = intent.getStringExtra(CommonValue.UPDATE_NEAREST_USER);
                    String type = intent.getStringExtra(CommonValue.UPDATE_NEAREST_TYPE);
                    String fullName = intent.getStringExtra(CommonValue.FULL_NAME_NEAREST_MESSAGE);
                    String content = intent.getStringExtra(CommonValue.CONTENT_NEAREST_MESSAGE);
                    String date = intent.getStringExtra(CommonValue.DATE_NEAREST_MESSAGE);
                    Spanned you = SpannableString.valueOf("You: ");
                    if (!user.equals(CommonValue.UPDATE_NEAAREST_MY))
                        you = SpannableString.valueOf(" ");
                    Spanned contentMain = null;
                    switch (type) {
                        case CommonValue.AHIHI_KEY:
                            contentMain = SpannableString.valueOf(content);
                            break;
                        case CommonValue.AHIHI_KEY_EMOTICON:
                            contentMain = Html.fromHtml("<u><font color='#827ca3'>" + "Emotion" + "</font></u>   ");
                            break;
                        case CommonValue.AHIHI_KEY_FILE:
                            contentMain = Html.fromHtml("<u><font color='#827ca3'>" + content + "</font></u>   ");
                            break;
                        case CommonValue.AHIHI_KEY_PICTURE:
                            contentMain = Html.fromHtml("<u><font color='#827ca3'>" + "Image" + "</font></u>   ");
                            break;
                    }
//                    SpannableString contentMessage = new SpannableString(" ");
//                    contentMessage.setSpan(TextUtils.concat(you, " ", contentMain), 0, 1, Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    int positon = checkHasIdMessagesLogItem(id);
                    if (positon > -1) {
                        messagesLogItems.get(positon).setMessage( new SpannableString(TextUtils.concat(you, " ", contentMain)));
                        messagesLogItems.get(positon).setDate(date);
                    } else {
                        messagesLogItems.add(new MessagesLogItem(id, fullName, new SpannableString(TextUtils.concat(you, " ", contentMain)), date));
                    }
                    nearestMessageAdapter.notifyDataSetChanged();
            }

        }
    }


    private int checkHasIdMessagesLogItem(String id) {
        for (int i = 0; i < messagesLogItems.size(); i++) {
            if (id.equals(messagesLogItems.get(i).getId())) return i;
        }
        return -1;
    }

    private class NearestMessageAdapter extends BaseAdapter {

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
                viewHolder.txtConent = (TextView) convertView.findViewById(R.id.txtContent);
                convertView.setTag(viewHolder);
            } else viewHolder = (ViewHolder) convertView.getTag();
            if (messagesLogItems.get(position).getAvatar() == null)
                viewHolder.imgAvatar.setImageResource(R.drawable.ic_ava_1);
            else viewHolder.imgAvatar.setImageBitmap(messagesLogItems.get(position).getAvatar());
            viewHolder.txtFullName.setText(messagesLogItems.get(position).getFullName());
            viewHolder.txtConent.setText(messagesLogItems.get(position).getMessage());
            return convertView;
        }
    }

    private class ViewHolder {
        private CircleImageView imgAvatar;
        private TextView txtFullName, txtConent;
    }

    @Override
    public void onDestroy() {
        if (broadcastTabMessage != null) {
            getActivity().unregisterReceiver(broadcastTabMessage);
            broadcastTabMessage = null;
        }
        super.onDestroy();
    }
}