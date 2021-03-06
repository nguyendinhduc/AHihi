package com.phongbm.loginsignup;

import android.app.DatePickerDialog;
import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;

import com.phongbm.ahihi.R;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProfileInformationFragment extends Fragment implements View.OnClickListener {
    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" +
                    "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    private View view;
    private EditText edtBirthday, edtFirstName, edtLastName, edtEmail;
    private Button btnOK;
    private RadioButton radioMale, radioFemale;
    private boolean isFillFirstName, isFillLastName, isFillEmail;
    private Pattern pattern;
    private Matcher matcher;
    private boolean gender = true;

    public ProfileInformationFragment() {
        pattern = Pattern.compile(EMAIL_PATTERN);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_profile_infomation, null);
        this.initializeComponent();
        return view;
    }

    private void initializeComponent() {
        btnOK = (Button) view.findViewById(R.id.btnOK);
        btnOK.setOnClickListener(this);
        edtBirthday = (EditText) view.findViewById(R.id.edtBirthday);
        edtBirthday.setOnClickListener(this);
        edtFirstName = (EditText) view.findViewById(R.id.edtFirstName);
        edtLastName = (EditText) view.findViewById(R.id.edtLastName);
        edtEmail = (EditText) view.findViewById(R.id.edtEmail);
        edtFirstName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillFirstName = true;
                    ProfileInformationFragment.this.enabledButtonOK();
                } else {
                    isFillFirstName = false;
                    btnOK.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        edtLastName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillLastName = true;
                    ProfileInformationFragment.this.enabledButtonOK();
                } else {
                    isFillLastName = false;
                    btnOK.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });
        edtEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && s.length() > 1) {
                    isFillEmail = true;
                    ProfileInformationFragment.this.enabledButtonOK();
                } else {
                    isFillEmail = false;
                    btnOK.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!validate(s.toString())) {
                    edtEmail.setError("Email is not valid");
                } else {
                    edtEmail.setError(null);
                }
            }
        });
        radioMale = (RadioButton) view.findViewById(R.id.radioMale);
        radioFemale = (RadioButton) view.findViewById(R.id.radioFemale);
        radioMale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gender = true;
                    radioMale.setTextColor(Color.parseColor("#4caf50"));
                } else {
                    gender = false;
                    radioMale.setTextColor(Color.parseColor("#666666"));
                }
            }
        });
        radioFemale.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    gender = false;
                    radioFemale.setTextColor(Color.parseColor("#4caf50"));
                } else {
                    gender = true;
                    radioFemale.setTextColor(Color.parseColor("#666666"));
                }
            }
        });
    }

    private void enabledButtonOK() {
        if (isFillFirstName && isFillLastName && isFillEmail) {
            btnOK.setEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.edtBirthday:
                this.showDatePickerDialog();
                break;
            case R.id.btnOK:
                ((MainFragment) this.getActivity()).showProfilePictureFragment();
                break;
        }
    }

    public void showDatePickerDialog() {
        DatePickerDialog.OnDateSetListener onDateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                edtBirthday.setText((dayOfMonth < 10 ? "0" + dayOfMonth : dayOfMonth) + "/" +
                        ((monthOfYear + 1) < 10 ? "0" + (monthOfYear + 1) : (monthOfYear + 1)) + "/" +
                        year);
            }
        };
        String date = edtBirthday.getText().toString();
        String dates[] = date.split("/");
        int day = Integer.parseInt(dates[0]);
        int month = Integer.parseInt(dates[1]) - 1;
        int year = Integer.parseInt(dates[2]);
        DatePickerDialog datePickerDialog = new DatePickerDialog(this.getActivity(),
                onDateSetListener, year, month, day);
        datePickerDialog.show();
    }

    public String getFullName() {
        return edtFirstName.getText().toString().trim() + " "
                + edtLastName.getText().toString().trim();
    }

    public String getEmail() {
        return edtEmail.getText().toString();
    }

    public String getBirthday() {
        return edtBirthday.getText().toString();
    }

    public boolean getSex() {
        return gender;
    }

    public boolean validate(final String hex) {
        matcher = pattern.matcher(hex);
        return matcher.matches();
    }

}