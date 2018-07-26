package com.rahulgaur.bloggersblog.notification;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.comment.Comments;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class NotificationRecyclerAdapter extends RecyclerView.Adapter<NotificationRecyclerAdapter.ViewHolder> {

    private List<NotificationList> notificationLists;
    private static Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String noti_message;
    private String noti_postID;
    private Date timestamp;

    public NotificationRecyclerAdapter(List<NotificationList> notificationLists) {
        this.notificationLists = notificationLists;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.notification_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        //holder.setIsRecyclable(false);
        noti_message = notificationLists.get(position).getMessage();
        noti_postID = notificationLists.get(position).getPost_id();
        timestamp = notificationLists.get(position).getTimestamp();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat("dd MMMM yy");
        final StringBuilder nowMMDDYYYY = new StringBuilder(dateformatMMDDYYYY.format(timestamp));

        Log.e("Notification", "message " + noti_message);
        Log.e("Notification", "post id " + noti_postID);
        Log.e("Notification", "timestamp " + nowMMDDYYYY);

        holder.setTimeStamp(nowMMDDYYYY);

        final String notificationID = notificationLists.get(position).NotificationID;

        //getting post image
        firebaseFirestore.collection("Posts").document(noti_postID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                try {
                    if (task.isSuccessful()) {
                        try {
                            String postURL = task.getResult().getString("thumb_image_url");
                            Log.e("Notification", "post_id " + noti_postID);
                            Log.e("Notification", "image url " + postURL);
                            holder.setPostImage(postURL);
                        } catch (Exception e) {
                            Log.e("Notification getting", "Exception " + e.getMessage());
                        }
                    } else {
                        Log.e("Notification", "else image url");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        final String current_user_id = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users/" + current_user_id + "/Notification").document(notificationID).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        try {
                            if (task.isSuccessful()) {
                                try {
                                    noti_message = task.getResult().getString("message");
                                    holder.setTextView(noti_message);
                                    Log.e("Notification", "current user id " + current_user_id);
                                    Log.e("Notification", "message " + noti_message);
                                } catch (Exception e) {
                                    Log.e("NotiRecycler", "Exception " + e.getMessage());
                                }
                            } else {
                                Log.e("Notification", "else message ");
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });

        final String notification_post_id = notificationLists.get(position).getPost_id().trim();
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Notification", "OnClick position" + notification_post_id);
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", notification_post_id);
                context.startActivity(i);
            }
        });
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Notification", "OnClick position" + notification_post_id);
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", notification_post_id);
                context.startActivity(i);
            }
        });
        holder.timeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.e("Notification", "OnClick position" + notification_post_id);
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", notification_post_id);
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return notificationLists.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView, timeView;
        private View mView;
        private ProgressDialog progressDialog;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imageView = mView.findViewById(R.id.noti_item_imageView);
            textView = mView.findViewById(R.id.noti_item_textView);
            timeView = mView.findViewById(R.id.noti_item_timeStamp);
            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Loading Please Wait..");
            //progressDialog.show();
            progressDialog.setCancelable(false);
        }

        @SuppressLint("CheckResult")
        void setPostImage(String imageURL) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_launcher_background);
            try {
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(imageURL).into(imageView);
            } catch (Exception e1) {
                Log.e("Notification", "Glide exceptioin " + e1.getMessage());
            }
            //progressDialog.dismiss();
        }

        void setTimeStamp(StringBuilder time){
            timeView.setText(time);
        }

        void setTextView(String message) {
            textView.setText(Html.fromHtml(message));
        }
    }
}