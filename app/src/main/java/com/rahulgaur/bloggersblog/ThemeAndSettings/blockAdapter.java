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
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;

import java.util.List;

import static android.support.constraint.Constraints.TAG;

public class blockAdapter extends RecyclerView.Adapter<blockAdapter.ViewHolder> {

    private List<BlockList> list;
    private static Context context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String user_id;
    private String user_name;
    private String user_profile;
    private String current_user_id;
    private BlockList bl;

    public blockAdapter(List<BlockList> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.setting_block_item, parent, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        context = parent.getContext();

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {
        final String blocked_user_id = list.get(position).BlockID;
        current_user_id = auth.getCurrentUser().getUid();
        Log.e("Setting block", "user_id " + blocked_user_id);
        firebaseFirestore.collection("Users").document(blocked_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    user_name = task.getResult().getString("name");
                    user_profile = task.getResult().getString("thumb_image");
                    Log.e("Setting block", "user_name " + user_name);
                    Log.e("Setting block", "user_id " + user_id);
                    Log.e("Setting block", "blocked_user_id ");
                    holder.setProfileImage(user_profile);
                    holder.setTextView(user_name);
                    notifyDataSetChanged();
                } else {
                    Log.e("Setting block", "else image url");
                }
            }
        });

        holder.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Users").document(blocked_user_id)
                        .delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            list.remove(position);
                            Toast.makeText(context, "User Unblocked", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e(TAG, "onComplete: unable to delete blocked user "+blocked_user_id);
                        }
                    }
                });

            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
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
        void setProfileImage(String url) {
            RequestOptions requestOptions = new RequestOptions();
            requestOptions.placeholder(R.drawable.ic_launcher_background);
            try {
                Glide.with(context)
                        .applyDefaultRequestOptions(requestOptions)
                        .load(url).into(imageView);
            } catch (Exception e){
                Log.e("blockAdapter","Exception glide "+e.getMessage());
            }
        }

        void setTextView(String text) {
            textView.setText(text);
        }
    }
}