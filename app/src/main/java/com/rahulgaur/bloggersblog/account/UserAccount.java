package com.rahulgaur.bloggersblog.account;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.rahulgaur.bloggersblog.account.Followers.FollowersList;
import com.rahulgaur.bloggersblog.account.Followers.FollowersRecyclerViewer;
import com.rahulgaur.bloggersblog.blogPost.postid;
import com.rahulgaur.bloggersblog.comment.Comments;
import com.rahulgaur.bloggersblog.notification.Remote.APIService;
import com.rahulgaur.bloggersblog.notification.notificationServices.Data;
import com.rahulgaur.bloggersblog.notification.notificationServices.MyResponse;
import com.rahulgaur.bloggersblog.notification.notificationServices.Sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rahulgaur.bloggersblog.home.MainActivity.restartMain;

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
    private ProgressDialog progressDialog;
    private ArrayList<GridViewList> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GridViewList gridViewList;
    private int post_count = 0;
    private TextView post_countTV;
    private UserAdapter userAdapter;
    private List<FollowersList> followersLists;
    private FollowersRecyclerViewer followersRecyclerAdapter;
    private APIService apiService;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            this.setTheme(R.style.darkTheme);
            setGradTheme(R.drawable.profile_green_back_grad);
        } else {
            this.setTheme(R.style.AppTheme);
            setGradTheme(R.drawable.profile_green_grad);
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
            setGradTheme(R.drawable.profile_green_back_grad);
        } else {
            setGradTheme(R.drawable.profile_green_grad);
        }

        post_countTV = findViewById(R.id.acc_user_post_count);

        postid pd = new postid();
        final String post_id = pd.getPostid();
        post_user_id = getIntent().getStringExtra("post_user_id");
        Log.e("post_user_id", "user id from intent " + post_user_id);

        toolbar = findViewById(R.id.user_account_frag_toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.user_account_recyclerView);

        final LinearLayout followersLayout = findViewById(R.id.user_followers_layout);
        final LinearLayout followingLayout = findViewById(R.id.user_following_layout);
        final TextView followersTV = findViewById(R.id.acc_user_followers);
        final TextView followingTV = findViewById(R.id.acc_user_following);
        final Button followBtn = findViewById(R.id.user_account_followBtn);
        NestedScrollView followSheetLayout = findViewById(R.id.user_sheet_layout);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(followSheetLayout);
        RecyclerView followRecyclerViw = findViewById(R.id.user_sheet_recyclerView);
        final TextView followTV = findViewById(R.id.user_sheet_textView);
        followersLists = new ArrayList<>();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        followRecyclerViw.setLayoutManager(new LinearLayoutManager(UserAccount.this));
        followersRecyclerAdapter = new FollowersRecyclerViewer(followersLists);
        followRecyclerViw.setAdapter(followersRecyclerAdapter);

        //Same2you#
        //Akonftjb# transaction
        //d10256858

        //followers list
        followersLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followersLists.clear();
                followTV.setText("Followers");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                firebaseFirestore.collection("Users/" + post_user_id + "/Followers")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    try {
                                        if (doc.exists()) {
                                            String followers_id = doc.getId();
                                            final FollowersList followersList = doc.toObject(FollowersList.class).withID(followers_id);
                                            firebaseFirestore.collection("Users").document(followers_id)
                                                    .addSnapshotListener(UserAccount.this, new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            try {
                                                                if (documentSnapshot.exists()) {
                                                                    String name = documentSnapshot.getString("name");
                                                                    String thumb_image = documentSnapshot.getString("thumb_image");

                                                                    Log.e(TAG, "onEvent: name " + name);

                                                                    followersList.setName(name);
                                                                    followersList.setThumb_image(thumb_image);

                                                                    followersLists.add(followersList);
                                                                    followersRecyclerAdapter.notifyDataSetChanged();
                                                                }
                                                            } catch (Exception e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //following list
        followingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                followTV.setText("Following");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                followersLists.clear();
                firebaseFirestore.collection("Users/" + post_user_id + "/Following")
                        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                for (DocumentSnapshot doc : task.getResult()) {
                                    try {
                                        if (doc.exists()) {
                                            String followers_id = doc.getId();
                                            final FollowersList followersList = doc.toObject(FollowersList.class)
                                                    .withID(followers_id);
                                            firebaseFirestore.collection("Users")
                                                    .document(followers_id)
                                                    .addSnapshotListener(UserAccount.this, new EventListener<DocumentSnapshot>() {
                                                        @Override
                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                            try {
                                                                if (documentSnapshot.exists()) {
                                                                    String name = documentSnapshot.getString("name");
                                                                    String thumb_image = documentSnapshot.getString("thumb_image");

                                                                    Log.e(TAG, "onEvent: name " + name);

                                                                    followersList.setName(name);
                                                                    followersList.setThumb_image(thumb_image);

                                                                    followersLists.add(followersList);
                                                                    followersRecyclerAdapter.notifyDataSetChanged();
                                                                }
                                                            } catch (Exception e1) {
                                                                e1.printStackTrace();
                                                            }
                                                        }
                                                    });
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

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
                    final Map<String, Object> map = new HashMap<>();
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
                                                                    restartMain = "yes";
                                                                    firebaseFirestore.collection("Users")
                                                                            .document(current_userID)
                                                                            .addSnapshotListener(UserAccount.this, new EventListener<DocumentSnapshot>() {
                                                                                @Override
                                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                                    try {
                                                                                        if (documentSnapshot.exists()) {
                                                                                            final String user_name = documentSnapshot.getString("name");
                                                                                            Log.e(TAG, "onEvent: inNotification" );
                                                                                            Map<String, Object> notificationMap = new HashMap<>();
                                                                                            notificationMap.put("user_id", current_userID);
                                                                                            notificationMap.put("timestamp", FieldValue.serverTimestamp());
                                                                                            notificationMap.put("message", "<b>" + user_name + "</b> <br>Followed you");
                                                                                            Log.e(TAG, "onEvent: inNotification "+user_name);
                                                                                            firebaseFirestore.collection("Users/" + post_user_id + "/Notification").add(notificationMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                                @Override
                                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                                    try {
                                                                                                        if (task.isSuccessful()) {
                                                                                                            Log.e(TAG, "onEvent: inside notification document" );
                                                                                                            firebaseFirestore.collection("Users/").document(post_user_id).addSnapshotListener(UserAccount.this, new EventListener<DocumentSnapshot>() {
                                                                                                                @Override
                                                                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                                                                    try {
                                                                                                                        if (documentSnapshot.exists()) {
                                                                                                                            String token = documentSnapshot.getString("token");
                                                                                                                            Log.e(TAG, "onEvent: generating token and adding noti to "+token );
                                                                                                                            Data data = new Data("no", "no",post_user_id);
                                                                                                                            com.rahulgaur.bloggersblog.notification.notificationServices.Notification notification = new com.rahulgaur.bloggersblog.notification.notificationServices.Notification("Comments", user_name + " Followed you", "com.rahulgaur.bloggersblog.followed");
                                                                                                                            Sender sender = new Sender(notification, token, data); //send notification to token
                                                                                                                            Log.e("Sender Token", "" + token);
                                                                                                                            apiService.sendNotification(sender)
                                                                                                                                    .enqueue(new Callback<MyResponse>() {
                                                                                                                                        @Override
                                                                                                                                        public void onResponse
                                                                                                                                                (Call<MyResponse> call, Response<MyResponse> response) {
                                                                                                                                            try {
                                                                                                                                                if (response.body().success == 1) {
                                                                                                                                                    Log.e("Notification service ", "Success");
                                                                                                                                                    Log.e(TAG, "onEvent: notification sent" );
                                                                                                                                                    followersRecyclerAdapter.notifyDataSetChanged();
                                                                                                                                                } else {
                                                                                                                                                    Log.e("Notification service ", "Failed");
                                                                                                                                                    followersRecyclerAdapter.notifyDataSetChanged();
                                                                                                                                                }
                                                                                                                                            } catch (NullPointerException ne) {
                                                                                                                                                Log.e("Notification", "Exception " + ne.getMessage());
                                                                                                                                            }
                                                                                                                                        }

                                                                                                                                        @Override
                                                                                                                                        public void onFailure
                                                                                                                                                (Call<MyResponse> call, Throwable
                                                                                                                                                        t) {
                                                                                                                                            Log.e("Notification service ", "Failed");
                                                                                                                                        }
                                                                                                                                    });
                                                                                                                            Log.e("Comment notificaiton", "Comment Notification Added");
                                                                                                                            followersRecyclerAdapter.notifyDataSetChanged();
                                                                                                                        }
                                                                                                                    } catch (Exception e1) {
                                                                                                                        e1.printStackTrace();
                                                                                                                    }
                                                                                                                }
                                                                                                            });
                                                                                                        } else

                                                                                                        {
                                                                                                            Log.e("Comment notificaiton", "Comment Notification failed");
                                                                                                        }
                                                                                                    } catch (Exception e1) {
                                                                                                        e1.printStackTrace();
                                                                                                    }
                                                                                                }
                                                                                            });
                                                                                        }
                                                                                    } catch (Exception e1) {
                                                                                        e1.printStackTrace();
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
                                                            restartMain = "yes";
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