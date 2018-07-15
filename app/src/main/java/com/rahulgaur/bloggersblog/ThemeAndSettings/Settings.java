package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.rahulgaur.bloggersblog.R;

import java.util.Objects;

public class Settings extends AppCompatActivity {

    private SharedPref sharedPref;
    private Toolbar toolbar;
    private BlockFragment blockFragment;
    private TextView textView;
    private int i;

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

        blockFragment = new BlockFragment();

        textView = findViewById(R.id.setting_block_textView);

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
        i = 0;
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i < 1) {
                    i++;
                    setFragment();
                } else {
                    revertFragment();
                    i--;
                }
            }
        });
    }

    private void revertFragment() {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.remove(blockFragment);
        fragmentTransaction.commit();
    }

    private void restartApp() {
        Intent i = getBaseContext().getPackageManager()
                .getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    public void setFragment() {
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.setting_block_fragmentFrame, blockFragment);
        fragmentTransaction.commit();
    }
}