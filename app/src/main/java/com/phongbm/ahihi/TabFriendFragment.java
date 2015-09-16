package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.hudomju.swipe.SwipeToDismissTouchListener;
import com.hudomju.swipe.adapter.ListViewAdapter;
import com.phongbm.call.OutgoingCallActivity;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.common.OnShowPopupMenu;
import com.phongbm.message.MessageActivity;

import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabFriendFragment extends Fragment implements View.OnClickListener, OnShowPopupMenu {
    private static final String TAG = "TabFriendFragment";

    private View view;
    private ListView listViewFriend;
    private AllFriendAdapter allFriendAdapter;
    private ActiveFriendAdapter activeFriendAdapter;
    private TextView btnTabActive, btnTabAllFriends;
    private BroadcastUpdateListFriend broadcastUpdateListFriend = new BroadcastUpdateListFriend();
    private boolean activeFriendAdapterVisible = true;
    private SwipeToDismissTouchListener<ListViewAdapter> touchListener;
    private Context context;
    private CircleImageView imgAvatar;
    private TextView txtFullName, txtStatus;
    private Switch switchOnline;

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case CommonValue.ACTION_UPDATE_LIST_FRIEND:
                    allFriendAdapter.notifyDataSetChanged();
                    activeFriendAdapter.setActiveFriendItems(
                            allFriendAdapter.getActiveFriendItems());
                    activeFriendAdapter.notifyDataSetChanged();
                    break;
            }
        }
    };

    public TabFriendFragment(Context context) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_friend, null);
        this.initializeComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerUpdateListFriend();
        context = this.getActivity();
        allFriendAdapter = new AllFriendAdapter(this.getActivity(), handler);
        activeFriendAdapter = new ActiveFriendAdapter(this.getActivity());
        allFriendAdapter.setOnShowPopupMenu(this);
        activeFriendAdapter.setOnShowPopupMenu(this);
        listViewFriend.setAdapter(activeFriendAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.initializeProfile();
        return view;
    }

    private void initializeComponent() {
        listViewFriend = (ListView) view.findViewById(R.id.listViewFriend);

        btnTabActive = (TextView) view.findViewById(R.id.btnTabActive);
        btnTabActive.setOnClickListener(this);
        btnTabAllFriends = (TextView) view.findViewById(R.id.btnTabAllFriends);
        btnTabAllFriends.setOnClickListener(this);

        touchListener = new SwipeToDismissTouchListener<>(new ListViewAdapter(listViewFriend),
                new SwipeToDismissTouchListener.DismissCallbacks<ListViewAdapter>() {
                    @Override
                    public boolean canDismiss(int position) {
                        return true;
                    }

                    @Override
                    public void onDismiss(ListViewAdapter view, int position) {
                        Toast.makeText(context, "onDismiss...", Toast.LENGTH_SHORT).show();
                    }
                });
        listViewFriend.setOnTouchListener(touchListener);
        listViewFriend.setOnScrollListener((AbsListView.OnScrollListener) touchListener.makeScrollListener());
        listViewFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (touchListener.existPendingDismisses()) {
                    touchListener.undoPendingDismiss();
                } else {
                    String inComingId, inComingFullName;
                    String urlAvatar;
                    if (activeFriendAdapterVisible) {
                        inComingId = activeFriendAdapter.getItem(position).getId();
                        inComingFullName = activeFriendAdapter.getItem(position).getFullName();
                        urlAvatar = activeFriendAdapter.getItem(position).getUrlAvatar();
                    } else {
                        inComingId = allFriendAdapter.getItem(position).getId();
                        inComingFullName = allFriendAdapter.getItem(position).getFullName();
                        urlAvatar = allFriendAdapter.getItem(position).getUrlAvatar();
                    }
                    Log.i(TAG, "setOnItemClickListener_ urlAvatar: " + urlAvatar);
                    Intent intentChat = new Intent(context, MessageActivity.class);
                    intentChat.putExtra(CommonValue.INCOMING_CALL_ID, inComingId);
                    intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, inComingFullName);
                    intentChat.putExtra(CommonValue.MESSAGE_LOG_LINK_AVATAR_RECEVER, urlAvatar);
                    GlobalApplication.linkAvatarReceiver = urlAvatar;
                    context.startActivity(intentChat);
                }
            }
        });
    }

    private void initializeProfile() {
        imgAvatar = (CircleImageView) view.findViewById(R.id.imgAvatar);
        imgAvatar.setImageBitmap(((GlobalApplication) this.getActivity().getApplication()).getAvatar());
        txtFullName = (TextView) view.findViewById(R.id.txtFullName);
        txtFullName.setText(((GlobalApplication) this.getActivity().getApplication()).getFullName());
        txtStatus = (TextView) view.findViewById(R.id.txtStatus);
        txtStatus.setText("ONLINE");
        switchOnline = (Switch) view.findViewById(R.id.switchOnline);
        switchOnline.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    txtStatus.setText("ONLINE");
                    txtStatus.setTextColor(ContextCompat.getColor(context, R.color.green_500));
                    listViewFriend.setAdapter(activeFriendAdapter);
                } else {
                    txtStatus.setText("OFFLINE");
                    txtStatus.setTextColor(Color.GRAY);
                    listViewFriend.setAdapter(null);
                }
            }
        });
    }

    public void registerUpdateListFriend() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(CommonValue.ACTION_ADD_FRIEND);
        this.getActivity().registerReceiver(broadcastUpdateListFriend, filter);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnTabActive:
                changeStateShow(btnTabActive);
                changeStateHide(btnTabAllFriends);
                listViewFriend.setAdapter(activeFriendAdapter);
                activeFriendAdapterVisible = true;
                break;
            case R.id.btnTabAllFriends:
                changeStateShow(btnTabAllFriends);
                changeStateHide(btnTabActive);
                listViewFriend.setAdapter(allFriendAdapter);
                activeFriendAdapterVisible = false;
                break;
        }
    }

    private void changeStateShow(TextView txt) {
        txt.setBackgroundResource(R.color.green_500);
        txt.setTextColor(Color.WHITE);
    }

    private void changeStateHide(TextView txt) {
        txt.setBackgroundResource(R.drawable.bg_button_friend_green);
        txt.setTextColor(Color.parseColor("#4caf50"));
    }

    @Override
    public void onShowPopupMenuListener(int position, View view) {
        final String inComingId, inComingFullName;
        if (activeFriendAdapterVisible) {
            inComingId = activeFriendAdapter.getItem(position).getId();
            inComingFullName = activeFriendAdapter.getItem(position).getFullName();
        } else {
            inComingId = allFriendAdapter.getItem(position).getId();
            inComingFullName = allFriendAdapter.getItem(position).getFullName();
        }
        PopupMenu popup = new PopupMenu(TabFriendFragment.this.getActivity(), view);
        popup.getMenuInflater().inflate(R.menu.popup_menu, popup.getMenu());
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_open_chat:
                        Intent intentChat = new Intent(getActivity(), MessageActivity.class);
                        intentChat.putExtra(CommonValue.INCOMING_CALL_ID, inComingId);
                        intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, inComingFullName);
                        TabFriendFragment.this.getActivity().startActivity(intentChat);
                        break;
                    case R.id.action_voice_call:
                        Intent intentCall = new Intent(getActivity(), OutgoingCallActivity.class);
                        intentCall.putExtra(CommonValue.INCOMING_CALL_ID, inComingId);
                        TabFriendFragment.this.getActivity().startActivity(intentCall);
                        break;
                    case R.id.action_view_profile:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    private class BroadcastUpdateListFriend extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CommonValue.ACTION_ADD_FRIEND)) {
                FriendItem newFriend = ((MainActivity) getActivity()).getNewFriend();
                AllFriendItem allFriendItem = new AllFriendItem(newFriend.getId(),
                        newFriend.getAvatar(), newFriend.getPhoneNumber(), newFriend.getFullName());
                allFriendAdapter.getAllFriendItems().add(allFriendItem);
                Collections.sort(allFriendAdapter.getAllFriendItems());
                allFriendAdapter.notifyDataSetChanged();

                if (intent.getBooleanExtra("isOnline", true)) {
                    ActiveFriendItem activeFriendItem = new ActiveFriendItem(newFriend.getId(),
                            newFriend.getAvatar(), newFriend.getPhoneNumber(), newFriend.getFullName());
                    activeFriendAdapter.getActiveFriendItems().add(activeFriendItem);
                    activeFriendAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        this.getActivity().unregisterReceiver(broadcastUpdateListFriend);
        super.onDestroy();
    }

    public AllFriendAdapter getAllFriendAdapter() {
        return allFriendAdapter;
    }

    public ActiveFriendAdapter getActiveFriendAdapter() {
        return activeFriendAdapter;
    }

}