package com.rahulgaur.bloggersblog.account;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.blogPost.postid;

import java.util.ArrayList;

/**
 * @author Rahul Gaur
 */

public class UserAccount extends AppCompatActivity {

    private String post_user_id;
    private String name;
    private String imageURL;
    private ImageView profileImage;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private SharedPref sharedPref;
    private android.support.v7.widget.Toolbar toolbar;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressDialog progressDialog;
    private ArrayList<GridViewList> postList = new ArrayList<>();
    private RecyclerView recyclerView;
    private GridViewList gridViewList;

    private UserAdapter userAdapter;

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

        postid pd = new postid();
        final String post_id = pd.getPostid();
        post_user_id = getIntent().getStringExtra("post_user_id");
        Log.e("post_user_id","user id from intent "+post_user_id);

        toolbar = findViewById(R.id.user_account_frag_toolbar);
        setSupportActionBar(toolbar);
        recyclerView = findViewById(R.id.user_account_recyclerView);

        firebaseAuth = FirebaseAuth.getInstance();

        final String current_userID = firebaseAuth.getCurrentUser().getUid();

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

        firebaseFirestore = FirebaseFirestore.getInstance();

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

        // TODO: 09-07-2018 uncomment this and also fix errors
        //getting posts from the database
        firebaseFirestore.collection("Posts/" + post_id).addSnapshotListener(this, new EventListener<QuerySnapshot>() {
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
                                    postList.add(new GridViewList(post_thumb_url,blog_post_id));
                                    gridViewList.setBlogPostID(blog_post_id);
                                    Log.e("userAccount", "post id " + blog_post_id+" post url "+post_thumb_url);
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

    public void setProfileImage(String profile) {
        Glide.with(this)
                .load(profile)
                .into(profileImage);
    }
}