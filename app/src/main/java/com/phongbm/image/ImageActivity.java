package com.phongbm.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;

import com.phongbm.ahihi.R;

public class ImageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ImageActivity";
    private static final int REQUEST_CROP_AVATAR = 0;

    private GridView gridViewImage;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image);
        this.initializeToolbar();
        this.initializeComponent();

        imageAdapter = new ImageAdapter(this);
        gridViewImage.setAdapter(imageAdapter);
    }

    private void initializeToolbar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(toolbar);
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initializeComponent() {
        gridViewImage = (GridView) findViewById(R.id.gridViewImage);
        gridViewImage.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String url = (String) parent.getItemAtPosition(position);
        if (checkMinimumSize(url)) {
            Snackbar.make(view, "Picture too small", Snackbar.LENGTH_LONG)
                    .setAction("ACTION", null)
                    .show();
            return;
        }
        Intent intent = new Intent(ImageActivity.this, ImageControl.class);
        intent.putExtra(ImageControl.EXTRA_IMAGE, url);
        this.startActivityForResult(intent, REQUEST_CROP_AVATAR);
    }

    private boolean checkMinimumSize(String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        int width = options.outWidth;
        int height = options.outHeight;
        return (width < 200 || height < 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent result) {
        if (requestCode == REQUEST_CROP_AVATAR && resultCode == Activity.RESULT_OK) {
            this.setResult(Activity.RESULT_OK, result);
            this.finish();
        }
    }

}