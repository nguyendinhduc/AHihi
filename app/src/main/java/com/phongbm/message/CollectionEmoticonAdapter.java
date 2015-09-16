package com.phongbm.message;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.phongbm.ahihi.R;

import java.util.ArrayList;

public class CollectionEmoticonAdapter extends PagerAdapter {
    private ArrayList<CollectionEmoticonItem> collectionEmoticonItems;
    private LayoutInflater layoutInflater;

    public CollectionEmoticonAdapter(Context context,
                                     ArrayList<CollectionEmoticonItem> collectionEmoticonItems) {
        this.collectionEmoticonItems = collectionEmoticonItems;
        layoutInflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return collectionEmoticonItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view.equals((View) object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View view = layoutInflater.inflate(R.layout.item_collection_emoticon, null);
        GridView gridViewPage = (GridView) view.findViewById(R.id.gridViewEmoticon);
        gridViewPage.setAdapter(collectionEmoticonItems.get(position).getEmoticonAdapter());
        ((ViewPager) container).addView(view);
        return view;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        ((ViewPager) container).removeView((View) object);
    }


}