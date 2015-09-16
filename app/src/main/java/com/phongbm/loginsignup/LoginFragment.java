package com.phongbm.loginsignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseAnonymousUtils;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.ahihi.MainActivity;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

import java.util.Arrays;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";
    private static final int REQUEST_LOGIN_FRAGMENT = 0;

    private View view;
    private EditText edtPhoneNumber, edtPassword, edtCode;
    private Button btnLogin;
    private String countryCode;
    private TextView txtLogo, forgotPassword, register;
    private boolean isFillPhoneNumber, isFillPassword;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        this.initializeToolbar();
        this.initializeComponent();
        return view;
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((MainFragment) this.getActivity()).setSupportActionBar(toolbar);
        this.getActivity().setTitle("LOG IN");
    }

    private void initializeComponent() {
        txtLogo = (TextView) view.findViewById(R.id.txtLogo);
        txtLogo.setTypeface(Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/AIRSTREA.TTF"));
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        forgotPassword = (TextView) view.findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);
        register = (TextView) view.findViewById(R.id.register);
        register.setOnClickListener(this);
        edtPhoneNumber = (EditText) view.findViewById(R.id.edtPhoneNumber);
        edtPassword = (EditText) view.findViewById(R.id.edtPassword);
        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    isFillPhoneNumber = true;
                    enabledButtonLogin();
                } else {
                    isFillPhoneNumber = false;
                    btnLogin.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        edtPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    isFillPassword = true;
                    enabledButtonLogin();
                } else {
                    isFillPassword = false;
                    btnLogin.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        edtCode = (EditText) view.findViewById(R.id.edtCode);
        edtCode.setOnClickListener(this);
        edtCode.setText("United States (+1)");
        countryCode = "(+1)";
    }

    private void enabledButtonLogin() {
        if (isFillPhoneNumber && isFillPassword) {
            btnLogin.setEnabled(true);
        }
    }

    private ProgressDialog progressDialog;

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                progressDialog = new ProgressDialog(getActivity());
                progressDialog.setTitle("Logging in");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                String phoneNumber = edtPhoneNumber.getText().toString().trim();
                if (phoneNumber.charAt(0) == '0') {
                    phoneNumber = phoneNumber.substring(1);
                }
                final String phoneNumberStand = countryCode + " " + phoneNumber;
                final String password = edtPassword.getText().toString().trim();
                ParseUser.logInInBackground(phoneNumberStand, password, new LogInCallback() {
                    @Override
                    public void done(ParseUser parseUser, ParseException e) {
                        if (parseUser != null) {
                            Log.i(TAG, "Login success!!!");
                            parseUser.pinInBackground();
                            parseUser.put("isOnline", true);
                            String objectId = parseUser.getObjectId();
                            if ( !((GlobalApplication)getActivity().getApplication()).getIdUers().contains(objectId) ) {
                                ((GlobalApplication)getActivity().getApplication()).addIdUser(objectId);
                                GlobalApplication.startWaittingAHihi = false;
                                GlobalApplication.checkLoginThisId = false;
                                GlobalApplication.startActivityMessage = false;
                            }
                            else {
                                GlobalApplication.startWaittingAHihi = true;
                                GlobalApplication.checkLoginThisId = true;
                                GlobalApplication.startActivityMessage = false;
                            }
                            Intent intent = new Intent(LoginFragment.this.getActivity(), MainActivity.class);
                            LoginFragment.this.getActivity().startActivity(intent);

                            progressDialog.dismiss();
                            LoginFragment.this.getActivity().finish();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(LoginFragment.this.getActivity(),
                                    "There was an error logging in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                break;
            case R.id.forgotPassword:
                break;
            case R.id.register:
                ((MainFragment) this.getActivity()).showSigupFragment();
                break;
            case R.id.edtCode:
                Intent intent = new Intent(this.getActivity(), CountryCodeActivity.class);
                startActivityForResult(intent, REQUEST_LOGIN_FRAGMENT);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_LOGIN_FRAGMENT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(CommonValue.COUNTRY_CODE);
                countryCode = content.substring(content.indexOf("(+"));
                edtCode.setText(content);
            }
        }
    }

}