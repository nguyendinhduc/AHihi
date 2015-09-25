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

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

public class SignupFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SignupFragment";
    private static final int REQUEST_SIGN_UP_FRAGMENT = 0;

    private View view;
    private EditText edtPhoneNumber, edtPassword, edtConfirmPassword, edtCode;
    private FloatingActionButton btnLogIn;
    private Button btnSignUp;
    private CheckBox checkBoxAgree;
    private boolean isFillPhoneNumber, isFillPassword, isFillConfirmPassword, isCheckBoxChecked;
    private String countryCode;
    private Activity activity;
    private String phoneNumber;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, null);
        this.initializeComponent();
        activity = this.getActivity();
        return view;
    }

    private void initializeComponent() {
        btnSignUp = (Button) view.findViewById(R.id.btnSignUp);
        btnSignUp.setOnClickListener(this);
        btnLogIn = (FloatingActionButton) view.findViewById(R.id.btnLogIn);
        btnLogIn.setOnClickListener(this);
        edtPhoneNumber = (EditText) view.findViewById(R.id.edtPhoneNumber);
        edtPassword = (EditText) view.findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText) view.findViewById(R.id.edtConfirmPassword);
        checkBoxAgree = (CheckBox) view.findViewById(R.id.checkBoxAgree);
        checkBoxAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBoxAgree.isChecked()) {
                    isCheckBoxChecked = true;
                    SignupFragment.this.enabledButtonSignUp();
                    checkBoxAgree.setTextColor(ContextCompat.getColor(activity, R.color.green_500));
                } else {
                    isCheckBoxChecked = false;
                    btnSignUp.setEnabled(false);
                    checkBoxAgree.setTextColor(Color.parseColor("#666666"));
                }
            }
        });
        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillPhoneNumber = true;
                    SignupFragment.this.enabledButtonSignUp();
                } else {
                    isFillPhoneNumber = false;
                    btnSignUp.setEnabled(false);
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
                    SignupFragment.this.enabledButtonSignUp();
                } else {
                    isFillPassword = false;
                    SignupFragment.this.enabledButtonSignUp();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtPassword.getText().toString().length() > 0
                        && edtConfirmPassword.getText().toString().length() > 0) {
                    if (!edtPassword.getText().toString()
                            .equals(edtConfirmPassword.getText().toString())) {
                        edtPassword.setError("'Password' and 'Confirm Password' do not match");
                    } else {
                        edtPassword.setError(null);
                        edtConfirmPassword.setError(null);
                    }
                } else {
                    edtPassword.setError(null);
                    edtConfirmPassword.setError(null);
                }
            }
        });
        edtConfirmPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillConfirmPassword = true;
                    SignupFragment.this.enabledButtonSignUp();
                } else {
                    isFillConfirmPassword = false;
                    SignupFragment.this.enabledButtonSignUp();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (edtPassword.getText().toString().length() > 0
                        && edtConfirmPassword.getText().toString().length() > 0) {
                    if (!edtConfirmPassword.getText().toString()
                            .equals(edtPassword.getText().toString())) {
                        edtConfirmPassword.setError("'Confirm Password' and 'Password' do not match");
                    } else {
                        edtConfirmPassword.setError(null);
                        edtPassword.setError(null);
                    }
                } else {
                    edtConfirmPassword.setError(null);
                    edtPassword.setError(null);
                }
            }
        });
        edtCode = (EditText) view.findViewById(R.id.edtCode);
        edtCode.setOnClickListener(this);
        edtCode.setText("United States (+1)");
        countryCode = "(+1)";
    }

    private void enabledButtonSignUp() {
        if (isFillPhoneNumber && isFillPassword && isFillConfirmPassword && isCheckBoxChecked
                && edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
            btnSignUp.setEnabled(true);
        }
    }

    @Override
    public void onClick(final View view) {
        switch (view.getId()) {
            case R.id.btnSignUp:
                final ProgressDialog progressDialog = new ProgressDialog(activity);
                progressDialog.setTitle("Signing up");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final ParseUser newUser = new ParseUser();
                phoneNumber = edtPhoneNumber.getText().toString().trim();
                if (phoneNumber.charAt(0) == '0') {
                    phoneNumber = phoneNumber.substring(1);
                }
                newUser.setUsername(countryCode + " " + phoneNumber);
                newUser.setPassword(edtPassword.getText().toString().trim());
                newUser.signUpInBackground(new SignUpCallback() {
                    @Override
                    public void done(ParseException e) {
                        if (e == null) {
                            newUser.put("isOnline", true);
                            newUser.saveInBackground();

                            progressDialog.dismiss();
                            btnLogIn.setBackgroundTintList(ColorStateList
                                    .valueOf(Color.parseColor("#e91e63")));
                            btnLogIn.setImageResource(R.drawable.ic_checkmark);
                            Snackbar snackbar = Snackbar.make(view, "Registered successfully",
                                    Snackbar.LENGTH_LONG)
                                    .setAction("ACTION", null)
                                    .setCallback(new Snackbar.Callback() {
                                        @Override
                                        public void onDismissed(Snackbar snackbar, int event) {
                                            super.onDismissed(snackbar, event);

                                            ((GlobalApplication) getActivity().getApplication()).
                                                    addIdUser(newUser.getObjectId());
                                            GlobalApplication.startWaitingAHihi = true;
                                            GlobalApplication.checkLoginThisId = true;
                                            GlobalApplication.startActivityMessage = false;

                                            ((MainFragment) activity).showProfileInformationFragment();
                                        }
                                    });
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
                            snackbar.show();
                        } else {
                            progressDialog.dismiss();
                            Snackbar.make(view, "There was an error signing up", Snackbar.LENGTH_LONG)
                                    .setAction("ACTION", null)
                                    .show();
                        }
                    }
                });
                break;
            case R.id.btnLogIn:
                ((MainFragment) activity).showLoginFragment();
                break;
            case R.id.edtCode:
                Intent intent = new Intent(activity, CountryCodeActivity.class);
                this.startActivityForResult(intent, REQUEST_SIGN_UP_FRAGMENT);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGN_UP_FRAGMENT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(CommonValue.COUNTRY_CODE);
                countryCode = content.substring(content.indexOf("(+"));
                edtCode.setText(content);
            }
        }
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}