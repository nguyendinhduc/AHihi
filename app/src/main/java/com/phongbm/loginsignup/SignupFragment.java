package com.phongbm.loginsignup;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
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
import android.widget.Toast;

import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.libs.SquareImageView;

public class SignupFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "SignupFragment";

    private static final int REQUEST_SIGNUP_FRAGMENT = 0;

    private View view;
    private EditText edtPhoneNumber, edtPassword, edtConfirmPassword, edtCode;
    private Button btnSignup;
    private CheckBox checkBoxAgree;
    private TextView login, txtLogo;
    private boolean isFillPhoneNumber, isFillPassword, isFillConfirmPassword, isCheckBoxChecked;
    private String countryCode;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_signup, container, false);
        this.initializeToolbar();
        this.initializeComponent();
        return view;
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        ((AppCompatActivity) this.getActivity()).setSupportActionBar(toolbar);
        this.getActivity().setTitle("SIGN UP");
    }

    private void initializeComponent() {
        txtLogo = (TextView) view.findViewById(R.id.txtLogo);
        txtLogo.setTypeface(Typeface.createFromAsset(this.getActivity().getAssets(), "fonts/AIRSTREA.TTF"));
        btnSignup = (Button) view.findViewById(R.id.btnSignup);
        btnSignup.setOnClickListener(this);
        login = (TextView) view.findViewById(R.id.login);
        login.setOnClickListener(this);
        edtPhoneNumber = (EditText) view.findViewById(R.id.edtPhoneNumber);
        edtPassword = (EditText) view.findViewById(R.id.edtPassword);
        edtConfirmPassword = (EditText) view.findViewById(R.id.edtConfirmPassword);
        checkBoxAgree = (CheckBox) view.findViewById(R.id.checkBoxAgree);
        checkBoxAgree.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (checkBoxAgree.isChecked()) {
                    isCheckBoxChecked = true;
                    enabledButtonSignup();
                } else {
                    isCheckBoxChecked = false;
                    btnSignup.setEnabled(false);
                }
            }
        });
        edtPhoneNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 0) {
                    isFillPhoneNumber = true;
                    enabledButtonSignup();
                } else {
                    isFillPhoneNumber = false;
                    btnSignup.setEnabled(false);
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
                    enabledButtonSignup();
                } else {
                    isFillPassword = false;
                    btnSignup.setEnabled(false);
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
                if (s != null && s.length() > 0) {
                    isFillConfirmPassword = true;
                    enabledButtonSignup();
                } else {
                    isFillConfirmPassword = false;
                    btnSignup.setEnabled(false);
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

    private void enabledButtonSignup() {
        if (isFillPhoneNumber && isFillPassword && isFillConfirmPassword && isCheckBoxChecked
                && edtPassword.getText().toString().equals(edtConfirmPassword.getText().toString())) {
            btnSignup.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSignup:
                final ProgressDialog progressDialog = new ProgressDialog(this.getActivity());
                progressDialog.setTitle("Signing up");
                progressDialog.setMessage("Please wait...");
                progressDialog.setCanceledOnTouchOutside(false);
                progressDialog.show();

                final ParseUser newUser = new ParseUser();
                String phoneNumber = edtPhoneNumber.getText().toString().trim();
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
                            ((MainFragment) SignupFragment.this.getActivity())
                                    .showProfileInfomationFragment();
                            ((GlobalApplication)getActivity().getApplication()).addIdUser(newUser.getObjectId());
                            progressDialog.dismiss();
                            Toast.makeText(SignupFragment.this.getActivity(),
                                    "Registered successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            progressDialog.dismiss();
                            Toast.makeText(SignupFragment.this.getActivity(),
                                    "There was an error signing up", Toast.LENGTH_LONG).show();
                        }
                    }
                });
                break;
            case R.id.login:
                ((MainFragment) this.getActivity()).showLoginFragment();
                break;
            case R.id.edtCode:
                Intent intent = new Intent(this.getActivity(), CountryCodeActivity.class);
                startActivityForResult(intent, REQUEST_SIGNUP_FRAGMENT);
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_SIGNUP_FRAGMENT && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(CommonValue.COUNTRY_CODE);
                countryCode = content.substring(content.indexOf("(+"));
                edtCode.setText(content);
            }
        }
    }

}