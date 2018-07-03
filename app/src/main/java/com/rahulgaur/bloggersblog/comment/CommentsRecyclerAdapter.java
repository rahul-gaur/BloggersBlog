package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
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
import com.rahulgaur.bloggersblog.blogPost.Post;
import com.rahulgaur.bloggersblog.blogPost.postid;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    private List<CommentList> cmntList;
    private ArrayList<Post> postList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private String blog_post_id;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    private postid pd = new postid();

    CommentsRecyclerAdapter(List<CommentList> cmntList, String blog_post_id) {
        this.cmntList = cmntList;
        this.blog_post_id = blog_post_id;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cmnt_card_layout, parent, false);

        postList = new ArrayList<>();

        firebaseFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        holder.setIsRecyclable(false);
        String message = cmntList.get(position).getMessage();
        String user_id = cmntList.get(position).getUser_id();
        String current_user_id = auth.getCurrentUser().getUid();

       // String blog_post_id = postList.get(position).BlogPostID;
        
        holder.commentOwership(user_id,current_user_id);

        //getting username and profile of the current user
        firebaseFirestore.collection("Users")
                .document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            String user_data = task.getResult().getString("name");
                            String profile_url = task.getResult().getString("thumb_image");
                            holder.setProfile(profile_url);
                            holder.setNameText(user_data);
                        } else {
                            String msg = task.getException().getMessage();
                            Toast.makeText(context, "Error: " + msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        
        holder.setMessageText(message);

        //comment delete feature
        holder.comment_deleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(context, "You clicked Deleted button", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public int getItemCount() {
        return cmntList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView name, message;
        private CircleImageView profile;
        private ImageView comment_deleteImageView;

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

        void setProfile(String profileUri) {
            profile = mView.findViewById(R.id.cmnt_profileView);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.default_usr);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(profileUri).into(profile);
        }

        private void commentOwership(String comment_user, String current_user_id) {
            comment_deleteImageView = mView.findViewById(R.id.cmnt_item_dlt_imgView);
            if (comment_user.equals(current_user_id)) {
                comment_deleteImageView.setEnabled(true);
                comment_deleteImageView.setVisibility(View.VISIBLE);
            } else {
                comment_deleteImageView.setEnabled(false);
                comment_deleteImageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}
