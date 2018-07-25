package com.rahulgaur.bloggersblog.comment.like_sheet;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.constraint.Constraints;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.account.UserAccount;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.ContentValues.TAG;

public class LikeRecyclerAdapter extends RecyclerView.Adapter<LikeRecyclerAdapter.ViewHolder> {

    private ArrayList<LikeList> likes;
    private static Context context;
    private String name, imageURL;
    private Date timestamp;

    public LikeRecyclerAdapter(ArrayList<LikeList> likes) {
        this.likes = likes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.like_sheet_item,parent,false);

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        name = likes.get(position).getName();
        imageURL = likes.get(position).getThumb_image();
        timestamp = likes.get(position).getTimestamp();

        Log.e(TAG, "onEvent: name "+name);

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat("dd MMMM yyyy");
        final StringBuilder nowMMDDYYYY = new StringBuilder(dateformatMMDDYYYY.format(timestamp));

        holder.setTime(nowMMDDYYYY);

        final String user_id = likes.get(position).LikeID;

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                Log.e(Constraints.TAG, "sendToUser: blog_post_id in LikeRecycler 1 "+user_id);
                context.startActivity(i);
            }
        });

        holder.nameTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                Log.e(Constraints.TAG, "sendToUser: blog_post_id in LikeRecycler"+user_id);
                context.startActivity(i);
            }
        });

        holder.setLikeNameAndImage(name,imageURL);
    }

    @Override
    public int getItemCount() {
        return likes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView imageView;
        private TextView nameTextView, timeTextView;
        private View view;

        ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            imageView = view.findViewById(R.id.like_imageView);
            nameTextView = view.findViewById(R.id.like_name_tv);
            timeTextView = view.findViewById(R.id.like_time_tv);
        }

        void setLikeNameAndImage(String name, String imageURL) {
            nameTextView.setText(name);
            try {
                Glide.with(context).load(imageURL).into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void setTime(StringBuilder nowMMDDYYYY) {
            timeTextView.setText(nowMMDDYYYY);
        }
    }
}
