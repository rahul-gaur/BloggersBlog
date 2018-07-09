package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.ServerTimestamp;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comments extends AppCompatActivity {

    private EditText comment_field;
    private ImageView comment_postView, postUserImageView;
    private TextView comment_Username;

    private String post_user_id;

    private List<CommentList> cmntList;

    private String blog_post_id;
    private String current_user_id;

    private CommentsRecyclerAdapter commentsRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

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

        Toolbar commentToolbar = findViewById(R.id.cmntToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cmntList = new ArrayList<>();

        blog_post_id = getIntent().getStringExtra("blog_post_id");

        commentsRecyclerAdapter = new CommentsRecyclerAdapter(cmntList, blog_post_id);

        comment_field = findViewById(R.id.cmntEditText);
        ImageView comment_post_btn = findViewById(R.id.cmntPostImageView);
        RecyclerView comment_list = findViewById(R.id.cmntRecyclerView);
        comment_postView = findViewById(R.id.cmntImageView);
        comment_Username = findViewById(R.id.cmnt_usernameTV);
        postUserImageView = findViewById(R.id.cmntProfileImageView);

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

        //post image and username retrieving
        firebaseFirestore.collection("Posts/").document(blog_post_id).addSnapshotListener(Comments.this, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    final String post_user_id = documentSnapshot.getString("user_id");
                    final String post_picture = documentSnapshot.getString("thumb_image_url");

                    String post_user = post_user_id;
                    setPostUserID(post_user);

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
        Query sortComment = firebaseFirestore.collection("Posts/" + blog_post_id + "/Comments").orderBy("timestamp", Query.Direction.ASCENDING);
        sortComment.addSnapshotListener(Comments.this, new EventListener<QuerySnapshot>() {
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
                                    if (task.isSuccessful()) {
                                        Log.e("Comment notificaiton","Comment Notification Entered");
                                        Log.e("Comment notificaiton","Comment Notification current user id "+current_user_id);
                                        firebaseFirestore.collection("Users").document(current_user_id).addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                            @Override
                                            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                if (documentSnapshot.exists()) {
                                                    Log.e("Comment notificaiton","Comment Notification current user entered");

                                                    String current_user_name = documentSnapshot.getString("name");
                                                    Log.e("Comment notificaiton","Comment Notification current user name "+current_user_name);

                                                    Map<String, Object> notificaitonMap = new HashMap<>();
                                                    notificaitonMap.put("post_id",blog_post_id);
                                                    notificaitonMap.put("timestamp", FieldValue.serverTimestamp());
                                                    notificaitonMap.put("message", "<b>"+current_user_name+"</b> Commented: "+comment_message);
                                                    firebaseFirestore.collection("Users/" + post_user_id + "/Notification").add(notificaitonMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            if (task.isSuccessful()){
                                                                Log.e("Comment notificaiton","Comment Notification Added");
                                                            } else {
                                                                Log.e("Comment notificaiton","Comment Notification failed");
                                                            }
                                                        }
                                                    });
                                                } else {
                                                    Log.e("Comment notificaiton","Comment Notification document not found");
                                                }
                                            }
                                        });
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

    public void setPostUserID(String user_id) {
        post_user_id = user_id;
    }

    public String getPostUserID() {
        return post_user_id;
    }
}