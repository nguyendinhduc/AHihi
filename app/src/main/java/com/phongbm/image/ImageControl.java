package com.phongbm.image;

import android.app.Activity;
import android.content.Context;
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
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.Pair;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;

import java.io.ByteArrayOutputStream;


public class ImageControl extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "ImageControl";
    public static final String EXTRA_IMAGE = "EXTRA_IMAGE";
    private String url;
    private int orientation;

    private static final float SIZE_IMAGE = GlobalApplication.WIDTH_SCREEN;

    private RelativeLayout layoutImage;
    private TouchImageView image;
    private ImageView imageView;

    private float radius;

    private Bitmap bmMain;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_control);
        url = getIntent().getStringExtra(ImageControl.EXTRA_IMAGE);
        initLayoutImage();
        intializeComponent();


    }

    private void initLayoutImage() {
        layoutImage = (RelativeLayout) findViewById(R.id.layoutImage);
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)layoutImage.getLayoutParams();
        params.height = (int)SIZE_IMAGE;
        params.width = (int)SIZE_IMAGE;

        RelativeLayout.LayoutParams paramImage = (RelativeLayout.LayoutParams)findViewById(R.id.img).getLayoutParams();
        paramImage.height = (int)SIZE_IMAGE;
        paramImage.width = (int)SIZE_IMAGE;

        RelativeLayout.LayoutParams paramImageView = (RelativeLayout.LayoutParams)findViewById(R.id.imageView).getLayoutParams();
        paramImageView.height = (int)SIZE_IMAGE;
        paramImageView.width = (int)SIZE_IMAGE;
    }

    private void intializeComponent() {
        findViewById(R.id.btnCrop).setOnClickListener(this);
        findViewById(R.id.btnRotate).setOnClickListener(this);
        findViewById(R.id.btnCancel).setOnClickListener(this);

        image = (TouchImageView) findViewById(R.id.img);
        image.setMaxZoom(5);
        createBmMain();
        cropFadeCirle(radius);
    }

    private void createBmMain() {
        int widthResize, heightResize;
        try {
            bmMain = BitmapFactory.decodeFile(url);
            heightResize = (int) ((float) GlobalApplication.WIDTH_SCREEN / bmMain.getWidth() * bmMain.getHeight());
            widthResize = GlobalApplication.WIDTH_SCREEN;
            if (heightResize > GlobalApplication.HEIGHT_SCREEN) {
                widthResize = (int) ((float) GlobalApplication.HEIGHT_SCREEN / heightResize * widthResize);
                heightResize = GlobalApplication.HEIGHT_SCREEN;
            }
            bmMain = Bitmap.createScaledBitmap(bmMain, widthResize, heightResize, true);

        } catch (OutOfMemoryError e) {
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(url, o);
            int width = o.outWidth;
            int height = o.outHeight;
            widthResize = width;
            heightResize = height;
            if (widthResize > GlobalApplication.WIDTH_SCREEN) {
                heightResize = (int) ((float) GlobalApplication.WIDTH_SCREEN
                        / widthResize * heightResize);
                widthResize = GlobalApplication.WIDTH_SCREEN;
            }
            if (heightResize > GlobalApplication.HEIGHT_SCREEN) {
                widthResize = (int) ((float) GlobalApplication.HEIGHT_SCREEN
                        / heightResize * widthResize);
                heightResize = GlobalApplication.HEIGHT_SCREEN;
            }

            bmMain = decodeSampledBitmapFromResource(url, widthResize, heightResize);

        }
        orientation = CommonMethod.getInstance().getOrientation(url);
        bmMain = CommonMethod.getInstance().getBitmap(orientation, bmMain);
        image.setImageBitmap(bmMain);
        bmMain = ((BitmapDrawable)image.getDrawable()).getBitmap();
        imageView = (ImageView) findViewById(R.id.imageView);
        radius = (float)GlobalApplication.WIDTH_SCREEN/2;
    }

    public static int calculateInSampleSize(BitmapFactory.Options options,
                                            int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize * 2;
    }


    public static Bitmap decodeSampledBitmapFromResource(String uri,
                                                         int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(uri, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(uri, options);
    }


    private void cropFadeCirle(float radius) {
        Bitmap bitmapFade = Bitmap.createBitmap((int)radius*2,
                (int)radius*2, Bitmap.Config.ARGB_8888);
        Canvas canvasbitmapFade = new Canvas(bitmapFade);
        canvasbitmapFade.drawColor(Color.parseColor("#AF000000"));
        Paint eraser = new Paint();
        eraser.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        eraser.setAntiAlias(true);
        canvasbitmapFade.drawCircle((int)radius,
                (int)radius, radius, eraser);
        imageView.setImageBitmap(bitmapFade);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnCrop:
                cropimage();
                break;
            case R.id.btnRotate:
                rotateBMMain();
                break;
            case R.id.btnCancel:
                setResult(Activity.RESULT_CANCELED);
                finish();
                break;
        }
    }
    private void rotateBMMain() {
        image.resetZoom();
        bmMain = CommonMethod.getInstance().getBitmap(90, bmMain);
        image.setImageBitmap(bmMain);
        image.setScaleType(ImageView.ScaleType.CENTER_CROP);
    }
    private void cropimage() {
        RectF rect = image.getZoomedRect();

        float leftReal = (rect.left * bmMain.getWidth());
        float topReal = (rect.top * bmMain.getHeight());
        float rightReal = rect.right * bmMain.getWidth();
        float bottomReal = rect.bottom * bmMain.getHeight();

        if ( bmMain.getWidth() < bmMain.getHeight() ) radius = (float)bmMain.getWidth()/2;
        else radius = (float)bmMain.getHeight()/2;


        Bitmap bmCrop = Bitmap.createBitmap((int) (rightReal - leftReal), (int) (bottomReal - topReal), Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bmCrop);

        canvas.drawBitmap(bmMain, new Rect((int) leftReal, (int) topReal, (int) (rightReal), (int) (bottomReal)),
                new Rect(0, 0, bmCrop.getWidth(), bmCrop.getHeight()), null);

        bmCrop = Bitmap.createScaledBitmap(bmCrop, 300, 300, true);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bmCrop.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream);
        byte[] bytes = byteArrayOutputStream.toByteArray();
        Intent intent = new Intent();
        intent.putExtra(CommonValue.BYTE_AVATAR, bytes);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
    @Override
    protected void onDestroy() {
        if (bmMain != null) {
            bmMain.recycle();
            bmMain = null;
        }
        super.onDestroy();
    }
}