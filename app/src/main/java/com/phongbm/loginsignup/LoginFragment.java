package com.phongbm.loginsignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.phongbm.ahihi.MainActivity;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

public class LoginFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "LoginFragment";
    private static final int REQUEST_LOGIN_FRAGMENT = 0;

    private View view;
    private EditText edtPhoneNumber, edtPassword, edtCode;
    private Button btnLogin;
    private String countryCode;
    private TextView forgotPassword;
    private CheckBox rememberMe;
    private boolean isFillPhoneNumber, isFillPassword;
    private ProgressDialog progressDialog;
    private Activity activity;
    private FloatingActionButton btnSignUp;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        this.initializeComponent();
        activity = this.getActivity();
        return view;
    }

    private void initializeComponent() {
        btnLogin = (Button) view.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(this);
        btnSignUp = (FloatingActionButton) view.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
        rememberMe = (CheckBox) view.findViewById(R.id.rememberMe);
        rememberMe.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    rememberMe.setTextColor(ContextCompat.getColor(activity, R.color.green_500));
                } else {
                    rememberMe.setTextColor(Color.parseColor("#666666"));
                }
            }
        });
        forgotPassword = (TextView) view.findViewById(R.id.forgotPassword);
        forgotPassword.setOnClickListener(this);
        edtPhoneNumber = (EditText) view.findViewById(R.id.edtPhoneNumber);
        edtPassword = (EditText) view.findViewById(R.id.edtPassword);
        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillPhoneNumber = true;
                    LoginFragment.this.enabledButtonLogin();
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
                if (s != null && s.length() > 1) {
                    isFillPassword = true;
                    LoginFragment.this.enabledButtonLogin();
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

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btnLogin:
                progressDialog = new ProgressDialog(activity);
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
                            parseUser.put("isOnline", true);
                            parseUser.saveInBackground();

                            String objectId = parseUser.getObjectId();
                            GlobalApplication globalApplication = (GlobalApplication)
                                    getActivity().getApplication();
                            if (!globalApplication.getIdUers().contains(objectId)) {
                                globalApplication.addIdUser(objectId);
                                GlobalApplication.startWaitingAHihi = false;
                                GlobalApplication.checkLoginThisId = false;
                                GlobalApplication.startActivityMessage = false;
                            } else {
                                GlobalApplication.startWaitingAHihi = true;
                                GlobalApplication.checkLoginThisId = true;
                                GlobalApplication.startActivityMessage = false;
                            }

                            progressDialog.dismiss();
                            btnSignUp.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#e91e63")));
                            btnSignUp.setImageResource(R.drawable.ic_checkmark);
                            Snackbar snackbar = Snackbar.make(view, "Logged successfully", Snackbar.LENGTH_LONG)
                                    .setAction("ACTION", null)
                                    .setCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            super.onDismissed(snackbar, event);
                                            Intent intent = new Intent(activity, MainActivity.class);
                                            activity.startActivity(intent);
                                            activity.finish();
                                        }
                                    });
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
                            snackbar.show();
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(view, "There was an error logging in", Snackbar.LENGTH_LONG)
                                    .setAction("ACTION", null)
                                    .show();
                        }
                    }
                });
                break;
            case R.id.forgotPassword:
                Snackbar.make(view, "Features are being developed", Snackbar.LENGTH_LONG)
                        .setAction("ACTION", null)
                        .show();
                break;
            case R.id.btnSignUp:
                ((MainFragment) activity).showSigupFragment();
                break;
            case R.id.edtCode:
                Intent intent = new Intent(activity, CountryCodeActivity.class);
                this.startActivityForResult(intent, REQUEST_LOGIN_FRAGMENT);
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