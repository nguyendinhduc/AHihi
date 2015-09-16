package com.phongbm.ahihi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

@SuppressLint("ValidFragment")
public class TabContactFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener,
        AbsListView.OnScrollListener {
    private static final String TAG = "TabContactFragment";

    private static final int TIME_REFRESH = 3000;

    private Context context;
    private View view;
    private ListView listViewContact;
    private SwipeRefreshLayout swipeRefreshLayout;

    public TabContactFragment(Context context) {
        this.context = context;
        LayoutInflater layoutInflater = LayoutInflater.from(context);
        view = layoutInflater.inflate(R.layout.tab_contact, null);
        this.initializeComponent();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        listViewContact.setAdapter(new ContactAdapter(context));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return view;
    }

    private void initializeComponent() {
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setColorSchemeColors(Color.parseColor("#f44336"),
                Color.parseColor("#2196f3"), Color.parseColor("#4caf50"));
        swipeRefreshLayout.setOnRefreshListener(this);
        listViewContact = (ListView) view.findViewById(R.id.listViewContact);
        listViewContact.setOnScrollListener(this);
    }

    @Override
    public void onRefresh() {
        swipeRefreshLayout.setRefreshing(true);
        (new Handler()).postDelayed(new Runnable() {
            @Override
            public void run() {
                swipeRefreshLayout.setRefreshing(false);
            }
        }, TIME_REFRESH);
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                         int totalItemCount) {
        boolean enable = false;
        if (listViewContact != null && listViewContact.getChildCount() > 0) {
            boolean firstItemVisible = listViewContact.getFirstVisiblePosition() == 0;
            boolean topOfFirstItemVisible = listViewContact.getChildAt(0).getTop() == 0;
            enable = firstItemVisible && topOfFirstItemVisible;
        }
        swipeRefreshLayout.setEnabled(enable);
    }

}