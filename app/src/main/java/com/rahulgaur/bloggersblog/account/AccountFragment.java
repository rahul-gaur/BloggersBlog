package com.rahulgaur.bloggersblog.account;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.Fragment;
import android.support.v4.widget.NestedScrollView;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
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
import com.rahulgaur.bloggersblog.ThemeAndSettings.Settings;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.account.Followers.FollowersList;
import com.rahulgaur.bloggersblog.account.Followers.FollowersRecyclerViewer;
import com.rahulgaur.bloggersblog.blogPost.postid;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static io.fabric.sdk.android.Fabric.TAG;

/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private CircleImageView profileImageView;
    private ImageView profile_background;
    private String current_userID;
    private String imageURL;
    private String post_id;
    private SwipeRefreshLayout swipeRefreshLayout;
    private String username = "";
    private SharedPref sharedPref;
    private GridViewList gridViewList;
    private FirebaseAuth auth;
    private Toolbar account_toolbar;
    private int post_count = 0;
    private TextView post_countTV;
    private List<FollowersList> followersLists;
    private FollowersRecyclerViewer followersRecyclerAdapter;
    ArrayList<GridViewList> postList = new ArrayList<>();

    private GridViewAdapter gridViewAdapter;

    public AccountFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = new SharedPref(getContext());
        if (sharedPref.loadNightModeState()) {
            getActivity().setTheme(R.style.darkTheme);
            setTheme(R.drawable.profile_grad_black);
        } else {
            getActivity().setTheme(R.style.AppTheme);
            setTheme(R.drawable.profile_grad);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        auth = FirebaseAuth.getInstance();

        account_toolbar = view.findViewById(R.id.account_frag_toolbar);
        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(account_toolbar);
        setHasOptionsMenu(true);

        profile_background = view.findViewById(R.id.profile_frag_Background);
        post_countTV = view.findViewById(R.id.accFrag_user_post_count);

        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        if (sharedPref.loadNightModeState()) {
            setTheme(R.drawable.profile_grad_black);
        } else {
            setTheme(R.drawable.profile_grad);
        }

        final LinearLayout followersLayout = view.findViewById(R.id.userFrag_followers_layout);
        final LinearLayout followingLayout = view.findViewById(R.id.userFrag_following_layout);
        final TextView followersTV = view.findViewById(R.id.accFrag_user_followers);
        final TextView followingTV = view.findViewById(R.id.accFrag_user_following);
        NestedScrollView followSheetLayout = view.findViewById(R.id.userFrag_sheet_layout);
        final BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(followSheetLayout);
        RecyclerView followRecyclerViw = view.findViewById(R.id.userFrag_sheet_recyclerView);
        final TextView followTV = view.findViewById(R.id.userFrag_sheet_textView);
        followersLists = new ArrayList<>();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        followRecyclerViw.setLayoutManager(new LinearLayoutManager(getContext()));
        followersRecyclerAdapter = new FollowersRecyclerViewer(followersLists);
        followRecyclerViw.setAdapter(followersRecyclerAdapter);

        current_userID = Objects.requireNonNull(auth.getCurrentUser()).getUid();
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


        //followers count
        firebaseFirestore.collection("Users/" + current_userID + "/Followers").addSnapshotListener((Activity) getContext(), new EventListener<QuerySnapshot>() {
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
        firebaseFirestore.collection("Users/" + current_userID + "/Following").addSnapshotListener((Activity) getContext(), new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        int count = documentSnapshots.size();
                        followingTV.setText("" + count);
                    } else {
                        followingTV.setText("0");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });


        final GridView gridView = view.findViewById(R.id.account_postGridView);
        profileImageView = view.findViewById(R.id.user_account_profileImage);

        profileImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getContext(), Account.class);
                startActivity(i);
            }
        });

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

        //getting posts from the database
        firebaseFirestore.collection("Posts/" + post_id).addSnapshotListener((Activity) Objects.requireNonNull(getContext()), new EventListener<QuerySnapshot>() {
            @SuppressLint("SetTextI18n")
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
                                    try {
                                        post_count++;
                                        Log.e(TAG, "onEvent: post count " + post_count);
                                        post_countTV.setText(post_count + "");
                                    } catch (Exception e1) {
                                        Log.e(TAG, "onEvent: Exception while post count");
                                        Log.e(TAG, "onEvent: exception " + e1.getMessage());
                                        e1.printStackTrace();
                                    }
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


        //followers list
        followersLayout.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                followersLists.clear();
                followTV.setText("Followers");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                firebaseFirestore.collection("Users/" + current_userID + "/Followers")
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
                                                    .addSnapshotListener((Activity) getContext(), new EventListener<DocumentSnapshot>() {
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
        followingLayout.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View view) {
                followTV.setText("Following");
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                followersLists.clear();
                firebaseFirestore.collection("Users/" + current_userID + "/Following")
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
                                                    .addSnapshotListener((Activity) getContext(), new EventListener<DocumentSnapshot>() {
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


        gridView.setAdapter(gridViewAdapter);

        return view;
    }

    private void setTheme(int profile) {

        try {
            Glide.with(getContext()).load(profile).into(profile_background);
        } catch (Exception e) {
            e.printStackTrace();
        }
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.toolbar_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile_menu:
                sendToAccount();
                break;
            case R.id.setting_AppBar:
                settings();
                break;
            case R.id.LogOut_app_bar:
                logout();
                return true;
            default:
                return false;
        }
        return false;
    }

    private void settings() {
        Intent i = new Intent(getContext(), Settings.class);
        startActivity(i);
    }

    private void logout() {
        auth.signOut();
        Intent i = new Intent(getContext(), WelcomePage.class);
        startActivity(i);
        Objects.requireNonNull(getActivity()).finish();
    }

    private void sendToAccount() {
        Intent i = new Intent(getContext(), Account.class);
        startActivity(i);
    }
}
