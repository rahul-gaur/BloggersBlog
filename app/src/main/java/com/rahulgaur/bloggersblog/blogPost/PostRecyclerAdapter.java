package com.rahulgaur.bloggersblog.blogPost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.comment.Comments;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private ArrayList<Post> postList;
    private ArrayList<User> user_list;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;

    private postid pd = new postid();

    public PostRecyclerAdapter(ArrayList<Post> postList, ArrayList<User> user_list) {
        this.postList = postList;
        this.user_list = user_list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_card_layout, parent, false);

        firebaseFirestore = FirebaseFirestore.getInstance();
        context = parent.getContext();
        auth = FirebaseAuth.getInstance();
        return new ViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.setIsRecyclable(false);

        //get blog post id here
        final String blogPostID = postList.get(position).BlogPostID;

        pd.setPostid(blogPostID);

        progressDialog = new ProgressDialog(context);

        //get current user id
        final String currentUserId = auth.getCurrentUser().getUid();

        String desc_data = postList.get(position).getDesc();
        final String user_id = postList.get(position).getUser_id();
        final String thumb_image_url = postList.get(position).getThumb_image_url();
        final String current_user_id = auth.getCurrentUser().getUid();

        //getting post ownership
        firebaseFirestore.collection("Posts")
                .document(blogPostID).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                if (documentSnapshot.exists()) {
                    String postUserID = documentSnapshot.getString("user_id");
                    holder.checkPostOwership(currentUserId, postUserID);
                }
            }
        });

        String user_data = user_list.get(position).getName();
        final String profile_url = user_list.get(position).getImage();
        holder.setProfileImage(profile_url);
        holder.setUserText(user_data);

        //time feature
        Date date = postList.get(position).getTimestamp();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat("dd MMMM yyyy");
        StringBuilder nowMMDDYYYY = new StringBuilder(dateformatMMDDYYYY.format(date));

        holder.setTime(nowMMDDYYYY);
        holder.setPostImage(thumb_image_url);
        holder.setDescText(desc_data);

        //get likes count
        firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        if (!documentSnapshots.isEmpty()) {
                            //some likes
                            int count = documentSnapshots.size();
                            holder.setLikeView(count);
                        } else {
                            //no likes
                            holder.setLikeView(0);
                        }

                    }
                });

        //get comment count
        firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()) {
                            //some comments
                            int count = documentSnapshots.size();
                            holder.setCmntView(count);
                        } else {
                            //no comments
                            holder.setCmntView(0);
                        }
                    }
                });

        //get likes on start
        firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                .document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                if (documentSnapshot.exists()) {
                    holder.likeImage.setImageResource(R.mipmap.like_pink);
                } else {
                    holder.likeImage.setImageResource(R.mipmap.like_grey);
                }
            }
        });

        //menu in card implantation and click listener
        holder.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PopupMenu popupMenu = new PopupMenu(context, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.post_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.post_menu_report_btn:
                                report(current_user_id, user_id, blogPostID);
                                break;
                            case R.id.post_menu_block_btn:
                                block();
                                return true;
                            default:
                                return false;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        //likes feature
        holder.likeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                        .document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (!task.getResult().exists()) {
                                    //if the like does not exists then add like
                                    Map<String, Object> likesMap = new HashMap<>();
                                    likesMap.put("timestamp", FieldValue.serverTimestamp());
                                    firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                                            .document(currentUserId).set(likesMap)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        holder.likeImage.setImageResource(R.mipmap.like_pink);
                                                    }
                                                }
                                            });
                                } else {
                                    //if like exists delete the like
                                    firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                                            .document(currentUserId).delete()
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        holder.likeImage.setImageResource(R.mipmap.like_grey);
                                                    }
                                                }
                                            });
                                }
                            }
                        });
            }
        });

        //delete feature
        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //deleting photos from storage
                progressDialog.setMessage("Deleting Post Please Wait....");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                firebaseFirestore.collection("Posts").document(blogPostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            String post_name = task.getResult().getString("post_name");

                            if (!post_name.isEmpty()) {
                                final FirebaseStorage storageReference = FirebaseStorage.getInstance();
                                StorageReference delfile = storageReference.getReferenceFromUrl(thumb_image_url);
                                delfile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ///the post deleted from the storage
                                        if (task.isSuccessful()) {

                                            firebaseFirestore.collection("Posts").document(blogPostID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        postList.remove(position);
                                                        user_list.remove(position);
                                                        Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                                        progressDialog.dismiss();
                                                        notifyDataSetChanged();
                                                    } else {
                                                        Toast.makeText(context, "Error while deleting post.", Toast.LENGTH_SHORT).show();
                                                        Log.e("deleting post", "error in deleting entries from database " + task.getException().getMessage());
                                                        progressDialog.dismiss();
                                                        //error in deleting entries from database
                                                    }
                                                }
                                            });
                                        } else {
                                            Toast.makeText(context, "Error while deleting post.", Toast.LENGTH_SHORT).show();
                                            progressDialog.dismiss();
                                            Log.e("deleting post", "error deleting thumbnail from storage " + task.getException().getMessage());
                                            //error in thumbnail deletion
                                        }
                                    }
                                });
                            } else {
                                progressDialog.dismiss();
                                Toast.makeText(context, "Error while deleting post.", Toast.LENGTH_SHORT).show();
                                Log.e("deleting post", "post not found " + task.getException().getMessage());
                                //post not found
                            }
                        }
                    }
                });
            }
        });

        //comment feature
        holder.cmntImage.setOnClickListener(new View.OnClickListener()

        {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", blogPostID);
                context.startActivity(i);
            }
        });

    }

    //report feature
    private void report(String current_user_id, String post_user_id, String post_id) {
        /*
        report the user
        if 5 people report the user it will give the user warning
        and if after warning 5 more people report it will ban the user from the app
        */

        firebaseFirestore.collection("Posts").document(post_id).collection("Report").add(current_user_id)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(context, "Reported..", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.e("report", "error: " + task.getException().getMessage());
                        }
                    }
                });
    }

    //block feature
    private void block() {
        //block the user
        Toast.makeText(context, "Block Clicked", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return user_list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private MenuItem reportBtn, blockBtn;
        private TextView descView, userView, dateView, likeView, cmntView;
        private CircleImageView profile;
        private ImageView imageView, likeImage, deleteImage, cmntImage, menuBtn;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            menuBtn = mView.findViewById(R.id.card_menu);
            deleteImage = mView.findViewById(R.id.delete_imageView);
            likeImage = mView.findViewById(R.id.like_imageView);
            cmntImage = mView.findViewById(R.id.cmnt_imageView);
            reportBtn = mView.findViewById(R.id.post_menu_report_btn);
            blockBtn = mView.findViewById(R.id.post_menu_block_btn);
        }

        @SuppressLint("SetTextI18n")
        void setLikeView(final int likeCount) {
            likeView = mView.findViewById(R.id.like_tv);
            likeView.setText(likeCount + "");
        }

        @SuppressLint("SetTextI18n")
        void setCmntView(final int cmntCount) {
            cmntView = mView.findViewById(R.id.cmnt_tv);
            cmntView.setText(cmntCount + "");
        }

        void checkPostOwership(String currentUser, String postUser) {
            if (currentUser.equals(postUser)) {
                deleteImage.setVisibility(View.VISIBLE);
                menuBtn.setVisibility(View.INVISIBLE);
                menuBtn.setEnabled(false);
            } else {
                deleteImage.setEnabled(false);
                deleteImage.setVisibility(View.INVISIBLE);
            }
        }

        void setDescText(String text) {
            descView = mView.findViewById(R.id.desc_tv);
            descView.setText(text);
        }

        @SuppressLint("CheckResult")
        void setPostImage(String downloadUri) {
            imageView = mView.findViewById(R.id.post_imageView);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.ic_launcher_background);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(downloadUri).into(imageView);
        }

        @SuppressLint("CheckResult")
        void setProfileImage(String downloadUri) {
            profile = mView.findViewById(R.id.profile_view);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.default_usr);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(downloadUri).into(profile);
        }

        void setUserText(String text) {
            userView = mView.findViewById(R.id.username_tv);
            userView.setText(text);
        }

        void setTime(StringBuilder date) {
            dateView = mView.findViewById(R.id.date_tv);
            dateView.setText(date);
        }

    }

    public void setFilter(ArrayList<User> newUser) {
        user_list = new ArrayList<>();
        user_list.addAll(newUser);
        Log.v("madapter", "setFilter called  ");
        notifyDataSetChanged();
    }
}