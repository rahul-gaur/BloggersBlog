package com.rahulgaur.bloggersblog.account;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

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
import com.google.firebase.storage.StorageReference;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.blogPost.User;
import com.rahulgaur.bloggersblog.blogPost.postid;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class AccountFragment extends Fragment {

    private GridView gridView;
    private List<User> userList;

    private com.rahulgaur.bloggersblog.blogPost.postid pd;

    private StorageReference storageReference;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;

    private TextView nameTV;
    private ImageView profileImageView;

    private String current_userID;
    private String username;
    private String imageURL;
    private String post_id;

    GridViewList gridViewList;
    ArrayList<GridViewList> postList = new ArrayList<>();

    private GridViewAdapter gridViewAdapter;

    public AccountFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        userList = new ArrayList<>();
        gridView = view.findViewById(R.id.account_postGridView);
        nameTV = view.findViewById(R.id.account_usernameTV);
        profileImageView = view.findViewById(R.id.account_profileImage);

        pd = new postid();

        post_id = pd.getPostid();

        gridViewList = new GridViewList();

        auth = FirebaseAuth.getInstance();

        current_userID = auth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();

        firebaseFirestore.collection("Users")
                .document(current_userID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.getResult().exists()) {
                    username = task.getResult().getString("name");
                    imageURL = task.getResult().getString("image");

                    setName(username);
                    setProfile(imageURL);
                }
            }
        });

        gridViewAdapter = new GridViewAdapter(getActivity(),R.layout.grid_view_item,postList);

        auth = FirebaseAuth.getInstance();

        current_userID = auth.getCurrentUser().getUid();

        firebaseFirestore = FirebaseFirestore.getInstance();

        //getting posts from the database
        firebaseFirestore.collection("Posts/"+post_id).addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                if (!documentSnapshots.isEmpty()){
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType()==DocumentChange.Type.ADDED){
                            String post_user_id = doc.getDocument().getString("user_id");
                            String post_thumb_url = doc.getDocument().getString("thumb_image_url");

                            if (post_user_id.equals(current_userID)){
                                postList.add(new GridViewList(post_thumb_url));
                                gridViewAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } else {

                }
            }
        });

        gridView.setAdapter(gridViewAdapter);

        return view;
    }

    public void setName(String name) {
        nameTV.setText(name);
    }

    public void setProfile(String profile) {
        RequestOptions placeholder = new RequestOptions();
        placeholder.placeholder(R.drawable.default_usr);
        Glide.with(getActivity()).applyDefaultRequestOptions(placeholder)
                .load(profile).into(profileImageView);
    }
}
