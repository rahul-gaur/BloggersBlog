package com.rahulgaur.bloggersblog.account;

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
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.comment.Comments;

import java.util.ArrayList;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {

    private ArrayList<GridViewList> postList;
    private FirebaseAuth auth;
    private FirebaseFirestore firebaseFirestore;
    private String postURL;
    private String postID;
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    private String current_userID;

    UserAdapter(ArrayList<GridViewList> postList) {
        this.postList = postList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_profile_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserAdapter.ViewHolder holder, final int position) {
        postURL = postList.get(position).getImageURL();
        postID = postList.get(position).getBlogPostID();
        Log.e("userAccount", "post_id " + postID);
        Log.e("userAccount", "postURL " + postURL);

        holder.setPostImage(postURL);
        final String post_id = postList.get(position).getBlogPostID();
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("userAccount", "post id onClick " + post_id);
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", post_id);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private ImageView imageView;
        private ProgressBar progressBar;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imageView = mView.findViewById(R.id.user_item_imageView);
            progressBar = mView.findViewById(R.id.user_item_progressBar);
        }

        @SuppressLint("CheckResult")
        void setPostImage(String image) {
            RequestOptions placeHolder = new RequestOptions();
            placeHolder.placeholder(R.drawable.ic_launcher_background);
            Glide.with(context)
                    .applyDefaultRequestOptions(placeHolder)
                    .load(image).into(imageView);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}