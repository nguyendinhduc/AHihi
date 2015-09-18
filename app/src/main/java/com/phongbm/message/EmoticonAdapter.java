package com.phongbm.message;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.libs.SquareImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class EmoticonAdapter extends BaseAdapter {
    private ArrayList<EmoticonItem> emoticonItems;
    private LayoutInflater layoutInflater;
    private String inComingMessageId, inComingFullName;
    private final int SIZE_EMOTION;

    public EmoticonAdapter(Context context, ArrayList<EmoticonItem> emoticonItems,
                           String inComingMessageId, String inComingFullName) {
        SIZE_EMOTION = (int)( GlobalApplication.WIDTH_SCREEN - 5 * 12 * ( GlobalApplication.DENSITY_DPI/160 ) ) / 4;
        this.emoticonItems = emoticonItems;
        this.inComingMessageId = inComingMessageId;
        this.inComingFullName = inComingFullName;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return emoticonItems.size();
    }

    @Override
    public EmoticonItem getItem(int position) {
        return emoticonItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return emoticonItems.get(position).getEmotionId();
    }

    @Override
    public View getView(final int position, View convertView, final ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = layoutInflater.inflate(R.layout.item_emoticon, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.imgEmoticon = (SquareImageView) convertView.findViewById(R.id.imgEmoticon);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Picasso.with(parent.getContext())
                .load(emoticonItems.get(position).getEmotionId())
                .resize(SIZE_EMOTION, SIZE_EMOTION)
                .centerCrop()
                .placeholder(R.drawable.loading_picture)
                .into(viewHolder.imgEmoticon);
        viewHolder.imgEmoticon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                int emoticonId = (int) EmoticonAdapter.this.getItemId(position);
//                Intent intentEmoticon = new Intent();
//                intentEmoticon.setAction(CommonValue.ACTION_SEND_MESSAGE);
//                intentEmoticon.putExtra(CommonValue.INCOMING_MESSAGE_ID, inComingMessageId);
//
//                intentEmoticon.putExtra(CommonValue.INCOMING_MESSAGE_FULL_NAME, inComingFullName);
//                intentEmoticon.putExtra(CommonValue.MESSAGE_CONTENT, "" + emoticonId);
//                intentEmoticon.putExtra(CommonValue.AHIHI_KEY, CommonValue.AHIHI_KEY_EMOTICON);
//                intentEmoticon.putExtra(CommonValue.AHIHI_KEY_DATE,
//                        CommonMethod.getInstance().getMessageDate());
//                parent.getContext().sendBroadcast(intentEmoticon);
                Intent intent = new Intent();
                intent.setAction(CommonValue.MESSAGE_SEND_EMOTION);
                String emoticonId = (int) EmoticonAdapter.this.getItemId(position) + "";
                intent.putExtra(CommonValue.KEY_EMOTION, emoticonId);
                Log.i("EmoticonAdapter","setOnClickListener_ key: " +  emoticonId);
                parent.getContext().sendBroadcast(intent);
            }
        });
        return convertView;
    }

    private class ViewHolder {
        SquareImageView imgEmoticon;
    }

}