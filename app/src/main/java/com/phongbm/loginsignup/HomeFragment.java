package com.phongbm.loginsignup;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.phongbm.ahihi.R;

public class HomeFragment extends Fragment implements View.OnClickListener {
    private View view;
    private Button btnLogin, btnSignup;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, null);
        this.initializeComponent();
        return view;
    }

    private void initializeComponent() {
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnSignup = (Button) view.findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                ((MainFragment) this.getActivity()).showLoginFragment();
                break;
            case R.id.btnSignup:
                ((MainFragment) this.getActivity()).showSigupFragment();
                break;
        }
    }

}