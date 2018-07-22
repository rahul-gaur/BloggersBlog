package com.rahulgaur.bloggersblog.account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
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
import com.rahulgaur.bloggersblog.comment.Comments;

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private ImageView profileImageView;
    private String current_userID;
    private String imageURL;
    private String post_id;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String username = "";
    private SharedPref sharedPref;
    private GridViewList gridViewList;

    ArrayList<GridViewList> postList = new ArrayList<>();

    private GridViewAdapter gridViewAdapter;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = new SharedPref(getContext());
        if (sharedPref.loadNightModeState()) {
            getActivity().setTheme(R.style.darkTheme);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        final Toolbar account_toolbar = view.findViewById(R.id.account_frag_toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(account_toolbar);
        current_userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseFirestore.collection("Users")
                .document(current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.getResult().exists()) {
                        imageURL = task.getResult().getString("thumb_image");
                        username = task.getResult().getString("name");
                        setProfile(imageURL);
                        setToolbarName(username);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            private void setToolbarName(String username) {
                account_toolbar.setTitle(username);
            }
        });

        final GridView gridView = view.findViewById(R.id.account_postGridView);
        profileImageView = view.findViewById(R.id.user_account_profileImage);

        postid pd = new postid();

        swipeRefreshLayout = view.findViewById(R.id.frag_account_swipeRefresh);

        post_id = pd.getPostid();

        gridViewList = new GridViewList();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Toast.makeText(getContext(), "Refreshed..", Toast.LENGTH_SHORT).show();
                gridView.setAdapter(gridViewAdapter);
                swipeRefreshLayout.setRefreshing(false);
            }

        });

        gridViewList = new GridViewList();


        gridViewAdapter = new GridViewAdapter(Objects.requireNonNull(getActivity()), R.layout.grid_view_item, postList);

        auth = FirebaseAuth.getInstance();

        current_userID = auth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();


        //getting posts from the database
        firebaseFirestore.collection("Posts/" + post_id).addSnapshotListener((Activity) Objects.requireNonNull(getContext()), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String post_user_id = doc.getDocument().getString("user_id");
                                String post_thumb_url = doc.getDocument().getString("thumb_image_url");

                                String blog_post_id = doc.getDocument().getId();

                                if (post_user_id.equals(current_userID)) {
                                    postList.add(new GridViewList(post_thumb_url, blog_post_id));
                                    gridViewList.setBlogPostID(post_id);
                                    Log.e("accountFragnent", "post_id " + blog_post_id);
                                    gridViewAdapter.notifyDataSetChanged();
                                }

                            }
                        }
                    } else {
                        //some error
                        Log.e("Account Fragment", "error in user_id and image url");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        gridView.setAdapter(gridViewAdapter);

        return view;
    }

    @SuppressLint("CheckResult")
    public void setProfile(String profile) {
        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.default_usr);
        try {
            Glide.with(Objects.requireNonNull(getActivity())).applyDefaultRequestOptions(placeholder)
                    .load(profile).into(profileImageView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
