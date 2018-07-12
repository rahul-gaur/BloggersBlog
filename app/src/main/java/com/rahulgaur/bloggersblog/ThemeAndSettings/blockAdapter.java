package com.rahulgaur.bloggersblog.ThemeAndSettings;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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

import java.util.List;

public class blockAdapter extends RecyclerView.Adapter<blockAdapter.ViewHolder> {

    private List<BlockList> list;
    private static Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String user_id;
    private String user_name;
    private String user_profile;
    private String current_user_id;

    public blockAdapter(List<BlockList> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_block_item,parent,false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        user_id = list.get(position).getUser_id();
        final String blocked_user_id = list.get(position).BlockID;
        current_user_id = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    user_name = task.getResult().getString("name");
                    user_profile = task.getResult().getString("thumb_image");
                    Log.e("Setting block", "user_name "+user_name);
                    Log.e("Setting block", "user_id "+user_id);
                    Log.e("Setting block", "blocked_user_id "+blocked_user_id);
                    holder.setProfileImage(user_profile);
                    holder.setTextView(user_name);
                    notifyDataSetChanged();
                } else {
                    Log.e("Setting block", "else image url");
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView imageView;
        private TextView textView;
        private Button button;
        private View mView;

        public ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;
            imageView = mView.findViewById(R.id.setting_block_profileView);
            textView = mView.findViewById(R.id.setting_block_profileName);
            button = mView.findViewById(R.id.setting_block_btn);
        }

        @SuppressLint("CheckResult")
        void setProfileImage(String url){
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_launcher_background);
            Glide.with(context)
                    .applyDefaultRequestOptions(requestOptions)
                    .load(url).into(imageView);
        }

        void setTextView(String text){
            textView.setText(text);
        }
    }
}