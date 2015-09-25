package com.phongbm.ahihi;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewMessageAdapter extends BaseAdapter implements Filterable {
    private LayoutInflater layoutInflater;
    private ArrayList<AllFriendItem> allFriendItems;
    private ArrayList<AllFriendItem> allFriendItemsOrigin;
    private ValueFilter valueFilter;

    public NewMessageAdapter(Context context, ArrayList<AllFriendItem> allFriendItems) {
        layoutInflater = LayoutInflater.from(context);
        this.allFriendItems = allFriendItems;
        this.allFriendItemsOrigin = allFriendItems;
        valueFilter = new ValueFilter();
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
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_new_message, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgAvatar = (CircleImageView) convertView.findViewById(R.id.imgAvatar);
            viewHolder.txtFullName = (TextView) convertView.findViewById(R.id.txtFullName);
            viewHolder.checkBoxOK = (CheckBox) convertView.findViewById(R.id.checkBoxOK);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.imgAvatar.setImageBitmap(allFriendItems.get(position).getAvatar());
        viewHolder.txtFullName.setText(allFriendItems.get(position).getFullName());
        return convertView;
    }

    @Override
    public Filter getFilter() {
        if (valueFilter == null)
            return new ValueFilter();
        return valueFilter;
    }

    private class ViewHolder {
        CircleImageView imgAvatar;
        TextView txtFullName;
        CheckBox checkBoxOK;
    }

    private class ValueFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            FilterResults filterResults = new FilterResults();
            if (constraint != null && constraint.length() > 0) {
                ArrayList<AllFriendItem> allFriendItemsResult = new ArrayList<>();
                for (int i = 0; i < allFriendItemsOrigin.size(); i++) {
                    if (allFriendItemsOrigin.get(i).getFullName().toLowerCase()
                            .contains(constraint.toString().toLowerCase())) {
                        allFriendItemsResult.add(allFriendItemsOrigin.get(i));
                    }
                }
                allFriendItems = allFriendItemsResult;
                filterResults.count = allFriendItemsResult.size();
                filterResults.values = allFriendItemsResult;
            } else {
                allFriendItems = allFriendItemsOrigin;
                filterResults.count = allFriendItemsOrigin.size();
                filterResults.values = allFriendItemsOrigin;
            }
            return filterResults;
        }

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            allFriendItems = (ArrayList<AllFriendItem>) results.values;
            NewMessageAdapter.this.notifyDataSetChanged();
        }
    }

}