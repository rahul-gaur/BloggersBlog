package com.rahulgaur.bloggersblog.home;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.ThemeAndSettings.DayNightTheme;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.Settings;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.blogPost.Post;
import com.rahulgaur.bloggersblog.blogPost.PostRecyclerAdapter;
import com.rahulgaur.bloggersblog.blogPost.User;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * A simple {@link Fragment} subclass.
 * @author Rahul Gaur
 */
public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private ArrayList<Post> postList;
    private ArrayList<User> userList;

    private DayNightTheme dayNightTheme = new DayNightTheme();

    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter;
    private FirebaseAuth auth;

    private Boolean isfirstPageLoad = true;

    private DocumentSnapshot lastVisible;
    private SwipeRefreshLayout swipeRefreshLayout;

    public HomeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
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

        android.support.v7.widget.Toolbar toolbar = view.findViewById(R.id.home_frag_toolbar);

        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);

        ((AppCompatActivity)getActivity()).getSupportActionBar().setTitle("Blogger's Blog");
        setHasOptionsMenu(true);

        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() != null) {
            firebaseFirestore = FirebaseFirestore.getInstance();

            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    Boolean bottom = !recyclerView.canScrollVertically(1);

                    if (bottom){
                        nextQuery();
                    }
                }
            });

            swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                @Override
                public void onRefresh() {
                    Toast.makeText(getContext(), "Refreshed..", Toast.LENGTH_SHORT).show();
                    recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
                    recyclerView.setAdapter(postRecyclerAdapter);
                    swipeRefreshLayout.setRefreshing(false);
                }

            });

            //adding 5 posts on create
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(5);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                    if (!documentSnapshots.isEmpty()) {

                        if (isfirstPageLoad) {
                            lastVisible = documentSnapshots.getDocuments()
                                    .get(documentSnapshots.size() - 1);
                            postList.clear();
                            userList.clear();
                        }

                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String blogPosTID = doc.getDocument().getId();
                                final Post post = doc.getDocument().toObject(Post.class).withID(blogPosTID);

                                String blogUserID = doc.getDocument().getString("user_id");
                                firebaseFirestore.collection("Users").document(blogUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if (task.isSuccessful()) {
                                            User user = task.getResult().toObject(User.class);

                                            if (isfirstPageLoad) {
                                                userList.add(user);
                                                postList.add(post);
                                            } else {
                                                userList.add(0, user);
                                                postList.add(0, post);
                                            }
                                            postRecyclerAdapter.notifyDataSetChanged();
                                        } else {
                                            //some error
                                        }
                                    }
                                });

                            }

                            isfirstPageLoad = false;
                        }
                    } else {
                        Toast.makeText(getContext(), "No posts..", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return view;
    }

    //adding 5 more posts
    public void nextQuery(){
        Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).startAfter(lastVisible).limit(5);

        firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {

            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()) {
                    lastVisible = documentSnapshots.getDocuments()
                            .get(documentSnapshots.size() - 1);

                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                        if (doc.getType() == DocumentChange.Type.ADDED) {
                            String blogPosTID = doc.getDocument().getId();
                            final Post post = doc.getDocument().toObject(Post.class).withID(blogPosTID);


                            String blogUserID = doc.getDocument().getString("user_id");
                            firebaseFirestore.collection("Users").document(blogUserID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                    if (task.isSuccessful()){
                                        User user = task.getResult().toObject(User.class);
                                         userList.add(user);
                                         postList.add(post);
                                        postRecyclerAdapter.notifyDataSetChanged();
                                    } else {
                                        //some error
                                    }
                                }
                            });
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "No more posts..", Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.toolbar_menu, menu);

        MenuItem mSearch = menu.findItem(R.id.app_bar_search);
        android.widget.SearchView searchView = (android.widget.SearchView) mSearch.getActionView();

        searchView.setQueryHint("Settings");

        searchView.setOnQueryTextListener(new android.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                newText = newText.toLowerCase();

                ArrayList<User> newUser = new ArrayList<>();

                for (User user : userList) {
                    String fname = user.getName().toLowerCase();
                    Log.v("onQueryTextChange", ""+fname );
                    if (fname.contains(newText)) {

                        newUser.add(user);

                    }
                    postRecyclerAdapter.setFilter(newUser);

                }
                return true;

            }
        });



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

    public void nightMode(String mode) {
        if (mode.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void settings() {
        Intent i = new Intent(getContext(),Settings.class);
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