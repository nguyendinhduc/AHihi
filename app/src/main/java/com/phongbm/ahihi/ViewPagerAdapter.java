package com.phongbm.ahihi;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private ArrayList<Fragment> fragments;
    private String[] titles = new String[]{"Messages", "Contacts", "Friends", "Account"};

    public ViewPagerAdapter(Context context, FragmentManager fragmentManager, ViewGroup viewGroup) {
        super(fragmentManager);
        fragments = new ArrayList<>();
        fragments.add( TabMessageFragment.newInstance("TabMessageFragment", context, viewGroup));
        fragments.add(TabContactFragment.newInstance("TabContactFragment", context, viewGroup));
        fragments.add(TabFriendFragment.newInstance("TabFriendFragment", context, viewGroup));
        fragments.add(TabFourFragment.newInstance("TabFourFragment", context, viewGroup));
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