package com.phongbm.image;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

import java.io.ByteArrayOutputStream;

public class ImageControl extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ImageControl";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    private static final int SIZE_IMAGE = GlobalApplication.WIDTH_SCREEN;
    private static final int HEIGHT_SCREEN = GlobalApplication.HEIGHT_SCREEN;

    private String url;
    private int orientation;
    private RelativeLayout layoutImage;
    private TouchImageView image;
    private ImageView imageView;
    private float radius;
    private Bitmap bitmapMain;
    private CommonMethod commonMethod;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_image_control);
        url = getIntent().getStringExtra(ImageControl.EXTRA_IMAGE);
        commonMethod = CommonMethod.getInstance();
        Log.i(TAG, "url: " + url);
        this.initializeLayoutImage();
        this.initializeComponent();

    }

    private void initializeLayoutImage() {
        layoutImage = (RelativeLayout) findViewById(R.id.layoutImage);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)
                layoutImage.getLayoutParams();
        params.height = SIZE_IMAGE;
        params.width = SIZE_IMAGE;

        RelativeLayout.LayoutParams paramImage = (RelativeLayout.LayoutParams)
                findViewById(R.id.image).getLayoutParams();
        paramImage.height = SIZE_IMAGE;
        paramImage.width = SIZE_IMAGE;

        RelativeLayout.LayoutParams paramImageView = (RelativeLayout.LayoutParams)
                findViewById(R.id.imageView).getLayoutParams();
        paramImageView.height = SIZE_IMAGE;
        paramImageView.width = SIZE_IMAGE;
    }

    private void initializeComponent() {
        findViewById(R.id.btnCrop).setOnClickListener(this);
        findViewById(R.id.btnRotate).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        image = (TouchImageView) findViewById(R.id.image);
        image.setMaxZoom(5);
        this.createBitmapMain();
        this.cropFadeCircle(radius);
    }

    private void createBitmapMain() {
        int widthResize, heightResize;
        try {
            bitmapMain = BitmapFactory.decodeFile(url);
            heightResize = (int) ((float) SIZE_IMAGE / bitmapMain.getWidth() * bitmapMain.getHeight());
            widthResize = SIZE_IMAGE;
            if (heightResize > HEIGHT_SCREEN) {
                widthResize = (int) ((float) HEIGHT_SCREEN / heightResize * widthResize);
                heightResize = HEIGHT_SCREEN;
            }
            bitmapMain = Bitmap.createScaledBitmap(bitmapMain, widthResize, heightResize, true);
        } catch (OutOfMemoryError e) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, o);
            int width = o.outWidth;
            int height = o.outHeight;
            widthResize = width;
            heightResize = height;
            if (widthResize > SIZE_IMAGE) {
                heightResize = (int) ((float) SIZE_IMAGE / widthResize * heightResize);
                widthResize = SIZE_IMAGE;
            }
            if (heightResize > HEIGHT_SCREEN) {
                widthResize = (int) ((float) HEIGHT_SCREEN / heightResize * widthResize);
                heightResize = HEIGHT_SCREEN;
            }
            bitmapMain = commonMethod.decodeSampledBitmapFromResource(url, widthResize, heightResize);

        }
        orientation = CommonMethod.getInstance().getOrientation(url);
        bitmapMain = CommonMethod.getInstance().getBitmap(orientation, bitmapMain);
        image.setImageBitmap(bitmapMain);
        bitmapMain = ((BitmapDrawable) image.getDrawable()).getBitmap();
        imageView = (ImageView) findViewById(R.id.imageView);
        radius = (float) SIZE_IMAGE / 2;
    }

    private void cropFadeCircle(float radius) {
        Bitmap bitmapFade = Bitmap.createBitmap((int) radius * 2,
                (int) radius * 2, Bitmap.Config.ARGB_8888);
        Canvas canvasBitmapFade = new Canvas(bitmapFade);
        canvasBitmapFade.drawColor(Color.parseColor("#AF000000"));
        Paint eraser = new Paint();
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setAntiAlias(true);
        canvasBitmapFade.drawCircle((int) radius,
                (int) radius, radius, eraser);
        imageView.setImageBitmap(bitmapFade);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnCrop:
                this.cropImage();
                break;
            case R.id.btnRotate:
                this.rotateBMMain();
                break;
            case R.id.btnCancel:
                this.setResult(Activity.RESULT_CANCELED);
                this.finish();
                break;
        }
    }

    private void rotateBMMain() {
        image.resetZoom();
        bitmapMain = CommonMethod.getInstance().getBitmap(90, bitmapMain);
        image.setImageBitmap(bitmapMain);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }

    private void cropImage() {
        RectF rect = image.getZoomedRect();

        float leftReal = (rect.left * bitmapMain.getWidth());
        float topReal = (rect.top * bitmapMain.getHeight());
        float rightReal = rect.right * bitmapMain.getWidth();
        float bottomReal = rect.bottom * bitmapMain.getHeight();

        if (bitmapMain.getWidth() < bitmapMain.getHeight())
            radius = (float) bitmapMain.getWidth() / 2;
        else radius = (float) bitmapMain.getHeight() / 2;

        Bitmap bmCrop = Bitmap.createBitmap((int) (rightReal - leftReal),
                (int) (bottomReal - topReal), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmCrop);

        canvas.drawBitmap(bitmapMain, new Rect((int) leftReal, (int) topReal,
                        (int) (rightReal), (int) (bottomReal)),
                new Rect(0, 0, bmCrop.getWidth(), bmCrop.getHeight()), null);

        bmCrop = Bitmap.createScaledBitmap(bmCrop, 300, 300, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmCrop.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        Intent intent = new Intent();
        intent.putExtra(CommonValue.BYTE_AVATAR, bytes);
        this.setResult(Activity.RESULT_OK, intent);
        this.finish();
    }

    @Override
    protected void onDestroy() {
        if (bitmapMain != null) {
            bitmapMain.recycle();
            bitmapMain = null;
        }
        super.onDestroy();
    }

}