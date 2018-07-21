package com.rahulgaur.bloggersblog.Search;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.account.UserAccount;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class SearchRecyclerAdapter extends RecyclerView.Adapter<SearchRecyclerAdapter.ViewHolder> {

    private List<SearchList> searchLists;
    private static Context context;

    SearchRecyclerAdapter(List<SearchList> searchLists) {
        this.searchLists = searchLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item, parent, false);

        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final String name = searchLists.get(position).getName();
        final String imageURL = searchLists.get(position).getThumb_image();
        final String user_id = searchLists.get(position).SearchID;

        Log.e("SearchAdapter", "user id " + user_id);

        holder.setName(name);
        holder.setImageURL(imageURL);

        holder.nameTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                context.startActivity(i);
            }
        });

        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(context, UserAccount.class);
                i.putExtra("post_user_id", user_id);
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return searchLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView nameTV;
        private CircleImageView imageView;

        public ViewHolder(View itemView) {
            super(itemView);

            mView = itemView;
        }

        public void setName(String name) {
            nameTV = mView.findViewById(R.id.searchItem_textView);
            nameTV.setText(name);
        }

        public void setImageURL(String imageURL) {
            imageView = mView.findViewById(R.id.searchItem_imageView);
            try {
                Glide.with(context).load(imageURL).into(imageView);
            } catch (Exception e) {
                Log.e("SearchRecycler", "exception in glide " + e.getMessage());
            }
        }
    }
}
