package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.view.ContextThemeWrapper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class Profile_theme extends AppCompatActivity {

    private SharedPref sharedPref;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String current_user_id;
    private String color = "blue";
    private CircleImageView profile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        //check theme
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);

            String color_background = sharedPref.loadProfilBackground();

            switch (color_background) {
                case "blue":
                    setTheme(R.style.profile_Background_theme_Blue_dark);
                    Toast.makeText(this, "blue selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "red":
                    setTheme(R.style.profile_Background_theme_red_dark);
                    Toast.makeText(this, "red selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "purple":
                    setTheme(R.style.profile_Background_theme_Purple_dark);
                    Toast.makeText(this, "purple selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "pink":
                    setTheme(R.style.profile_Background_theme_Pink_dark);
                    Toast.makeText(this, "pink selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "orange":
                    setTheme(R.style.profile_Background_theme_orange_dark);
                    Toast.makeText(this, "orange selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "green":
                    setTheme(R.style.profile_Background_theme_Green_dark);
                    Toast.makeText(this, "green selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
            }
        } else {
            String color_background = sharedPref.loadProfilBackground();

            switch (color_background) {
                case "blue":
                    setTheme(R.style.profile_Background_theme_Blue);
                    Toast.makeText(this, "blue selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "red":
                    setTheme(R.style.profile_Background_theme_red);
                    Toast.makeText(this, "red selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "purple":
                    setTheme(R.style.profile_Background_theme_Purple);
                    Toast.makeText(this, "purple selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "pink":
                    setTheme(R.style.profile_Background_theme_Pink);
                    Toast.makeText(this, "pink selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "orange":
                    setTheme(R.style.profile_Background_theme_orange);
                    Toast.makeText(this, "orange selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
                case "green":
                    setTheme(R.style.profile_Background_theme_Green);
                    Toast.makeText(this, "green selected " + color_background, Toast.LENGTH_SHORT).show();
                    break;
            }
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_theme);

        CircleImageView blue = findViewById(R.id.blueImageView);
        CircleImageView red = findViewById(R.id.redImageView);
        CircleImageView pink = findViewById(R.id.pinkImageView);
        CircleImageView green = findViewById(R.id.greenImageView);
        CircleImageView purple = findViewById(R.id.purpleImageView);
        CircleImageView orange = findViewById(R.id.orangeImageView);

        profile = findViewById(R.id.profile_imageView);
        Button apply = findViewById(R.id.applyBtn);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        current_user_id = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                String thumb_image = task.getResult().getString("thumb_image");

                setProfile(thumb_image);
            }
        });

        blue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //blue selected
                color = "blue";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });

        red.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //red selected
                color = "red";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });

        orange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //orange selected
                color = "orange";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });

        pink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //pink selected
                color = "pink";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });

        green.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //green selected
                color = "green";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });

        purple.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //purple selected
                color = "purple";
                sharedPref.setProfileBackground(color);
                setProfileBackgroudOnline(current_user_id);
            }
        });
    }

    private void setProfile(String thumb_image) {
        Glide.with(Profile_theme.this)
                .load(thumb_image)
                .into(profile);
    }

    private void setProfileBackgroudOnline(String user_id) {
        Map<String, Object> themeMap = new HashMap<>();
        themeMap.put("theme", color);
        firebaseFirestore.collection("Users").document(user_id).update(themeMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(Profile_theme.this, "Theme updated", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(Profile_theme.this, Profile_theme.class);
                            startActivity(i);
                            finish();
                        } else {
                            Log.e("theme", "theme not updated on the database " + task.getException());
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
