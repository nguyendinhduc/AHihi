package com.phongbm.loginsignup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.design.widget.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.parse.ParseUser;
import com.phongbm.ahihi.MainActivity;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.common.GlobalApplication;
import com.phongbm.image.ImageActivity;
import com.phongbm.image.ImageControl;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePictureFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "ProfilePictureFragment";
    private static final int REQUEST_TAKE_PHOTO = 0;
    private static final int REQUEST_UPLOAD_PHOTO = 1;

    private View view;
    private CircleImageView imgAvatar;
    private Bitmap bitmapAvatar, bitmapDefault;
    private Uri capturedImageURI;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bitmapDefault = BitmapFactory.decodeResource(this.getResources(), R.drawable.ic_avatar_default);
        view = inflater.inflate(R.layout.fragment_profile_picture, null);
        this.initializeComponent();
        return view;
    }

    private void initializeComponent() {
        imgAvatar = (CircleImageView) view.findViewById(R.id.imgAvatar);
        bitmapAvatar = ((BitmapDrawable) imgAvatar.getDrawable()).getBitmap();
        view.findViewById(R.id.layoutUploadPhoto).setOnClickListener(this);
        view.findViewById(R.id.layoutTakePhoto).setOnClickListener(this);
        view.findViewById(R.id.btnSkip).setOnClickListener(this);
        view.findViewById(R.id.btnOK).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnSkip:
                this.signUpSuccess(bitmapDefault);
                break;
            case R.id.btnOK:
                this.signUpSuccess(bitmapAvatar);
                break;
            case R.id.layoutUploadPhoto:
                Intent intentUpload = new Intent();
                intentUpload.setClass(this.getActivity(), ImageActivity.class);
                this.startActivityForResult(intentUpload, REQUEST_UPLOAD_PHOTO);
                break;
            case R.id.layoutTakePhoto:
                Intent intentTakePhoto = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intentTakePhoto.resolveActivity(getActivity().getPackageManager()) != null) {
                    this.startActivityForResult(intentTakePhoto, REQUEST_TAKE_PHOTO);
                } else {
                    Snackbar.make(view, "Device does not support camera", Snackbar.LENGTH_LONG)
                            .setAction("ACTION", null)
                            .show();
                }
                break;
        }
    }

    private void signUpSuccess(final Bitmap avatar) {
        MainFragment mainFragment = (MainFragment) this.getActivity();
        final String phoneNumber = mainFragment.getSignUpFragment().getPhoneNumber();
        final String fullName = mainFragment.getProfileInfomationFragment().getFullName();
        final String email = mainFragment.getProfileInfomationFragment().getEmail();
        String birthday = mainFragment.getProfileInfomationFragment().getBirthday();
        boolean sex = mainFragment.getProfileInfomationFragment().getSex();

        ParseUser currentUser = ParseUser.getCurrentUser();
        currentUser.put("fullName", fullName);
        currentUser.setEmail(email);
        currentUser.put("birthday", birthday);
        currentUser.put("sex", sex);
        currentUser.saveInBackground();
        CommonMethod.uploadAvatar(currentUser, avatar);

        new Handler().post(new Runnable() {
            @Override
            public void run() {
                GlobalApplication globalApplication = (GlobalApplication) ProfilePictureFragment.this
                        .getActivity().getApplication();
                globalApplication.setAvatar(avatar);
                globalApplication.setFullName(fullName);
                globalApplication.setPhoneNumber(phoneNumber);
                globalApplication.setEmail(email);
            }
        });

        Intent intent = new Intent(mainFragment, MainActivity.class);
        mainFragment.startActivity(intent);
        mainFragment.finish();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_UPLOAD_PHOTO:
                    byte[] bytes = data.getByteArrayExtra(CommonValue.BYTE_AVATAR);
                    bitmapAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgAvatar.setImageBitmap(bitmapAvatar);
                    break;
                case REQUEST_TAKE_PHOTO:
                    String capturedImageFilePath = CommonMethod.getInstance().getPathFromUri(getActivity()
                            .getBaseContext(), data.getData());
                    Intent intentCropImage = new Intent();
                    intentCropImage.setClass(getActivity(), ImageControl.class);
                    intentCropImage.putExtra(ImageControl.EXTRA_IMAGE, capturedImageFilePath);
                    this.startActivityForResult(intentCropImage, REQUEST_UPLOAD_PHOTO);
                    break;
            }
            view.findViewById(R.id.btnOK).setEnabled(true);
        }
    }

}