package com.rahulgaur.bloggersblog.account;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;

public class UserAccount extends AppCompatActivity {

    private String post_user_id;
    private String name;
    private String imageURL;
    private ImageView profileImage;
    private FirebaseFirestore firebaseFirestore;
    private SharedPref sharedPref;
    private android.support.v7.widget.Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            this.setTheme(R.style.darkTheme);
        } else {
            this.setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);
        post_user_id = getIntent().getStringExtra("post_user_id");

        toolbar = findViewById(R.id.user_account_frag_toolbar);
        setSupportActionBar(toolbar);

        progressDialog = new ProgressDialog(UserAccount.this);
        progressDialog.setMessage("Loading Please Wait..");
        progressDialog.show();
        progressDialog.setCancelable(false);

        profileImage = findViewById(R.id.user_account_profileImage);
        swipeRefreshLayout = findViewById(R.id.user_account_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(UserAccount.this, "Refreshed..", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Users")
                .document(post_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    imageURL = task.getResult().getString("thumb_image");
                    name = task.getResult().getString("name");

                    setProfileImage(imageURL);
                    getSupportActionBar().setTitle(name+" ");
                    progressDialog.dismiss();
                }
            }
        });
    }

    public void setProfileImage(String profile) {
        Glide.with(this)
                .load(profile)
                .into(profileImage);
    }
}