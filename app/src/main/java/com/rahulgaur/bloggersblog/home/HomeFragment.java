package com.rahulgaur.bloggersblog.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.Settings;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.blogPost.Post;
import com.rahulgaur.bloggersblog.blogPost.User;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 *
 * @author Rahul Gaur
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Post> postList;
    private ArrayList<User> userList;

    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter;
    private FirebaseAuth auth;
    private SwipeRefreshLayout swipeRefreshLayout;

    private SharedPref sharedPref;
    private android.support.v7.widget.Toolbar toolbar;

    public HomeFragment() {
        // Required empty public constructor
    }


    @SuppressLint("RestrictedApi")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = new SharedPref(getContext());
        if (sharedPref.loadNightModeState()) {
            getActivity().setTheme(R.style.darkTheme);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postList = new ArrayList<>();
        userList = new ArrayList<>();
        recyclerView = view.findViewById(R.id.frag_home_recyclerView);

        postRecyclerAdapter = new PostRecyclerAdapter(postList, userList);

        swipeRefreshLayout = view.findViewById(R.id.frag_home_swipeRefresh);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postRecyclerAdapter);

        auth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (auth.getCurrentUser() != null) {

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(getContext(), "Refreshed..", Toast.LENGTH_SHORT).show();
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(postRecyclerAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }

            });

            toolbar = view.findViewById(R.id.home_frag_toolbar);
            toolbar.setTitle("Blogger's Blog");
            ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);
            setHasOptionsMenu(true);

            //adding posts
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(final QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    try {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String blogPosTID = doc.getDocument().getId();
                                    final Post post = doc.getDocument().toObject(Post.class).withID(blogPosTID);

                                    String blogUserID = doc.getDocument().getString("user_id");
                                    firebaseFirestore.collection("Users").document(blogUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            try {
                                                if (task.isSuccessful()) {
                                                    User user = task.getResult().toObject(User.class);
                                                    userList.add(user);
                                                    postList.add(post);
                                                    postRecyclerAdapter.notifyDataSetChanged();
                                                } else {
                                                    //some error
                                                }
                                            } catch (Exception e1) {
                                                e1.printStackTrace();
                                            }
                                        }
                                    });

                                }
                            }
                        } else {
                            Toast.makeText(getContext(), "No posts..", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e1) {
                        e1.printStackTrace();
                    }
                }
            });
        }
        return view;
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