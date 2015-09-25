package com.phongbm.settings;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.parse.ParseUser;
import com.phongbm.ahihi.R;
import com.phongbm.call.CallLogsDBManager;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.loginsignup.MainFragment;

import java.util.ArrayList;

public class MyAccountActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private Toolbar toolbar;
    private ListView listViewOption;
    private ArrayList<OptionItem> optionItems;
    private OptionAdapter optionAdapter;
    private CallLogsDBManager callLogsDBManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_my_account);
        initializeToolbar();
        this.initializeComponent();

        callLogsDBManager = new CallLogsDBManager(MyAccountActivity.this);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setTitle(R.string.activity_my_account);
    }

    private void initializeComponent() {
        listViewOption = (ListView) findViewById(R.id.listViewOption);
        listViewOption.setOnItemClickListener(this);

        optionItems = new ArrayList<>();
        optionItems.add(new OptionItem("Change Password", "Change your password"));
        optionItems.add(new OptionItem("Delete Account", "Permanently delete your account and data"));
        optionItems.add(new OptionItem("Change Account", "Log in to another account"));

        optionAdapter = new OptionAdapter();
        listViewOption.setAdapter(optionAdapter);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (position) {
            case 0:
                break;
            case 1:
                break;
            case 2:
                final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
                alertDialog.setTitle("Confirm");
                alertDialog.setMessage("Log out?");
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.setCancelable(false);
                alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "No",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                alertDialog.dismiss();
                            }
                        });
                alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, "Yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                ParseUser parseUser = ParseUser.getCurrentUser();
                                parseUser.put("isOnline", false);
                                parseUser.saveInBackground();
                                ParseUser.logOut();
                                callLogsDBManager.deleteAllData();

                                ((GlobalApplication) getApplication()).setAvatar(null);
                                ((GlobalApplication) getApplication()).setFullName(null);
                                ((GlobalApplication) getApplication()).setPhoneNumber(null);
                                ((GlobalApplication) getApplication()).setEmail(null);
                                ((GlobalApplication) getApplication()).setPictureSend(null);
                                ((GlobalApplication) getApplication()).setAllFriendItems(null);
                                ((GlobalApplication) getApplication()).setActiveFriendItems(null);

                                Intent intentLogout = new Intent(CommonValue.ACTION_LOGOUT);
                                MyAccountActivity.this.sendBroadcast(intentLogout);
                                Intent intent = new Intent(MyAccountActivity.this, MainFragment.class);
                                alertDialog.dismiss();
                                MyAccountActivity.this.startActivity(intent);
                                MyAccountActivity.this.finish();
                            }
                        });
                alertDialog.show();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                MyAccountActivity.this.finish();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    private class OptionItem {
        String option, description;

        public OptionItem(String option, String description) {
            this.option = option;
            this.description = description;
        }
    }

    private class OptionAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return optionItems.size();
        }

        @Override
        public OptionItem getItem(int position) {
            return optionItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = MyAccountActivity.this.getLayoutInflater()
                        .inflate(R.layout.item_my_account, parent, false);
                viewHolder = new ViewHolder();
                viewHolder.txtOption = (TextView) convertView.findViewById(R.id.txtOption);
                viewHolder.txtDescription = (TextView) convertView.findViewById(R.id.txtDescription);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.txtOption.setText(optionItems.get(position).option);
            viewHolder.txtDescription.setText(optionItems.get(position).description);
            return convertView;
        }
    }

    private class ViewHolder {
        TextView txtOption, txtDescription;
    }

    @Override
    public void finish() {
        callLogsDBManager.closeDatabase();
        super.finish();
    }

}