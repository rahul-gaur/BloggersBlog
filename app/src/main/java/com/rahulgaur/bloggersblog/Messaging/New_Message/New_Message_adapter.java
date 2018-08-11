package com.rahulgaur.bloggersblog.Messaging.New_Message;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rahulgaur.bloggersblog.R;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class New_Message_adapter extends RecyclerView.Adapter<New_Message_adapter.ViewHolder>{

    private ArrayList<MessageItem> list;
    private static Context context;
    private String name, imageURL;

    public New_Message_adapter(ArrayList<MessageItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.new_message_item, parent, false);

        context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        name = list.get(position).getName();
        imageURL = list.get(position).getThumb_image();

        holder.setNameAndImage(name, imageURL);

        final String user_id = list.get(position).new_messageID;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder{

        private View view;
        private CircleImageView imageView;
        private TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);
            view = itemView;

            imageView = view.findViewById(R.id.new_message_item_imageView);
            textView = view.findViewById(R.id.new_message_item_nameTV);
        }

        public void setNameAndImage(String name, String imageURL) {
            try {
                Glide.with(context)
                        .load(imageURL)
                        .into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
            textView.setText(name);
        }
    }
}
