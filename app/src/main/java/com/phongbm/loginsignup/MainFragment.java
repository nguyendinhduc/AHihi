package com.phongbm.loginsignup;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.phongbm.ahihi.R;

public class MainFragment extends AppCompatActivity {
    private HomeFragment homeFragment = new HomeFragment();
    private LoginFragment loginFragment = new LoginFragment();
    private SignupFragment signupFragment = new SignupFragment();
    private ProfileInfomationFragment profileInfomationFragment = new ProfileInfomationFragment();
    private ProfilePictureFragment profilePictureFragment = new ProfilePictureFragment();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.showHomeFragmentNoAnimation();
    }

    public void showHomeFragment() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_left, R.anim.anim_out_right)
                .replace(android.R.id.content, homeFragment).commit();
    }

    public void showHomeFragmentNoAnimation() {
        this.getFragmentManager().beginTransaction().replace(android.R.id.content,
                homeFragment).commit();
    }

    public void showLoginFragment() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_right, R.anim.anim_out_left)
                .replace(android.R.id.content, loginFragment).commit();
    }

    public void showSigupFragment() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_right, R.anim.anim_out_left)
                .replace(android.R.id.content, signupFragment).commit();
    }

    public void showProfileInfomationFragment() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_right, R.anim.anim_out_left)
                .replace(android.R.id.content, profileInfomationFragment).commit();
    }

    public void showProfileInfomationFragmentBack() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_left, R.anim.anim_out_right)
                .replace(android.R.id.content, profileInfomationFragment).commit();
    }

    public void showProfilePictureFragment() {
        this.getFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_in_right, R.anim.anim_out_left)
                .replace(android.R.id.content, profilePictureFragment).commit();
    }

    public ProfileInfomationFragment getProfileInfomationFragment() {
        return profileInfomationFragment;
    }

    @Override
    public void onBackPressed() {
        if (loginFragment.isVisible() || signupFragment.isVisible()) {
            this.showHomeFragment();
        } else {
            if (profileInfomationFragment.isVisible()) {
            } else {
                if (profilePictureFragment.isVisible()) {
                    this.showProfileInfomationFragmentBack();
                } else {
                    super.onBackPressed();
                }
            }
        }
    }

}