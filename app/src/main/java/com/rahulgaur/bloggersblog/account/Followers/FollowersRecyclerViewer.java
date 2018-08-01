package com.rahulgaur.bloggersblog.account.Followers;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.Constraints;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.account.UserAccount;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.ContentValues.TAG;

public class FollowersRecyclerViewer extends RecyclerView.Adapter<FollowersRecyclerViewer.ViewHolder> {
    private List<FollowersList> followers;
    private static Context context;
    private String name, imageURL;

    public FollowersRecyclerViewer(List<FollowersList> followers) {
        this.followers = followers;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.follow_item_layout, parent, false);

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        name = followers.get(position).getName();
        imageURL = followers.get(position).getThumb_image();

        holder.setNameAndImage(name, imageURL);

        final String user_id = followers.get(position).FollowersID;
        Log.e(TAG, "onBindViewHolder: user_id in followesRecyclerView "+user_id);
        Log.e(TAG, "onBindViewHolder: name in followesRecyclerView "+name);

        holder.nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                Log.e(Constraints.TAG, "sendToUser: blog_post_id in LikeRecycler 1 "+user_id);
                context.startActivity(i);
            }
        });

        holder.follow.setEnabled(false);
        holder.follow.setVisibility(View.INVISIBLE);

        holder.follow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                Log.e(Constraints.TAG, "sendToUser: blog_post_id in LikeRecycler 1 "+user_id);
                context.startActivity(i);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                Log.e(Constraints.TAG, "sendToUser: blog_post_id in LikeRecycler 1 "+user_id);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return followers.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView nameTV;
        private Button follow;
        private View view;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            imageView = view.findViewById(R.id.follow_item_circleView);
            nameTV = view.findViewById(R.id.follow_item_userTV);
            follow = view.findViewById(R.id.follow_item_btn);
        }

        void setNameAndImage(String name, String imageURL) {
            try {
                Glide.with(context).load(imageURL)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            nameTV.setText(name);
        }
    }
}