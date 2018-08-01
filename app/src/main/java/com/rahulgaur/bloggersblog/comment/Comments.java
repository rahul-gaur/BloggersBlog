package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.comment.like_sheet.LikeList;
import com.rahulgaur.bloggersblog.comment.like_sheet.LikeRecyclerAdapter;
import com.rahulgaur.bloggersblog.notification.Remote.APIService;
import com.rahulgaur.bloggersblog.notification.notificationServices.Common;
import com.rahulgaur.bloggersblog.notification.notificationServices.Data;
import com.rahulgaur.bloggersblog.notification.notificationServices.MyResponse;
import com.rahulgaur.bloggersblog.notification.notificationServices.Sender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.rahulgaur.bloggersblog.home.PostRecyclerAdapter.context;

public class Comments extends AppCompatActivity {

    private static final String TAG = "Comments.java";
    private EditText comment_field;
    private ImageView comment_postView, postUserImageView;
    private TextView comment_Username;
    private FirebaseAnalytics mFirebaseAnalytics;

    private String post_user_id;

    private LikeRecyclerAdapter likeRecyclerAdapter;

    private List<CommentList> cmntList;
    private ArrayList<LikeList> likeLists;
    private APIService apiService;
    private TextView descTV;
    private String post_user_token;
    private String blog_post_id;

    //private String blog_post_id = null;
    private String current_user_id;
    private String post_user;

    private NestedScrollView likeSheetLayout;
    private BottomSheetBehavior bottomSheetBehavior;
    private ImageView likeView;
    private TextView likeText;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ProgressBar progressBar;

    private SharedPref sharedPref;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);

        String id = null;
        try {
            id = getIntent().getStringExtra("id");
            Log.e(TAG, "onCreate: id from notification " + id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            blog_post_id = getIntent().getStringExtra("blog_post_id");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            if (id.length() > 0) {
                blog_post_id = id;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Log.e(TAG, "onCreate: blog post id after id " + blog_post_id);

        Toolbar commentToolbar = findViewById(R.id.cmntToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        likeText = findViewById(R.id.comment_likeTV);
        likeView = findViewById(R.id.comment_likeView);
        likeSheetLayout = findViewById(R.id.like_sheet_layout);
        bottomSheetBehavior = BottomSheetBehavior.from(likeSheetLayout);
        descTV = findViewById(R.id.cmnt_DescTV);
        RecyclerView likeRecyclerView = findViewById(R.id.like_sheet_recyclerView);

        likeRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        likeLists = new ArrayList<>();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        likeRecyclerAdapter = new LikeRecyclerAdapter(likeLists);

        likeRecyclerView.setAdapter(likeRecyclerAdapter);

        likeText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cmntList = new ArrayList<>();

        apiService = Common.getFCMCLient();

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(cmntList, blog_post_id);

        comment_field = findViewById(R.id.cmntEditText);
        ImageView comment_post_btn = findViewById(R.id.cmntPostImageView);
        final RecyclerView comment_list = findViewById(R.id.cmntRecyclerView);
        comment_postView = findViewById(R.id.cmntImageView);
        comment_Username = findViewById(R.id.cmnt_usernameTV);
        postUserImageView = findViewById(R.id.cmntProfileImageView);
        progressBar = findViewById(R.id.comment_progressBar);

        comment_list.setLayoutManager(new LinearLayoutManager(Comments.this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        comment_field.clearFocus();

        comment_field.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                comment_field.setAlpha(1.00f);
                return false;
            }
        });

        current_user_id = auth.getCurrentUser().getUid();

        //Likes count
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes")
                .addSnapshotListener(Comments.this, new EventListener<QuerySnapshot>() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                //some likes
                                int count = documentSnapshots.size();
                                if (count == 1) {
                                    Glide.with(Comments.this).load(R.mipmap.like_pink).into(likeView);
                                    likeText.setText("1 person liked this pic");
                                } else {
                                    Glide.with(Comments.this).load(R.mipmap.like_pink).into(likeView);
                                    likeText.setText(count + " people liked this pic");
                                }
                            } else {
                                likeText.setText("No likes \uD83D\uDE32 ");
                                //no likes
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });


        Log.e(TAG, "onCreate: blog post id before post and image " + blog_post_id);
        //post image and username retrieving
        firebaseFirestore.collection("Posts/").document(blog_post_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                try {
                    if (documentSnapshot.exists()) {
                        progressBar.setVisibility(View.VISIBLE);
                        final String post_user_id = documentSnapshot.getString("user_id");
                        final String post_picture = documentSnapshot.getString("thumb_image_url");
                        final String token = documentSnapshot.getString("token");
                        final String desc = documentSnapshot.getString("desc");
                        post_user_token = token;
                        post_user = post_user_id;
                        Log.e("Comment post", "post user id " + post_user_id);

                        firebaseFirestore.collection("Users").document(post_user_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
                            @Override
                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                if (documentSnapshot.exists()) {
                                    final String post_username = documentSnapshot.getString("name");
                                    final String post_userProfile = documentSnapshot.getString("thumb_image");
                                    setPostImage(post_picture);
                                    setPostDesc(desc);
                                    Log.e("Comments.java", "post image is loaded ");
                                    setUsernameAndProfile(post_username, post_userProfile);
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    Log.e("Comments.java", "No user found");
                                }
                            }
                        });
                    } else {
                        Log.e("Comments.java", "No data comment.java " + blog_post_id);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //comments retrieving
        Query sortComment = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").orderBy("timestamp", Query.Direction.ASCENDING);
        sortComment.addSnapshotListener(Comments.this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String commentid = doc.getDocument().getId();
                                CommentList commentList = doc.getDocument().toObject(CommentList.class)
                                        .withID(commentid);
                                cmntList.add(commentList);
                                commentList.setPost_user_id(post_user);
                                commentList.setPostID(blog_post_id);
                                Log.e("Comment post", "post user id in cmntRet " + post_user);
                                commentsRecyclerAdapter.notifyDataSetChanged();

                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }

            }
        });

        //likes retrieving
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Likes").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        try {
                            for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                String user_id = documentSnapshot.getId();
                                final LikeList likes = documentSnapshot.toObject(LikeList.class)
                                        .withID(user_id);
                                firebaseFirestore.collection("Users").document(user_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
                                    @Override
                                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                        try {
                                            if (documentSnapshot.exists()) {
                                                String name = documentSnapshot.getString("name");
                                                String thumb_image = documentSnapshot.getString("thumb_image");

                                                Log.e(TAG, "onEvent: name " + name);

                                                likes.setName(name);
                                                likes.setThumb_image(thumb_image);

                                                likeLists.add(likes);
                                                likeRecyclerAdapter.notifyDataSetChanged();
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
                    } else {
                        Log.e(TAG, "onComplete: some error " + task.getException().getMessage());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        });

        //comment posting
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                final String comment_message = comment_field.getText().toString();
                comment_field.setText(null);

                if (!comment_message.isEmpty()) {

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_message);
                    commentMap.put("user_id", current_user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                            .add(commentMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            Log.e("Comment notificaiton", "Comment Notification Entered");
                                            Log.e("Comment notificaiton", "Comment Notification current user id " + current_user_id);

                                            firebaseFirestore.collection("Posts").document(blog_post_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                    try {
                                                        if (documentSnapshot.exists()) {
                                                            post_user_id = documentSnapshot.getString("user_id");

                                                            Log.e("comment", "post user id " + post_user_id);

                                                            firebaseFirestore.collection("Users").document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                    try {
                                                                        if (documentSnapshot.exists()) {
                                                                            Log.e("Comment notificaiton", "Comment Notification current user entered");

                                                                            final String current_user_name = documentSnapshot.getString("name");
                                                                            Log.e("Comment notificaiton", "Comment Notification current user name " + current_user_name);

                                                                            Map<String, Object> notificaitonMap = new HashMap<>();
                                                                            notificaitonMap.put("post_id", blog_post_id);
                                                                            notificaitonMap.put("timestamp", FieldValue.serverTimestamp());
                                                                            notificaitonMap.put("message", "<b>" + current_user_name + "</b> Commented: <br>" + comment_message);
                                                                            Log.e("Notification user", "post user id " + post_user_id);
                                                                            firebaseFirestore.collection("Users/" + post_user_id + "/Notification").add(notificaitonMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                                @Override
                                                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                    try {
                                                                                        if (task.isSuccessful()) {
                                                                                            firebaseFirestore.collection("Users/").document(post_user_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
                                                                                                @Override
                                                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                                                    try {
                                                                                                        if (documentSnapshot.exists()) {
                                                                                                            String token = documentSnapshot.getString("token");
                                                                                                            Data data = new Data(blog_post_id, "no","no");
                                                                                                            com.rahulgaur.bloggersblog.notification.notificationServices.Notification notification = new com.rahulgaur.bloggersblog.notification.notificationServices.Notification("Comments", current_user_name + " Commented " + comment_message, "com.rahulgaur.bloggersblog.fcmClick");
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
                                                                                                                                    commentsRecyclerAdapter.notifyDataSetChanged();
                                                                                                                                } else {
                                                                                                                                    Log.e("Notification service ", "Failed");
                                                                                                                                    commentsRecyclerAdapter.notifyDataSetChanged();
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
                                                                                                            commentsRecyclerAdapter.notifyDataSetChanged();
                                                                                                            progressBar.setVisibility(View.INVISIBLE);
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
                                                                        } else

                                                                        {
                                                                            Log.e("Comment notificaiton", "Comment Notification document not found");
                                                                        }
                                                                    } catch (Exception e1) {
                                                                        e1.printStackTrace();
                                                                    }
                                                                }
                                                            });

                                                        } else {
                                                            Log.e("comment", "post user id not found ");
                                                        }
                                                    } catch (Exception e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            });

                                        } else {
                                            commentsRecyclerAdapter.notifyDataSetChanged();
                                            comment_field.setText(null);
                                            comment_field.clearFocus();
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                } else

                {
                    Toast.makeText(Comments.this, "Please write the comment", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void setPostDesc(String desc) {
        descTV.setText(desc);
    }

    @SuppressLint("CheckResult")
    private void setPostImage(String post_picture) {
        Log.e("Comments.java", "post image url " + post_picture);
        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.ic_launcher_background);
        try {
            Glide.with(Comments.this)
                    .applyDefaultRequestOptions(placeholder)
                    .load(post_picture)
                    .into(comment_postView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("CheckResult")
    private void setUsernameAndProfile(String name, String image) {
        Log.e("Comments.java", "image url " + image + " name " + name);
        comment_Username.setText(name);
        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.default_usr);
        try {
            Glide.with(Comments.this)
                    .applyDefaultRequestOptions(placeholder)
                    .load(image)
                    .into(postUserImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}