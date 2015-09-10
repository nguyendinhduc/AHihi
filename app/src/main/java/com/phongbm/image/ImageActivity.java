package com.phongbm.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonValue;

public class ImageActivity extends AppCompatActivity implements AdapterView.OnItemClickListener {
    private static final String TAG = "ImageActivity";

    private GridView gridViewImage;
    private ImageAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image);
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
        String url = (String)parent.getItemAtPosition(position);
        if (!checkMinimumSize(url)) {
            Toast.makeText(this, "Picture too small", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent intent = new Intent(ImageActivity.this, ImageControl.class);
        intent.putExtra(ImageControl.EXTRA_IMAGE, url);
        this.startActivityForResult(intent, CommonValue.REQUECODE_SET_AVATAR);
        return;
    }

    private boolean checkMinimumSize(String url) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(url, options);
        int width = options.outWidth;
        int height = options.outHeight;
        if (width < 200 || height < 200) {
            return false;
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( requestCode == CommonValue.REQUECODE_SET_AVATAR ) {
            if ( resultCode == Activity.RESULT_OK)
                setResult(Activity.RESULT_OK, data);
            else setResult(Activity.RESULT_CANCELED);
            finish();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}