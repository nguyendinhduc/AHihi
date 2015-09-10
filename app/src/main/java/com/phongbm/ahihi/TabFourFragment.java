package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

@SuppressLint("ValidFragment")
public class TabFourFragment extends Fragment {
    private static final String TAG = "TabFourFragment";
    private View view;

    public TabFourFragment(Context context, ViewGroup viewGroup) {
        super();
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_four, viewGroup, false);
    }

    public static TabFourFragment newInstance(String address, Context context, ViewGroup viewGroup) {
        TabFourFragment myFragment = new TabFourFragment(context, viewGroup);
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

}