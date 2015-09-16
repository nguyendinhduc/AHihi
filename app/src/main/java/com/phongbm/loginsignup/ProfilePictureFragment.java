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
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;

import com.parse.ParseUser;
import com.phongbm.ahihi.MainActivity;
import com.phongbm.ahihi.R;
import com.phongbm.common.CommonMethod;
import com.phongbm.common.CommonValue;
import com.phongbm.image.ImageActivity;
import com.phongbm.image.ImageControl;
import com.phongbm.libs.SquareImageView;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;

public class ProfilePictureFragment extends Fragment implements View.OnClickListener{
    private static final String TAG = "ProfilePictureFragment";

    private View view;
    private CircleImageView imgAvatar;
//    private int[] avatarDefaultIDs = new int[]{R.drawable.ic_ava_1, R.drawable.ic_ava_2,
//            R.drawable.ic_ava_3, R.drawable.ic_ava_4, R.drawable.ic_ava_5, R.drawable.ic_ava_6,
//            R.drawable.ic_ava_7, R.drawable.ic_ava_8, R.drawable.ic_ava_9, R.drawable.ic_ava_10,
//            R.drawable.ic_ava_11, R.drawable.ic_ava_12, R.drawable.ic_avatar_default};
    private LayoutInflater layoutInflater;
    private Bitmap bitmapAvatar;
    private Bitmap bitmapDefault;
    private static final int REQUEST_CAMERA = 345;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        layoutInflater = LayoutInflater.from(container.getContext());
        bitmapDefault = BitmapFactory.decodeResource(getResources(), R.drawable.ic_avatar_default);
        view = inflater.inflate(R.layout.fragment_profile_picture, null);
        initializeComponent();
        return view;
    }

    private void initializeComponent() {
        imgAvatar = (CircleImageView) view.findViewById(R.id.imgAvatar);
        bitmapAvatar = ((BitmapDrawable)imgAvatar.getDrawable()).getBitmap();
        view.findViewById(R.id.layoutUploadPhoto).setOnClickListener(this);
        view.findViewById(R.id.layoutTakePhoto).setOnClickListener(this);
        view.findViewById(R.id.btnBack).setOnClickListener(this);
        view.findViewById(R.id.btnOK).setOnClickListener(this);
        view.findViewById(R.id.btnSkip).setOnClickListener(this);
    }
    private Uri capturedImageURI;
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnOK:
                signUpSuccess(bitmapAvatar);
                break;
            case R.id.btnSkip:
                signUpSuccess(bitmapDefault);
                break;
            case R.id.layoutUploadPhoto:
                Intent intentAccount = new Intent();
                intentAccount.setClass(getActivity(), ImageActivity.class);
                startActivityForResult(intentAccount, CommonValue.REQUECODE_SET_AVATAR);
                break;
            case R.id.layoutTakePhoto:
                Intent intentCamera = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (intentCamera.resolveActivity(getActivity().getPackageManager()) != null) {
                    @SuppressLint("SimpleDateFormat")
                    String date = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
                    String fileName = "AHIHI_" + date;
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(MediaStore.Images.Media.TITLE, fileName);
                    capturedImageURI = getActivity().getContentResolver().insert(
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                    intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageURI);
                    startActivityForResult(intentCamera, ProfilePictureFragment.this.REQUEST_CAMERA);
                }
                break;
            case R.id.btnBack:
                ((MainFragment)getActivity()).showProfileInfomationFragmentBack();
                break;


        }
    }

    private void signUpSuccess( Bitmap avatar ) {
        final String fullName = ((MainFragment) getActivity()).getProfileInfomationFragment().getFullName();
        final String email = ((MainFragment) getActivity()).getProfileInfomationFragment().getEmail();
        final String birthday = ((MainFragment) getActivity()).getProfileInfomationFragment().getBirthday();
        final boolean sex = ((MainFragment) getActivity()).getProfileInfomationFragment().getSex();

        ParseUser newUser = ParseUser.getCurrentUser();

        newUser.put("fullName", fullName);
        newUser.setEmail(email);
        newUser.put("birthday", birthday);
        newUser.put("sex", sex);
        newUser.saveInBackground();

        imgAvatar.buildDrawingCache();

        CommonMethod.uploadAvatar(newUser, avatar);

        Intent intent = new Intent(this.getActivity(), MainActivity.class);
        this.getActivity().startActivity(intent);
        this.getActivity().finish();
    }

//    @Override
//    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//        imgAvatar.setImageResource(avatarDefaultIDs[position]);
//    }
//
//    private class AvatarDefaultAdapter extends BaseAdapter {
//        @Override
//        public int getCount() {
//            return avatarDefaultIDs.length;
//        }
//
//        @Override
//        public Integer getItem(int position) {
//            return avatarDefaultIDs[position];
//        }
//
//        @Override
//        public long getItemId(int position) {
//            return position;
//        }
//
//        @SuppressLint("ViewHolder")
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//            if (convertView == null) {
//                convertView = layoutInflater.inflate(R.layout.item_image, parent, false);
//            }
//            SquareImageView imgImage = (SquareImageView) convertView.findViewById(R.id.imgImage);
//            imgImage.setImageResource(avatarDefaultIDs[position]);
//            return convertView;
//        }
//    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ( resultCode == Activity.RESULT_OK) {
            switch ( requestCode ) {
                case CommonValue.REQUECODE_SET_AVATAR:
                    byte[] bytes = data.getByteArrayExtra(CommonValue.BYTE_AVATAR);
//                    bitmapAvatar.recycle();
                    bitmapAvatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                    imgAvatar.setImageBitmap(bitmapAvatar);
                    break;
                case REQUEST_CAMERA:
                    Cursor cursor = getActivity().getContentResolver().query(capturedImageURI,
                            new String[]{MediaStore.Images.Media.DATA}, null, null, null);
                    int index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                    cursor.moveToFirst();
                    String capturedImageFilePath = cursor.getString(index);
                    cursor.close();
                    Intent intentCropImage = new Intent();
                    intentCropImage.setClass(getActivity(), ImageControl.class);
                    intentCropImage.putExtra(ImageControl.EXTRA_IMAGE, capturedImageFilePath);
                    startActivityForResult(intentCropImage,CommonValue.REQUECODE_SET_AVATAR);
                    break;
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}