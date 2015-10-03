package com.phongbm.ahihi;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.parse.GetCallback;
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
import com.phongbm.loginsignup.MainFragment;
import com.phongbm.settings.MyAccountActivity;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

@SuppressWarnings("ALL")
public class MainActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int REQUEST_ADDITION_FRIEND = 0;


    private Toolbar toolbar;
    private DrawerLayout drawerLayout;
    private NavigationView navigation;
    private ViewPagerAdapter viewPagerAdapter;
    private ViewPager viewPager;
    private TabLayout tab;
    private FriendItem newFriend;
    private Bitmap userAvatar;
    private CircleImageView imgAvatar;
    private FloatingActionButton btnAction;
    private ParseUser currentUser;
    private CallLogsDBManager callLogsDBManager;
    private CoordinatorLayout coordinator;
    private BroadcastMain broadcastMain;

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        return networkInfo == null ? false : true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentUser = ParseUser.getCurrentUser();
        CommonMethod.getInstance().loadListFriend(currentUser, this);
        registerBroastCastMain();
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                progressDialog.dismiss();
            }
        }, 3000);

        this.setContentView(R.layout.activity_main);
        this.initializeToolbar();
        this.initializeComponent();
        this.initializeProfileInformation();
        this.startService();
        callLogsDBManager = new CallLogsDBManager(this);
    }

    private void initializeToolbar() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setTitle(R.string.app_name);
    }

    private void initializeComponent() {
        coordinator = (CoordinatorLayout) findViewById(R.id.coordinator);
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

        viewPagerAdapter = new ViewPagerAdapter(this, this.getSupportFragmentManager(),
                (ViewGroup) findViewById(android.R.id.content));
        viewPager = (ViewPager) findViewById(R.id.viewPager);
        viewPager.setAdapter(viewPagerAdapter);

        int[] tabBackgroundIds = new int[]{R.drawable.bg_tab_message, R.drawable.bg_tab_contact,
                R.drawable.bg_tab_friend, R.drawable.bg_tab_setting};
        tab = (TabLayout) findViewById(R.id.tab);
        tab.setupWithViewPager(viewPager);
        for (int i = 0; i < viewPagerAdapter.getCount(); i++) {
            tab.getTabAt(i).setText(null);
            tab.getTabAt(i).setIcon(tabBackgroundIds[i]);
        }

        btnAction = (FloatingActionButton) findViewById(R.id.btnAction);
        btnAction.setOnClickListener(this);
    }

    private void initializeProfileInformation() {
        imgAvatar = (CircleImageView) findViewById(R.id.imgAvatar);
        imgAvatar.setOnClickListener(this);
        final TextView txtName = (TextView) findViewById(R.id.txtName);
        final TextView txtEmail = (TextView) findViewById(R.id.txtEmail);

        if (((GlobalApplication) getApplication()).getAvatar() != null) {
            imgAvatar.setImageBitmap(((GlobalApplication) getApplication()).getAvatar());
            txtName.setText(((GlobalApplication) getApplication()).getFullName());
            txtEmail.setText(((GlobalApplication) getApplication()).getEmail());
            Log.i(TAG, "Get Profile Information from GlobalApplication");
            return;
        }

        Log.i(TAG, "Get Profile Information from Server");
        ParseFile parseFile = (ParseFile) currentUser.get("avatar");
        if (parseFile != null) {
            parseFile.getDataInBackground(new GetDataCallback() {
                @Override
                public void done(byte[] bytes, ParseException e) {
                    if (e == null) {
                        String fullName = currentUser.getString("fullName");
                        String email = currentUser.getEmail();

                        userAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imgAvatar.setImageBitmap(userAvatar);
                        txtName.setText(fullName);
                        txtEmail.setText(email);

                        ((GlobalApplication) getApplication()).setAvatar(userAvatar);
                        ((GlobalApplication) getApplication()).setFullName(fullName);
                        ((GlobalApplication) getApplication()).setPhoneNumber(currentUser.getUsername());
                        ((GlobalApplication) getApplication()).setEmail(email);
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
        /*if (menuItem.isChecked()) {
            menuItem.setChecked(false);
        } else {
            menuItem.setChecked(true);
        }*/
        menuItem.setChecked(true);
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

                                ((GlobalApplication) getApplication()).setAvatar(null);
                                ((GlobalApplication) getApplication()).setFullName(null);
                                ((GlobalApplication) getApplication()).setPhoneNumber(null);
                                ((GlobalApplication) getApplication()).setEmail(null);
                                ((GlobalApplication) getApplication()).setPictureSend(null);
                                ((GlobalApplication) getApplication()).setAllFriendItems(null);
                                ((GlobalApplication) getApplication()).setActiveFriendItems(null);

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
            case R.id.nav_about_us:
                Intent intentAboutUs = new Intent(this, AboutUsActivity.class);
                this.startActivity(intentAboutUs);
                break;
            case R.id.nav_settings:
                drawerLayout.closeDrawers();
                viewPager.setCurrentItem(3);
                break;
            case R.id.nav_my_account:
                Intent intentMyAccount = new Intent(this, MyAccountActivity.class);
                this.startActivity(intentMyAccount);
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
                Intent intentAdditionFriend = new Intent(MainActivity.this, AdditionFriend.class);
                MainActivity.this.startActivityForResult(intentAdditionFriend, REQUEST_ADDITION_FRIEND);
                break;
        }
        return super.onOptionsItemSelected(item);
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
                        e.printStackTrace();
                        return;
                    }
                    final Bitmap avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    final String id = parseUser.getObjectId();
                    newFriend = new FriendItem(id, avatar, parseUser.getUsername(),
                            parseUser.getString("fullName"));
                    Intent intentAddFriend = new Intent();
                    intentAddFriend.setAction(CommonValue.ACTION_ADD_FRIEND);
                    boolean isOnline = parseUser.getBoolean("isOnline");
                    intentAddFriend.putExtra("isOnline", isOnline);
                    MainActivity.this.sendBroadcast(intentAddFriend);
                }
            });
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.imgAvatar:
                Intent intentProfile = new Intent(this, DetailActivity.class);
                intentProfile.putExtra(CommonValue.USER_ID, currentUser.getObjectId());
                this.startActivity(intentProfile);
                break;
            case R.id.btnAction:
                Intent intentNewMessage = new Intent(this, NewMessageActivity.class);
                this.startActivity(intentNewMessage);
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_ADDITION_FRIEND:
                    if (data == null) {
                        return;
                    }
                    String phoneNumber = data.getStringExtra("PHONE_NUMBER");
                    if (phoneNumber.equals(((GlobalApplication) this.getApplication())
                            .getPhoneNumber())) {
                        Snackbar.make(coordinator, "You can not make friends with yourself",
                                Snackbar.LENGTH_LONG)
                                .setAction("ACTION", null)
                                .show();
                        return;
                    }
                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setTitle("Addition Friend");
                    progressDialog.setMessage("Please wait...");
                    progressDialog.setCanceledOnTouchOutside(false);
                    progressDialog.show();

                    final ParseUser currentUser = ParseUser.getCurrentUser();
                    ParseQuery<ParseUser> query = ParseUser.getQuery();
                    query.whereEqualTo("username", phoneNumber);
                    query.getFirstInBackground(new GetCallback<ParseUser>() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            if (e != null) {
                                e.printStackTrace();
                                progressDialog.dismiss();
                                Snackbar.make(coordinator, "Error",
                                        Snackbar.LENGTH_LONG)
                                        .setAction("ACTION", null)
                                        .show();
                                return;
                            }
                            if (parseUser == null) {
                                progressDialog.dismiss();
                                Snackbar.make(coordinator, "That account does not exist",
                                        Snackbar.LENGTH_LONG)
                                        .setAction("ACTION", null)
                                        .show();
                                return;
                            }
                            ArrayList<String> listFriend = (ArrayList<String>) currentUser.get("listFriend");
                            String newUserId = parseUser.getObjectId();
                            if (listFriend == null) {
                                listFriend = new ArrayList<String>();
                            } else {
                                if (listFriend.contains(newUserId)) {
                                    progressDialog.dismiss();
                                    Snackbar.make(coordinator, "That account has been identical",
                                            Snackbar.LENGTH_LONG)
                                            .setAction("ACTION", null)
                                            .show();
                                    return;
                                }
                            }
                            listFriend.add(newUserId);
                            currentUser.put("listFriend", listFriend);
                            currentUser.saveInBackground();
                            MainActivity.this.createNewFriend(parseUser);
                            progressDialog.dismiss();
                            Snackbar snackbar = Snackbar.make(coordinator, "Addition friend successfully",
                                    Snackbar.LENGTH_LONG)
                                    .setAction("ACTION", null);
                            View snackbarView = snackbar.getView();
                            snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
                            snackbar.show();
                        }
                    });
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawers();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (currentUser != null) {
            currentUser.put("isOnline", false);
            currentUser.saveInBackground();
        }

        if (broadcastMain != null) {
            unregisterReceiver(broadcastMain);
            broadcastMain = null;
        }
        super.onDestroy();
    }

    @Override
    public void finish() {
        super.finish();
    }

    private void registerBroastCastMain() {
        if (broadcastMain == null) {
            broadcastMain = new BroadcastMain();
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("MAIN");
            registerReceiver(broadcastMain, intentFilter);
        }
    }

    private class BroadcastMain extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            imgAvatar.setImageBitmap(((GlobalApplication) getApplication()).getAvatar());
        }
    }

}