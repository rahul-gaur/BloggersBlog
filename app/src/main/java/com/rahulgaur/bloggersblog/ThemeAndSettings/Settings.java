package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.account.AccountFragment;
import com.rahulgaur.bloggersblog.comment.Comments;
import com.rahulgaur.bloggersblog.home.HomeFragment;
import com.rahulgaur.bloggersblog.home.MainActivity;
import com.rahulgaur.bloggersblog.home.NewPostActivity;
import com.rahulgaur.bloggersblog.notification.NotificationFragment;
import com.rahulgaur.bloggersblog.welcome.RegisterPage;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

public class Settings extends AppCompatActivity {

    private DayNightTheme dayNightTheme = new DayNightTheme();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //check theme
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //TO-DO = Add Toolbar name "Settings".

        SwitchCompat switchCompat = findViewById(R.id.setting_switchCompat);

        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            switchCompat.setChecked(true);
        }

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    nightMode("night");
                    restartApp();
                } else {
                    nightMode("day");
                    restartApp();
                }
            }
        });

    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    private void nightMode(String mode) {
        if (mode.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            setMode(mode);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            setMode(mode);
        }
    }

    private void setMode(String mode) {
        RegisterPage registerPage = new RegisterPage();
        registerPage.nightMode(mode);

        WelcomePage welcomePage = new WelcomePage();
        welcomePage.nightMode(mode);

        NotificationFragment notificationFragment = new NotificationFragment();
        notificationFragment.nightMode(mode);

        NewPostActivity newPostActivity = new NewPostActivity();
        newPostActivity.nightMode(mode);

        MainActivity mainActivity = new MainActivity();
        mainActivity.nightMode(mode);

        HomeFragment homeFragment = new HomeFragment();
        homeFragment.nightMode(mode);

        Comments comments = new Comments();
        comments.nightMode(mode);

        Account account = new Account();
        account.nightMode(mode);

        AccountFragment accountFragment = new AccountFragment();
        accountFragment.nightMode(mode);
    }
}