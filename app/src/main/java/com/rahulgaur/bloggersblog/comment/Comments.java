package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.rahulgaur.bloggersblog.ThemeAndSettings.DayNightTheme;
import com.rahulgaur.bloggersblog.blogPost.Post;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comments extends AppCompatActivity {

    private EditText comment_field;
    private ImageView comment_postView, postUserImageView;
    private TextView comment_Username;

    private Post postClass = new Post();

    private List<CommentList> cmntList;

    private String blog_post_id;
    private String current_user_id;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        android.support.v7.widget.Toolbar commentToolbar = findViewById(R.id.cmntToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cmntList = new ArrayList<>();

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(cmntList,blog_post_id);

        comment_field = findViewById(R.id.cmntEditText);
        ImageView comment_post_btn = findViewById(R.id.cmntPostImageView);
        RecyclerView comment_list = findViewById(R.id.cmntRecyclerView);
        comment_postView = findViewById(R.id.cmntImageView);
        comment_Username = findViewById(R.id.cmnt_usernameTV);
        postUserImageView = findViewById(R.id.cmntProfileImageView);

        comment_list.setLayoutManager(new LinearLayoutManager(Comments.this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        comment_field.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                comment_field.setAlpha(1.00f);
                return false;
            }
        });

        current_user_id = auth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");


        //post image and username retrieving
        firebaseFirestore.collection("Posts/").document(blog_post_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    final String post_user_id = documentSnapshot.getString("user_id");
                    final String post_picture = documentSnapshot.getString("thumb_image_url");

                    firebaseFirestore.collection("Users").document(post_user_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
                        @Override
                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                            if (documentSnapshot.exists()) {
                                final String post_username = documentSnapshot.getString("name");
                                final String post_userProfile = documentSnapshot.getString("thumb_image");
                                setPostImage(post_picture);
                                setUsernameAndProfile(post_username, post_userProfile);
                            }
                        }
                    });
                }
            }
        });

        //comments retrieving
        firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments")
                .addSnapshotListener(Comments.this, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String commentid = doc.getDocument().getId();
                                    CommentList commentList = doc.getDocument().toObject(CommentList.class)
                                            .withID(commentid);
                                    cmntList.add(commentList);
                                    commentsRecyclerAdapter.notifyDataSetChanged();

                                }
                            }
                        }

                    }
                });

        //comment posting
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                                    if (!task.isSuccessful()) {
                                    } else {
                                        comment_field.setText(null);
                                        comment_field.clearFocus();
                                    }
                                }
                            });

                } else {
                    Toast.makeText(Comments.this, "Please write the comment", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @SuppressLint("CheckResult")
    private void setPostImage(String post_picture) {

        Log.e("Comments.java", "post image url " + post_picture);

        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.ic_launcher_background);

        Glide.with(Comments.this)
                .applyDefaultRequestOptions(placeholder)
                .load(post_picture)
                .into(comment_postView);
    }

    @SuppressLint("CheckResult")
    private void setUsernameAndProfile(String name, String image) {

        Log.e("Comments.java", "image url " + image + " name " + name);

        comment_Username.setText(name);

        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.default_usr);

        Glide.with(Comments.this)
                .applyDefaultRequestOptions(placeholder)
                .load(image)
                .into(postUserImageView);
    }

    public void nightMode(String mode) {
        if (mode.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }
}