package com.phongbm.message;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;

import com.phongbm.ahihi.R;
import com.squareup.picasso.Picasso;

public class PictureActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE = "PictureActivity_Image";
    private ImageView imgPicture;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_picture);
        this.initializeToolbar();
        imgPicture = (ImageView) findViewById(R.id.imgPicture);
        ViewCompat.setTransitionName(imgPicture, EXTRA_IMAGE);
        Picasso.with(this).load(getIntent().getStringExtra(EXTRA_IMAGE)).into(imgPicture);
    }

    @SuppressWarnings("ConstantConditions")
    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public static void launch(AppCompatActivity activity, View transitionView, String url) {
        ActivityOptionsCompat options = ActivityOptionsCompat.makeSceneTransitionAnimation(
                activity, transitionView, EXTRA_IMAGE);
        Intent intent = new Intent(activity, PictureActivity.class);
        intent.putExtra(EXTRA_IMAGE, url);
        ActivityCompat.startActivity(activity, intent, options.toBundle());
    }

}