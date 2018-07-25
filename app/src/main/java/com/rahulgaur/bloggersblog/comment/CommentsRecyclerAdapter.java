package com.rahulgaur.bloggersblog.comment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.account.UserAccount;
import com.rahulgaur.bloggersblog.blogPost.User;

import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.support.constraint.Constraints.TAG;

/**
 * @author Rahul Gaur
 */

public class CommentsRecyclerAdapter extends RecyclerView.Adapter<CommentsRecyclerAdapter.ViewHolder> {

    private List<CommentList> commentList;
    @SuppressLint("StaticFieldLeak")
    private static Context context;

    private String blogPostId;
    private String postID;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    CommentsRecyclerAdapter(List<CommentList> commentList, String blogPostId) {
        this.commentList = commentList;
        this.blogPostId = blogPostId;
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
    public void onBindViewHolder(@NonNull final ViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.setIsRecyclable(false);
        final String message = commentList.get(position).getMessage();
        final String userId = commentList.get(position).getUser_id();
        String currentUserId = Objects.requireNonNull(auth.getCurrentUser()).getUid();

        String postUserId = commentList.get(position).getPost_user_id();

        // String blogPostId = postList.get(position).BlogPostID;
        final String commentId = commentList.get(position).CommentID;

        holder.commentOwnership(userId, currentUserId, postUserId);

        postID = commentList.get(position).getPostID();

        //getting username and profile of the current user
        firebaseFirestore.collection("Users")
                .document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                String userData = task.getResult().getString("name");
                                String profileUrl = task.getResult().getString("thumb_image");
                                holder.setProfile(profileUrl);
                                holder.setNameText(userData);
                            } else {
                                String msg = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(context, "Error: " + msg, Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        holder.setMessageText(message);

        //comment delete feature
        holder.commentDeleteImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.commentDeleteImageView.setEnabled(false);
                Log.e("commentid delete", ""+commentId);
                Log.e("blogpostid delete", ""+postID);
                firebaseFirestore.collection("Posts/").document(postID).collection("Comments").document(commentId).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override

                    public void onComplete(@NonNull Task<Void> task) {
                        try {
                            if (task.isSuccessful()) {
                                try {
                                    commentList.remove(position);
                                } catch (IndexOutOfBoundsException e) {
                                    Log.e("comment remove", "Comment remove exception " + e.getMessage());
                                }
                                notifyDataSetChanged();
                                holder.commentDeleteImageView.setEnabled(true);
                                Toast.makeText(context, "Deleted", Toast.LENGTH_SHORT).show();
                            } else {
                                holder.commentDeleteImageView.setEnabled(true);
                                Toast.makeText(context, "some error", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        holder.profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToUser(userId);
            }
        });

        holder.name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToUser(userId);
            }
        });

        holder.message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToUser(userId);
            }
        });

    }

    private void sendToUser(String user_id) {
        Intent i = new Intent(context, UserAccount.class);
        i.putExtra("post_user_id", user_id);
        Log.e(TAG, "sendToUser: blog_post_id in commentRecyclerAdapter "+user_id);
        context.startActivity(i);
    }


    @Override
    public int getItemCount() {
        return commentList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView name, message;
        private CircleImageView profile;
        private ImageView commentDeleteImageView;

        ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
            profile = mView.findViewById(R.id.cmnt_profileView);
            name = mView.findViewById(R.id.cmnt_nameTV);
            message = mView.findViewById(R.id.cmnt_messageTV);

        }

        void setNameText(String nameText) {
            name.setText(nameText);
        }

        void setMessageText(String messageText) {
            message.setText(messageText);
        }

        @SuppressLint("CheckResult")
        void setProfile(String profileUri) {
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.default_usr);
            try {
                Glide.with(context).applyDefaultRequestOptions(placeholder).load(profileUri).into(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void commentOwnership(String commentUser, String currentUserId, String postUserId) {
            commentDeleteImageView = mView.findViewById(R.id.cmnt_item_dlt_imgView);
            if (currentUserId.equals(postUserId)) {
                Log.e("comment delete","currentUserId "+currentUserId+" currentUserID "+currentUserId+" 1nd if");
                commentDeleteImageView.setEnabled(true);
                commentDeleteImageView.setVisibility(View.VISIBLE);
            } else if (commentUser.equals(currentUserId)) {
                Log.e("comment delete","CommentUser "+commentUser+" currentUserID "+currentUserId+" 2nd if else");
                commentDeleteImageView.setEnabled(true);
                commentDeleteImageView.setVisibility(View.VISIBLE);
            } else if (!commentUser.equals(currentUserId) || !currentUserId.equals(postUserId)){
                Log.e("comment delete","CommentUser "+commentUser+" currentUserID "+currentUserId+" postUserId "+postUserId+" 3nd if else");
                commentDeleteImageView.setEnabled(false);
                commentDeleteImageView.setVisibility(View.INVISIBLE);
            }
        }
    }
}