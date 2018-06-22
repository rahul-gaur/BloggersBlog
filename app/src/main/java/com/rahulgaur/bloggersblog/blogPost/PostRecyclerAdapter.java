package com.rahulgaur.bloggersblog.blogPost;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private List<Post> postList;
    private List<User> user_list;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    public String postID = "";

    private String postUserID = null;

    private postid pd = new postid();

    public PostRecyclerAdapter(List<Post> postList, List<User> user_list) {
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

        //get current user id
        final String currentUserId = auth.getCurrentUser().getUid();

        String desc_data = postList.get(position).getDesc();
        final String user_id = postList.get(position).getUser_id();
        final String image_url = postList.get(position).getImage_url();
        final String thumb_image_url = postList.get(position).getThumb_image_url();

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
        String profile_url = user_list.get(position).getImage();
        holder.setProfileImage(profile_url);
        holder.setUserText(user_data);

        //time feature
        Date date = postList.get(position).getTimestamp();
        long millisecond = postList.get(position).getTimestamp().getTime();

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
                firebaseFirestore.collection("Posts").document(blogPostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                        if (task.getResult().exists()) {
                            String post_name = task.getResult().getString("post_name");

                            if (!post_name.isEmpty()) {
                                final FirebaseStorage storageReference = FirebaseStorage.getInstance();
                                StorageReference delfile = storageReference.getReferenceFromUrl(image_url);
                                delfile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ///the post deleted from the storage
                                        if (task.isSuccessful()) {

                                            StorageReference delThumb = storageReference.getReferenceFromUrl(thumb_image_url);
                                            delThumb.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    if (task.isSuccessful()) {
                                                        //thumbnail deleted successful

                                                        //deleting post from database
                                                        firebaseFirestore.collection("Posts").document(blogPostID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    postList.remove(position);
                                                                    user_list.remove(position);
                                                                    Toast.makeText(context, "Post deleted", Toast.LENGTH_SHORT).show();
                                                                } else {
                                                                    Log.e("deleting post", "error in deleting entries from database " + task.getException().getMessage());
                                                                    //error in deleting entries from database
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                        Log.e("deleting post", "error deleting thumbnail from storage " + task.getException().getMessage());
                                                        //error in thumbnail deletion
                                                    }
                                                }
                                            });

                                        } else {
                                            Log.e("deleting post", "error deleting file from storage " + task.getException().getMessage());
                                            //error deleting file from storage
                                        }
                                    }
                                });
                            } else {
                                Log.e("deleting post", "post not found " + task.getException().getMessage());
                                //post not found
                            }
                        }
                    }
                });
            }
        });

        //comment feature
        holder.cmntImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, Comments.class);
                i.putExtra("blog_post_id", blogPostID);
                context.startActivity(i);
            }
        });

    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private View mView;
        private TextView descView, userView, dateView, likeView, cmntView;
        private CircleImageView profile;
        private ImageView imageView, likeImage, deleteImage, cmntImage;

        ViewHolder(View itemView) {
            super(itemView);
            mView = itemView;

            deleteImage = mView.findViewById(R.id.delete_imageView);
            likeImage = mView.findViewById(R.id.like_imageView);
            cmntImage = mView.findViewById(R.id.cmnt_imageView);
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
            } else {
                deleteImage.setVisibility(View.INVISIBLE);
            }
        }

        void setDescText(String text) {
            descView = mView.findViewById(R.id.desc_tv);
            descView.setText(text);
        }

        void setPostImage(String downloadUri) {
            imageView = mView.findViewById(R.id.post_imageView);
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.ic_launcher_background);
            Glide.with(context).applyDefaultRequestOptions(placeholder).load(downloadUri).into(imageView);
        }

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
}