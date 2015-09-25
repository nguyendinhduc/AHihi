package com.phongbm.ahihi;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.image.ImageActivity;
import com.phongbm.libs.SquareImageView;

@SuppressWarnings("ConstantConditions")
public class DetailActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "DetailActivity";
    private static final int REQUEST_UPLOAD_PHOTO = 10;

    private CollapsingToolbarLayout collapsingToolbar;
    private SquareImageView imgAvatar;
    private TextView txtFullName, txtPhoneNumber, txtEmail, txtBirthday, txtSex;
    private String id;
    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_detail);
        this.initializeComponent();
        this.loadInformation();
    }

    private void initializeComponent() {
        this.setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsingToolbar);
        collapsingToolbar.setTitle("Profile Information");
        collapsingToolbar.setExpandedTitleColor(ContextCompat.getColor(this,
                android.R.color.transparent));

        imgAvatar = (SquareImageView) findViewById(R.id.imgAvatar);
        txtFullName = (TextView) findViewById(R.id.txtFullName);
        txtPhoneNumber = (TextView) findViewById(R.id.txtPhoneNumber);
        txtEmail = (TextView) findViewById(R.id.txtEmail);
        txtBirthday = (TextView) findViewById(R.id.txtBirthday);
        txtSex = (TextView) findViewById(R.id.txtSex);
    }

    private void loadInformation() {
        final ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading...");
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();

        id = this.getIntent().getStringExtra(CommonValue.USER_ID);
        final ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.getInBackground(id, new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                if (e != null) {
                    e.printStackTrace();
                }
                ParseFile parseFile = (ParseFile) parseUser.get("avatar");
                if (parseFile != null) {
                    parseFile.getDataInBackground(new GetDataCallback() {
                        @Override
                        public void done(byte[] bytes, ParseException e) {
                            if (e == null) {
                                imgAvatar.setImageBitmap(
                                        BitmapFactory.decodeByteArray(bytes, 0, bytes.length));
                            }
                        }
                    });
                } else {
                    imgAvatar.setImageResource(R.drawable.ic_avatar_default);
                }
                String fullName = parseUser.getString("fullName");
                collapsingToolbar.setTitle(fullName);
                txtFullName.setText(fullName);
                txtPhoneNumber.setText(parseUser.getUsername());
                txtEmail.setText(parseUser.getEmail());
                txtBirthday.setText(parseUser.getString("birthday"));
                boolean sex = parseUser.getBoolean("sex");
                txtSex.setText(sex ? "Male" : "Female");

                progressDialog.dismiss();
            }
        });

        currentUser = ParseUser.getCurrentUser();
        if (currentUser.getObjectId().equals(id)) {
            imgAvatar.setOnClickListener(this);
        } else {
            imgAvatar.setOnClickListener(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        Intent intentUpload = new Intent();
        intentUpload.setClass(this, ImageActivity.class);
        startActivityForResult(intentUpload, REQUEST_UPLOAD_PHOTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.i(TAG, "onActivityResult...");
        if (requestCode == REQUEST_UPLOAD_PHOTO && resultCode == Activity.RESULT_OK) {
            Log.i(TAG, "onActivityResult...");
            byte[] bytes = data.getByteArrayExtra(CommonValue.BYTE_AVATAR);
            Bitmap bitmapAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            ((GlobalApplication) DetailActivity.this.getApplication()).setAvatar(bitmapAvatar);
            imgAvatar.setImageBitmap(bitmapAvatar);
            Intent intent = new Intent();
            intent.setAction("MAIN");
            sendBroadcast(intent);
            CommonMethod.getInstance().uploadAvatar(currentUser, bitmapAvatar);
//            Snackbar snackbar = Snackbar.make(collapsingToolbar, "Successfully", Snackbar.LENGTH_LONG)
//                    .setAction("ACTION", null);
//            View snackbarView = snackbar.getView();
//            snackbarView.setBackgroundColor(Color.parseColor("#4caf50"));
//            snackbar.show();
        }
    }

}