package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.parse.ParseUser;
import com.phongbm.call.OutgoingCallActivity;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.common.OnShowPopupMenu;
import com.phongbm.message.MessageActivity;

import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressLint("ValidFragment")
public class TabFriendFragment extends Fragment implements View.OnClickListener, OnShowPopupMenu,
        SwipeRefreshLayout.OnRefreshListener, AbsListView.OnScrollListener {
    private static final String TAG = "TabFriendFragment";

    private View view;
    private ListView listViewFriend;
    private AllFriendAdapter allFriendAdapter;
    private ActiveFriendAdapter activeFriendAdapter;
    private TextView btnTabActive, btnTabAllFriends;
    private BroadcastUpdateListFriend broadcastUpdateListFriend = new BroadcastUpdateListFriend();
    private boolean tabActive = true;
    private Context context;
    private CircleImageView imgAvatar;
    private TextView txtFullName, txtStatus;
    private Switch switchOnline;
    private RelativeLayout layoutMe;
    private boolean enableTabAllFriend = false, isOnline = true;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RelativeLayout layoutNote;

    public TabFriendFragment(Context context, ViewGroup viewGroup) {
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_friend, null);
        this.initializeComponent();
    }

    public static TabFriendFragment instantTabContactFragment( Context context, ViewGroup viewGroup ) {
        TabFriendFragment tabFriendFragment = new TabFriendFragment(context, viewGroup);
        Bundle args = new Bundle();
        args.putString("address", "TabFriendFragment");
        tabFriendFragment.setArguments(args);
        return  tabFriendFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.registerUpdateListFriend();
        context = this.getActivity();
        activeFriendAdapter = new ActiveFriendAdapter(this.getActivity(), this.getActivity());
        activeFriendAdapter.setOnShowPopupMenu(this);
        listViewFriend.setAdapter(activeFriendAdapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.initializeProfile();
        return view;
    }

    private void initializeComponent() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#f44336"),
                Color.parseColor("#2196f3"), Color.parseColor("#4caf50"));
        swipeRefreshLayout.setOnRefreshListener(this);

        listViewFriend = (ListView) view.findViewById(R.id.listViewFriend);
        listViewFriend.setOnScrollListener(this);

        layoutNote = (RelativeLayout) view.findViewById(R.id.layoutNote);

        btnTabActive = (TextView) view.findViewById(R.id.btnTabActive);
        btnTabActive.setOnClickListener(this);
        btnTabAllFriends = (TextView) view.findViewById(R.id.btnTabAllFriends);
        btnTabAllFriends.setOnClickListener(this);

        layoutMe = (RelativeLayout) view.findViewById(R.id.layoutMe);

        listViewFriend.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String inComingId, inComingFullName;
                if (tabActive) {
                    inComingId = activeFriendAdapter.getItem(position).getId();
                    inComingFullName = activeFriendAdapter.getItem(position).getFullName();
                } else {
                    inComingId = allFriendAdapter.getItem(position).getId();
                    inComingFullName = allFriendAdapter.getItem(position).getFullName();
                }
                Intent intentChat = new Intent(context, MessageActivity.class);
                intentChat.putExtra(CommonValue.INCOMING_CALL_ID, inComingId);
                intentChat.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, inComingFullName);
                context.startActivity(intentChat);
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
                    isOnline = true;
                    listViewFriend.setVisibility(View.VISIBLE);
                    layoutNote.setVisibility(View.GONE);

                    ParseUser currentUser = ParseUser.getCurrentUser();
                    currentUser.put("isOnline", true);
                    currentUser.saveInBackground();
                } else {
                    txtStatus.setText("OFFLINE");
                    txtStatus.setTextColor(Color.GRAY);
                    listViewFriend.setAdapter(null);
                    isOnline = false;
                    layoutNote.setVisibility(View.VISIBLE);
                    listViewFriend.setVisibility(View.GONE);

                    ParseUser currentUser = ParseUser.getCurrentUser();
                    currentUser.put("isOnline", false);
                    currentUser.saveInBackground();
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
                this.changeStateShow(btnTabActive);
                this.changeStateHide(btnTabAllFriends);
                listViewFriend.setAdapter(activeFriendAdapter);
                if (isOnline) {
                    listViewFriend.setVisibility(View.VISIBLE);
                    layoutNote.setVisibility(View.GONE);
                } else {
                    listViewFriend.setVisibility(View.GONE);
                    layoutNote.setVisibility(View.VISIBLE);
                }
                layoutMe.setVisibility(View.VISIBLE);
                tabActive = true;
                break;
            case R.id.btnTabAllFriends:
                if (!enableTabAllFriend) {
                    Log.i(TAG, "enableTabAllFriend...");
                    enableTabAllFriend = true;
                    allFriendAdapter = new AllFriendAdapter(this.getActivity(), this.getActivity());
                    allFriendAdapter.setOnShowPopupMenu(this);
                }
                this.changeStateShow(btnTabAllFriends);
                this.changeStateHide(btnTabActive);
                listViewFriend.setAdapter(allFriendAdapter);
                listViewFriend.setVisibility(View.VISIBLE);
                layoutNote.setVisibility(View.GONE);
                layoutMe.setVisibility(View.GONE);
                tabActive = false;
                break;
        }
    }

    private void changeStateShow(TextView textView) {
        textView.setBackgroundResource(R.color.green_500);
        textView.setTextColor(Color.WHITE);
    }

    private void changeStateHide(TextView textView) {
        textView.setBackgroundResource(R.drawable.bg_button_friend_green);
        textView.setTextColor(Color.parseColor("#4caf50"));
    }

    @Override
    public void onShowPopupMenuListener(final int position, View view) {
        final String inComingId, inComingFullName;
        if (tabActive) {
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
                        Intent intentProfile = new Intent(getActivity(), DetailActivity.class);
                        intentProfile.putExtra(CommonValue.USER_ID, inComingId);
                        TabFriendFragment.this.getActivity().startActivity(intentProfile);
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    @Override
    public void onRefresh() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        CommonMethod.getInstance().loadListFriend(currentUser, this.getActivity());
        final ProgressDialog progressDialog = new ProgressDialog(this.getActivity());
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        swipeRefreshLayout.setRefreshing(true);
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
                progressDialog.dismiss();
            }
        }, 3000);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        boolean enable = false;
        if (listViewFriend != null && listViewFriend.getChildCount() > 0) {
            boolean firstItemVisible = listViewFriend.getFirstVisiblePosition() == 0;
            boolean topOfFirstItemVisible = listViewFriend.getChildAt(0).getTop() == 0;
            enable = firstItemVisible && topOfFirstItemVisible;
        }
        swipeRefreshLayout.setEnabled(enable);
    }

    private class BroadcastUpdateListFriend extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(CommonValue.ACTION_ADD_FRIEND)) {
                FriendItem newFriend = ((MainActivity) TabFriendFragment.this.getActivity()).getNewFriend();
                AllFriendItem allFriendItem = new AllFriendItem(newFriend.getId(),
                        newFriend.getAvatar(), newFriend.getPhoneNumber(), newFriend.getFullName());
                if (allFriendAdapter == null) {
                    allFriendAdapter = new AllFriendAdapter(TabFriendFragment.this.getActivity(),
                            TabFriendFragment.this.getActivity());
                }
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

}