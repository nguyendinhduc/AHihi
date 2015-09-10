package com.phongbm.ahihi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.parse.FindCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.call.CallLogActivity;
import com.phongbm.call.CallLogsDBManager;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.image.ImageActivity;
import com.phongbm.loginsignup.MainFragment;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";

    private GlobalApplication globalApplication;
    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigation;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tab;
    private InputMethodManager inputMethodManager;
    private FriendItem newFriend;
    private Bitmap userAvatar;
    private CircleImageView imgAvatar;
    private FloatingActionButton btnAction;
    private CallLogsDBManager callLogsDBManager;

    // private MessagesLogDBManager messagesLogDBManager;

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? false : true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);
        globalApplication = (GlobalApplication) this.getApplicationContext();
        this.initializeToolbar();
        this.initializeComponent();
        this.initializeProfileInformation();

        this.startService();
        callLogsDBManager = new CallLogsDBManager(this);
        // messagesLogDBManager = new MessagesLogDBManager(this);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle("Messages");
    }

    private void initializeComponent() {
        inputMethodManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.open_navigation_drawer, R.string.close_navigation_drawer) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        drawerLayout.setDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        navigation = (NavigationView) findViewById(R.id.navigation);
        navigation.setNavigationItemSelectedListener(this);

        viewPagerAdapter = new ViewPagerAdapter(this, this.getSupportFragmentManager(), (ViewGroup) findViewById(android.R.id.content));
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                MainActivity.this.getSupportActionBar().setTitle(viewPagerAdapter.getPageTitle(position));
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        int[] tabBackgroundIds = new int[]{R.drawable.bg_tab_message, R.drawable.bg_tab_contact,
                R.drawable.bg_tab_friend, R.drawable.bg_tab_info};
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(viewPager);
        for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
            tab.getTabAt(i).setText(null);
            tab.getTabAt(i).setIcon(tabBackgroundIds[i]);
        }

        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(this);

        btnAction = (FloatingActionButton) findViewById(R.id.btnAction);
        btnAction.setOnClickListener(this);
    }

    private void initializeProfileInformation() {
        ParseUser currentUser = ParseUser.getCurrentUser();
        View header = navigation.getChildAt(0);
        TextView txtName = (TextView) header.findViewById(R.id.txtName);
        txtName.setText((String) currentUser.get("fullName"));
        TextView txtEmail = (TextView) header.findViewById(R.id.txtEmail);
        txtEmail.setText(currentUser.getEmail());

        ParseFile parseFile = (ParseFile) currentUser.get("avatar");
        if (parseFile != null) {
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        userAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgAvatar.setImageBitmap(userAvatar);
                        globalApplication.setAvatar(userAvatar);
                    }
                }
            });
        }
    }

    private void startService() {
        Intent intentStartService = new Intent();
        intentStartService.setClassName(CommonValue.PACKAGE_NAME_MAIN,
                CommonValue.PACKAGE_NAME_COMMON + ".AHihiService");
        this.startService(intentStartService);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        if (menuItem.isChecked()) {
            menuItem.setChecked(false);
        } else {
            menuItem.setChecked(true);
        }
        switch (menuItem.getItemId()) {
            case R.id.nav_call_logs:
                Intent intentCallLogs = new Intent(MainActivity.this, CallLogActivity.class);
                MainActivity.this.startActivity(intentCallLogs);
                break;
            case R.id.nav_log_out:
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
                                callLogsDBManager.closeDatabase();
                                Intent intentLogout = new Intent(CommonValue.ACTION_LOGOUT);
                                MainActivity.this.sendBroadcast(intentLogout);
                                Intent intent = new Intent(MainActivity.this, MainFragment.class);
                                alertDialog.dismiss();
                                MainActivity.this.startActivity(intent);
                                MainActivity.this.finish();
                            }
                        });
                alertDialog.show();
                break;
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.action_add_user:
                inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                AddFriendDialog addFriendDialog = new AddFriendDialog();
                addFriendDialog.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        super.finish();
    }

    @Override
    protected void onDestroy() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            parseUser.put("isOnline", false);
            parseUser.saveInBackground();
        }

        super.onDestroy();
    }

    public FriendItem getNewFriend() {
        return newFriend;
    }

    private void createNewFriend(final ParseUser parseUser) {
        final ParseFile parseFile = (ParseFile) parseUser.get("avatar");
        if (parseFile != null) {
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e != null) {
                        return;
                    }
                    Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    newFriend = new FriendItem(parseUser.getObjectId(), avatar,
                            parseUser.getUsername(), parseUser.getString("fullName"));
                    Intent intentAddFriend = new Intent();
                    intentAddFriend.setAction(CommonValue.ACTION_ADD_FRIEND);
                    boolean isOnline = parseUser.getBoolean("isOnline");
                    intentAddFriend.putExtra("isOnline", isOnline);
                    sendBroadcast(intentAddFriend);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgAvatar:
                startActivitySetAvatar();
                break;
            case R.id.btnAction:
                Intent intent = new Intent(this, DetailActivity.class);
                this.startActivity(intent);
                break;
        }
    }

    private void startActivitySetAvatar() {
        Intent intentAccount = new Intent();
        intentAccount.setClass(MainActivity.this, ImageActivity.class);
        MainActivity.this.startActivityForResult(intentAccount, CommonValue.REQUECODE_SET_AVATAR);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CommonValue.REQUECODE_SET_AVATAR && resultCode == Activity.RESULT_OK) {
            byte[] bytes = data.getByteArrayExtra(CommonValue.BYTE_AVATAR);
            Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            CommonMethod.uploadAvatar(ParseUser.getCurrentUser(), avatar);
            imgAvatar.setImageBitmap(avatar);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private class AddFriendDialog extends Dialog implements android.view.View.OnClickListener {
        private EditText edtPhoneNumber;
        private Button btnAddFriend;

        public AddFriendDialog() {
            super(MainActivity.this);
            this.requestWindowFeature(Window.FEATURE_NO_TITLE);
            this.setContentView(R.layout.dialog_addfriend);
            this.initializeComponent();
        }

        private void initializeComponent() {
            edtPhoneNumber = (EditText) findViewById(R.id.edtPhoneNumber);
            btnAddFriend = (Button) findViewById(R.id.btnAddFriend);
            btnAddFriend.setOnClickListener(this);
            edtPhoneNumber.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (s != null && s.length() > 0) {
                        btnAddFriend.setEnabled(true);
                    } else {
                        btnAddFriend.setEnabled(false);
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                }
            });
        }

        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnAddFriend:
                    String phoneNumber = edtPhoneNumber.getText().toString().trim();
                    final ParseUser currentUser = ParseUser.getCurrentUser();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", phoneNumber);
                    query.findInBackground(new FindCallback<ParseUser>() {
                        @Override
                        public void done(List<ParseUser> list, ParseException e) {
                            if (e == null) {
                                if (list.size() == 0) {
                                    Toast.makeText(MainActivity.this, "That account does not exist",
                                            Toast.LENGTH_SHORT).show();
                                    AddFriendDialog.this.dismiss();
                                    return;
                                }
                                ArrayList<String> listFriend = (ArrayList<String>)
                                        currentUser.get("listFriend");
                                if (listFriend == null)
                                    listFriend = new ArrayList<String>();
                                listFriend.add(list.get(0).getObjectId());
                                currentUser.put("listFriend", listFriend);
                                currentUser.saveInBackground();
                                createNewFriend(list.get(0));
                                AddFriendDialog.this.dismiss();
                            }
                        }
                    });
                    break;
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

}