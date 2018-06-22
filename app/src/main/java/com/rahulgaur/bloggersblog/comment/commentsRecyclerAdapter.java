package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.blogPost.postid;

import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class commentsRecyclerAdapter extends RecyclerView.Adapter<commentsRecyclerAdapter.ViewHolder> {

    private List<commentList> cmntList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    private String blog_post_id;

    private postid pd = new postid();

    commentsRecyclerAdapter(List<commentList> cmntList) {
        this.cmntList = cmntList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cmnt_card_layout, parent, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        final String postID = pd.getPostid();
        final String currentUserId = auth.getCurrentUser().getUid();

        final String blogPostID = cmntList.get(position).BlogPostID;

        String message = cmntList.get(position).getMessage();
        String user_id = cmntList.get(position).getUser_id();
        Date timestamp = cmntList.get(position).getTimestamp();

        //getting username and profile of the current user
        firebaseFirestore.collection("Users")
                .document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String user_data = task.getResult().getString("name");
                            String profile_url = task.getResult().getString("image");
                            holder.setProfile(profile_url);
                            holder.setNameText(user_data);
                        } else {
                            String msg = task.getException().getMessage();
                            Toast.makeText(context, "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        //get timestamp
      /*  if (!(cmntList.get(position).getTimestamp()).equals(null)) {
            Date date = cmntList.get(position).getTimestamp();
            @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat("dd MMMM yyyy");
            StringBuilder nowMMDDYYYY = new StringBuilder(dateformatMMDDYYYY.format(date));
            holder.setTimestamp(nowMMDDYYYY);
        }
*/
        holder.setMessageText(message);

        //get comments
        firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()) {
                            //some comments
                        } else {
                            //no comments
                        }
                    }
                });

        //get comments on start
        firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                .document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    //comments exists
                } else {
                    //comments do not exists
                }
            }
        });

        //comments feature
    }


    @Override
    public int getItemCount() {
        return cmntList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView name, message, timestamp;
        private CircleImageView profile;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        void setNameText(String nameText) {
            name = mView.findViewById(R.id.cmnt_nameTV);
            name.setText(nameText);
        }

        void setMessageText(String messageText) {
            message = mView.findViewById(R.id.cmnt_messageTV);
            message.setText(messageText);
        }
/*
        void setTimestamp(StringBuilder timestampDate) {
        }*/

        void setProfile(String profileUri) {
            profile = mView.findViewById(R.id.cmnt_profileView);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.default_usr);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(profileUri).into(profile);
        }
    }
}
