package com.rahulgaur.bloggersblog.account;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.comment.Comments;

import java.util.ArrayList;

public class GridViewAdapter extends ArrayAdapter {

    private Context context;
    private ArrayList<GridViewList> objects;


    GridViewAdapter(@NonNull Context context, int resource, ArrayList<GridViewList> list) {
        super(context, resource, list);
        this.context = context;
        objects = list;
    }

    public class Holder {
        ImageView post_image;
        ProgressBar progressBar;

    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Holder holder;
        View row = convertView;
        final String postID;
        postID = objects.get(position).BlogPostID;
        final FirebaseFirestore firebaseFirestore;
        firebaseFirestore = FirebaseFirestore.getInstance();

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.grid_view_item, parent, false);
            holder = new Holder();
            holder.post_image = row.findViewById(R.id.grid_item_imageView);
            holder.progressBar = row.findViewById(R.id.grid_item_progressBar);
            holder.progressBar.setVisibility(View.VISIBLE);
            row.setTag(holder);
        } else {
            holder = (Holder) convertView.getTag();
        }

        final GridViewList gridViewList = new GridViewList();
        final String post_id = objects.get(position).getBlogPostID();

        try {
            Glide.with(context).load(objects.get(position).getImageURL())
                    .into(holder.post_image);
        } catch (Exception e) {
            e.printStackTrace();
        }
        holder.progressBar.setVisibility(View.INVISIBLE);
        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        Log.e("on click ", "blog post id " + post_id);
                        sendToComments(post_id);
                    }
                });
            }

            private void sendToComments(String post_id) {
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", post_id);
                context.startActivity(i);
            }
        });

        return row;
    }
}
