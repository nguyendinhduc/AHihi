package com.phongbm.ahihi;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.phongbm.common.GlobalApplication;
import com.phongbm.common.OnShowPopupMenu;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ActiveFriendAdapter extends BaseAdapter {
    private LayoutInflater layoutInflater;
    private ArrayList<ActiveFriendItem> activeFriendItems;
    private OnShowPopupMenu onShowPopupMenu;

    public ActiveFriendAdapter(Context context, Activity activity) {
        layoutInflater = LayoutInflater.from(context);

        if (((GlobalApplication) activity.getApplication()).getAllFriendItems() != null) {
            activeFriendItems = ((GlobalApplication) activity.getApplication()).getActiveFriendItems();
        } else {
            activeFriendItems = new ArrayList<>();
        }
    }

    @Override
    public int getCount() {
        return activeFriendItems.size();
    }

    @Override
    public ActiveFriendItem getItem(int position) {
        return activeFriendItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        final ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_active_friend, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
            viewHolder.txtName = (TextView) convertView.findViewById(R.id.txtName);
            viewHolder.menu = (ImageView) convertView.findViewById(R.id.menu);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imgAvatar.setImageBitmap(activeFriendItems.get(position).getAvatar());
        viewHolder.txtName.setText(activeFriendItems.get(position).getFullName());
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

    public ArrayList<ActiveFriendItem> getActiveFriendItems() {
        return activeFriendItems;
    }

}
