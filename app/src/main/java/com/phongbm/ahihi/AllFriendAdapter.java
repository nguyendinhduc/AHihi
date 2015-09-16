package com.phongbm.ahihi;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.common.CommonValue;
import com.phongbm.common.OnShowPopupMenu;

import java.util.ArrayList;
import java.util.Collections;

import de.hdodenhof.circleimageview.CircleImageView;

public class AllFriendAdapter extends BaseAdapter {
    private static final String TAG = "FriendAdapter";

    private Handler handler;
    private ArrayList<AllFriendItem> allFriendItems;
    private ArrayList<ActiveFriendItem> activeFriendItems;
    private LayoutInflater layoutInflater;
    private OnShowPopupMenu onShowPopupMenu;

    public AllFriendAdapter(Context context, Handler handler) {
        layoutInflater = LayoutInflater.from(context);
        this.handler = handler;
        allFriendItems = new ArrayList<AllFriendItem>();
        activeFriendItems = new ArrayList<ActiveFriendItem>();
        if (MainActivity.isNetworkConnected(context))
            this.initializeListFriend();
    }

    private void initializeListFriend() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        ArrayList<String> listFriendId = (ArrayList<String>) currentUser.get("listFriend");
        if (listFriendId == null || listFriendId.size() == 0) {
            return;
        }
        for (int i = 0; i < listFriendId.size(); i++) {
            ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
            parseQuery.whereEqualTo("objectId", listFriendId.get(i));
            parseQuery.getFirstInBackground(new GetCallback<ParseUser>() {
                @Override
                public void done(final ParseUser parseUser, ParseException e) {
                    final ParseFile parseFile = (ParseFile) parseUser.get("avatar");
                    if (parseFile == null) {
                        return;
                    }
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e != null) {
                                return;
                            }
                            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            AllFriendItem allFriendItem = new AllFriendItem(parseUser.getObjectId(), avatar,
                                    parseUser.getUsername(), parseUser.getString("fullName"));

                            String urlAvatar = parseFile.getUrl();
                            allFriendItem.setUrlAvatar(urlAvatar);
                            allFriendItems.add(allFriendItem);

                            Collections.sort(allFriendItems);

                            if (parseUser.getBoolean("isOnline")) {
                                ActiveFriendItem activeFriendItem = new ActiveFriendItem(parseUser.getObjectId(),
                                        avatar, parseUser.getUsername(), parseUser.getString("fullName"));
                                activeFriendItem.setUrlAvatar(urlAvatar);
                                activeFriendItems.add(activeFriendItem);
                            }
                        }
                    });
                    Message message = new Message();
                    message.what = CommonValue.ACTION_UPDATE_LIST_FRIEND;
                    message.setTarget(handler);
                    message.sendToTarget();
                }
            });
        }
    }

    @Override
    public int getCount() {
        return allFriendItems.size();
    }

    @Override
    public AllFriendItem getItem(int position) {
        return allFriendItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_all_friend, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.menu = (ImageView) convertView.findViewById(R.id.menu);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imgAvatar.setImageBitmap(allFriendItems.get(position).getAvatar());
        viewHolder.txtName.setText(allFriendItems.get(position).getFullName());
        viewHolder.menu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onShowPopupMenu.onShowPopupMenuListener(position, viewHolder.menu);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        CircleImageView imgAvatar;
        TextView txtName;
        ImageView menu;
    }

    public void setOnShowPopupMenu(OnShowPopupMenu onShowPopupMenu) {
        this.onShowPopupMenu = onShowPopupMenu;
    }

    public ArrayList<AllFriendItem> getAllFriendItems() {
        return allFriendItems;
    }

    public ArrayList<ActiveFriendItem> getActiveFriendItems() {
        return activeFriendItems;
    }

}