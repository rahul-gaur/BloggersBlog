package com.rahulgaur.bloggersblog.account;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.blogPost.postid;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * @author Rahul Gaur
 */

public class UserAccount extends AppCompatActivity {

    private static final String TAG = "UserAccount";
    private String post_user_id;
    private String name;
    private String imageURL;
    private CircleImageView profileImage;
    private ImageView profile_background;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private SharedPref sharedPref;
    private android.support.v7.widget.Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private ArrayList<GridViewList> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GridViewList gridViewList;
    private int post_count = 0;
    private TextView post_countTV;
    private UserAdapter userAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            this.setTheme(R.style.darkTheme);
            setGradTheme(R.drawable.profile_red_back_grad);
        } else {
            this.setTheme(R.style.AppTheme);
            setGradTheme(R.drawable.profile_red_grad);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_account);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        final String current_userID = firebaseAuth.getCurrentUser().getUid();

        final ProgressDialog progressDialog1 = new ProgressDialog(UserAccount.this);
        progressDialog1.setTitle("Loading");
        progressDialog1.setMessage("Please wait a bit..");

        profile_background = findViewById(R.id.acc_user_backgroundImage);

        if (sharedPref.loadNightModeState()) {
            setGradTheme(R.drawable.profile_red_back_grad);
        } else {
            setGradTheme(R.drawable.profile_red_grad);
        }

        post_countTV = findViewById(R.id.acc_user_post_count);

        postid pd = new postid();
        final String post_id = pd.getPostid();
        post_user_id = getIntent().getStringExtra("post_user_id");
        Log.e("post_user_id", "user id from intent " + post_user_id);

        toolbar = findViewById(R.id.user_account_frag_toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.user_account_recyclerView);

        final TextView followersTV = findViewById(R.id.acc_user_followers);
        final TextView followingTV = findViewById(R.id.acc_user_following);
        final Button followBtn = findViewById(R.id.user_account_followBtn);

        //followers count
        firebaseFirestore.collection("Users/" + post_user_id + "/Followers").addSnapshotListener(UserAccount.this, new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        followersTV.setText(count + "");
                    } else {
                        followersTV.setText("0");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //following count
        firebaseFirestore.collection("Users/" + post_user_id + "/Following").addSnapshotListener(UserAccount.this, new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()) {
                    int count = documentSnapshots.size();
                    followingTV.setText("" + count);
                } else {
                    followingTV.setText("0");
                }
            }
        });

        //follow on start
        Log.e(TAG, "onCreate: post user id " + post_user_id);
        Log.e(TAG, "onCreate: current user id " + current_userID);
        firebaseFirestore.collection("Users/" + post_user_id + "/Followers").document(current_userID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.getResult().exists()) {
                                followBtn.setText("Unfollow");
                                followBtn.setBackgroundColor(Color.RED);
                            } else {
                                followBtn.setText("Follow");
                                followBtn.setBackgroundColor(getResources().getColor(R.color.pbBlue));
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        //follow and unfollow feature
        followBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore = FirebaseFirestore.getInstance();
                firebaseFirestore = FirebaseFirestore.getInstance();
                progressDialog1.show();
                progressDialog1.setCanceledOnTouchOutside(false);

                //user is unfollowed right now
                if (followBtn.getText().toString().toLowerCase().equals("follow")) {
                    Map<String, Object> map = new HashMap<>();
                    map.put("timestamp", FieldValue.serverTimestamp());
                    firebaseFirestore.collection("Users/" + post_user_id + "/Followers").document(current_userID).set(map)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            Map<String, Object> followingMap = new HashMap<>();
                                            followingMap.put("timestamp", FieldValue.serverTimestamp());
                                            firebaseFirestore.collection("Users/" + current_userID + "/Following").document(post_user_id).set(followingMap)
                                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<Void> task) {
                                                            try {
                                                                if (task.isSuccessful()) {
                                                                    followBtn.setText("Unfollow");
                                                                    followBtn.setBackgroundColor(Color.RED);
                                                                    progressDialog1.dismiss();
                                                                } else {
                                                                    Toast.makeText(UserAccount.this, "error " + task.getException(), Toast.LENGTH_SHORT).show();
                                                                    Log.e(TAG, "onComplete: error " + task.getException());
                                                                    progressDialog1.dismiss();
                                                                }
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
                                                                progressDialog1.dismiss();
                                                            }
                                                        }
                                                    });
                                        } else {
                                            Toast.makeText(UserAccount.this, "error " + task.getException(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "onComplete: error " + task.getException());
                                            progressDialog1.dismiss();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        progressDialog1.dismiss();
                                    }
                                }
                            });
                } else if (followBtn.getText().toString().toLowerCase().equals("unfollow")) {
                    //user is followed right now
                    progressDialog1.show();
                    progressDialog1.setCanceledOnTouchOutside(false);
                    firebaseFirestore.collection("Users/" + post_user_id + "/Followers").document(current_userID).delete()
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            //unfollowed
                                            firebaseFirestore.collection("Users/" + current_userID + "/Following").document(post_user_id).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    try {
                                                        if (task.isSuccessful()) {
                                                            followBtn.setBackgroundColor(getResources().getColor(R.color.pbBlue));
                                                            followBtn.setText("Follow");
                                                            Log.e(TAG, "onComplete: unfollowed");
                                                            progressDialog1.dismiss();
                                                        } else {
                                                            //error
                                                            Toast.makeText(UserAccount.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                                                            Log.e(TAG, "onComplete: error " + task.getException());
                                                            progressDialog1.dismiss();
                                                        }
                                                    } catch (Resources.NotFoundException e) {
                                                        e.printStackTrace();
                                                        progressDialog1.dismiss();
                                                    }
                                                }
                                            });
                                        } else {
                                            //error
                                            Toast.makeText(UserAccount.this, "Error " + task.getException(), Toast.LENGTH_SHORT).show();
                                            Log.e(TAG, "onComplete: error " + task.getException());
                                            progressDialog1.dismiss();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        progressDialog1.dismiss();
                                    }
                                }
                            });
                }
            }
        });

        gridViewList = new GridViewList();

        progressDialog = new ProgressDialog(UserAccount.this);
        progressDialog.setMessage("Loading Please Wait..");
        progressDialog.show();
        progressDialog.setCancelable(false);

        recyclerView.setLayoutManager(new GridLayoutManager(this, 3));
        userAdapter = new UserAdapter(postList);
        recyclerView.setAdapter(userAdapter);

        profileImage = findViewById(R.id.user_account_profileImage);
        swipeRefreshLayout = findViewById(R.id.user_account_swipeRefresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                userAdapter.notifyDataSetChanged();
                Toast.makeText(UserAccount.this, "Refreshed..", Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //retrieving user profile and name
        firebaseFirestore.collection("Users")
                .document(post_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        imageURL = task.getResult().getString("thumb_image");
                        name = task.getResult().getString("name");

                        setProfileImage(imageURL);
                        getSupportActionBar().setTitle(name + "'s Profile");
                        progressDialog.dismiss();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        //getting posts from the database
        firebaseFirestore.collection("Posts/" + post_id).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String post_thumb_url = doc.getDocument().getString("thumb_image_url");
                                String blog_user_id = doc.getDocument().getString("user_id");
                                String blog_post_id = doc.getDocument().getId();

                                if (post_user_id.equals(blog_user_id)) {
                                    try {
                                        post_count++;
                                        Log.e(TAG, "onEvent: post count " + post_count);
                                        post_countTV.setText(post_count + "");
                                    } catch (Exception e1) {
                                        Log.e(TAG, "onEvent: exception " + e1.getMessage());
                                        Log.e(TAG, "onEvent: Exception while printing post count");
                                        e1.printStackTrace();
                                    }
                                    postList.add(new GridViewList(post_thumb_url, blog_post_id));
                                    gridViewList.setBlogPostID(blog_post_id);
                                    Log.e("userAccount", "post id " + blog_post_id + " post url " + post_thumb_url);
                                    userAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    } else {
                        //some error
                        Log.e("userFragment", "error in user_id and image url");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
    }

    private void setGradTheme(int grad) {
        try {
            Glide.with(UserAccount.this).load(grad).into(profile_background);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setProfileImage(String profile) {
        Glide.with(this)
                .load(profile)
                .into(profileImage);
    }
}