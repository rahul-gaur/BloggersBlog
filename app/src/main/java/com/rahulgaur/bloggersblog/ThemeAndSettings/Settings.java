package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
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

import java.util.Objects;

public class Settings extends AppCompatActivity {

    private SharedPref sharedPref;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        //check theme
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //TO-DO = Add Toolbar name "Settings".
        toolbar = findViewById(R.id.setting_toolbar);

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Settings");

        SwitchCompat switchCompat = findViewById(R.id.setting_switchCompat);

        if (sharedPref.loadNightModeState()) {
            switchCompat.setChecked(true);
        }

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    sharedPref.setNightModeState(true);
                    restartApp();
                } else {
                    sharedPref.setNightModeState(false);
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
}