package com.phongbm.ahihi;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;
    private String[] titles = new String[]{"Messages", "Contacts", "Friends", "AccountActivity"};

    public ViewPagerAdapter(Context context, FragmentManager fragmentManager) {
        super(fragmentManager);
        fragments = new ArrayList<>();
        fragments.add(new TabMessageFragment(context));
        fragments.add(new TabContactFragment(context));
        fragments.add(new TabFriendFragment(context));
        fragments.add(new TabFourFragment(context));
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

}