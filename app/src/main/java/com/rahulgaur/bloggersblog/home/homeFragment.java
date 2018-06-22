package com.rahulgaur.bloggersblog.home;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.blogPost.Post;
import com.rahulgaur.bloggersblog.blogPost.PostRecyclerAdapter;
import com.rahulgaur.bloggersblog.blogPost.User;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class homeFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<Post> postList;
    private List<User> user_list;

    private FirebaseFirestore firebaseFirestore;
    private PostRecyclerAdapter postRecyclerAdapter;
    private FirebaseAuth auth;

    private Boolean isfirstPageLoad = true;

    private DocumentSnapshot lastVisible;

    public homeFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        postList = new ArrayList<>();
        user_list = new ArrayList<>();
        recyclerView = view.findViewById(R.id.frag_home_recyclerView);

        postRecyclerAdapter = new PostRecyclerAdapter(postList, user_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(postRecyclerAdapter);

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

            //adding 5 posts on create
            Query firstQuery = firebaseFirestore.collection("Posts").orderBy("timestamp", Query.Direction.DESCENDING).limit(5);
            firstQuery.addSnapshotListener(getActivity(), new EventListener<QuerySnapshot>() {

                @Override
                public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                    if (isfirstPageLoad){
                        lastVisible = documentSnapshots.getDocuments()
                                .get(documentSnapshots.size() -1);
                        postList.clear();
                        user_list.clear();
                    }

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

                                        if (isfirstPageLoad){
                                            user_list.add(user);
                                            postList.add(post);
                                        } else {
                                            user_list.add(0,user);
                                            postList.add(0,post);
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
                                         user_list.add(user);
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
}