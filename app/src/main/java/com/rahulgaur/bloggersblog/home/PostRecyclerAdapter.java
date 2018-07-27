package com.rahulgaur.bloggersblog.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.ContextThemeWrapper;
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
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.account.UserAccount;
import com.rahulgaur.bloggersblog.blogPost.Post;
import com.rahulgaur.bloggersblog.blogPost.User;
import com.rahulgaur.bloggersblog.blogPost.postid;
import com.rahulgaur.bloggersblog.comment.Comments;
import com.rahulgaur.bloggersblog.notification.Remote.APIService;
import com.rahulgaur.bloggersblog.notification.notificationServices.Common;
import com.rahulgaur.bloggersblog.notification.notificationServices.Data;
import com.rahulgaur.bloggersblog.notification.notificationServices.MyResponse;
import com.rahulgaur.bloggersblog.notification.notificationServices.Sender;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PostRecyclerAdapter extends RecyclerView.Adapter<PostRecyclerAdapter.ViewHolder> {

    private ArrayList<Post> postList;
    private ArrayList<User> user_list;

    @SuppressLint("StaticFieldLeak")
    public static Context context;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ProgressDialog progressDialog;
    private SharedPref sharedPref;
    private String post_user_id;
    private postid pd = new postid();
    private APIService apiService;

    PostRecyclerAdapter(ArrayList<Post> postList, ArrayList<User> user_list) {
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

        apiService = Common.getFCMCLient();

        //get current user id
        final String currentUserId = auth.getCurrentUser().getUid();

        final String desc_data = postList.get(position).getDesc();
        final String user_id = postList.get(position).getUser_id();
        final String thumb_image_url = postList.get(position).getThumb_image_url();
        final String current_user_id = auth.getCurrentUser().getUid();
        final String post_user_token = postList.get(position).getToken();

        //getting post ownership11
        firebaseFirestore.collection("Posts")
                .document(blogPostID).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                try {
                    if (documentSnapshot.exists()) {
                        post_user_id = documentSnapshot.getString("user_id");
                        holder.checkPostOwership(currentUserId, post_user_id);
                    }
                } catch (Exception e1) {
                    Log.e("ownership", "Exception " + e1);
                }
            }
        });

        String user_data = user_list.get(position).getName();
        final String profile_url = user_list.get(position).getImage();
        holder.setProfileImage(profile_url);
        holder.setUserText(user_data);

        //time feature
        Date date = postList.get(position).getTimestamp();

        @SuppressLint("SimpleDateFormat") SimpleDateFormat dateformatMMDDYYYY = new SimpleDateFormat("hh:mma dd MMMM yy");
        final StringBuilder nowMMDDYYYY = new StringBuilder(dateformatMMDDYYYY.format(date));

        //hiding reported posts
        firebaseFirestore.collection("Posts").document(blogPostID).collection("Report").document(current_user_id).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                try {
                    if (documentSnapshot.exists()) {
                        Log.e("reported posts ", "reported post " + blogPostID + " by " + current_user_id);
                        try {
                            holder.mView.setVisibility(View.GONE);
                            postList.remove(position);
                            user_list.remove(position);
                            notifyDataSetChanged();
                        } catch (IndexOutOfBoundsException exception) {
                            Log.e("report hide", " post remove exception " + exception.getMessage());
                        }
                    } else {
                        holder.setTime(nowMMDDYYYY);
                        holder.setPostImage(thumb_image_url);
                        holder.setDescText(desc_data);

                        holder.mView.setVisibility(View.VISIBLE);
                        Log.e("reported posts ", "no post exists");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });
        //hiding blocked posts
        firebaseFirestore.collection("Users/" + current_user_id + "/Block").document(user_id).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                try {
                    if (documentSnapshot.exists()) {
                        String blocked_user = documentSnapshot.getId();
                        Log.e("blocked posts", "blocked posts " + blocked_user + " by " + current_user_id);
                        try {
                            if (blocked_user.equals(post_user_id)) {
                                try {
                                    postList.remove(position);
                                    user_list.remove(position);
                                    notifyDataSetChanged();
                                } catch (IndexOutOfBoundsException e1) {
                                    Log.e("Blocked", "Exception while removing blocked posts " + e1.getMessage());
                                }
                                Log.e("blocked hide", " post removed");
                            } else {
                                Log.e("blocked hide", " post remove else");
                            }
                        } catch (IndexOutOfBoundsException exce) {
                            Log.e("block hide", " post remove exception " + exce.getMessage());
                        }
                    } else {
                        holder.setTime(nowMMDDYYYY);
                        holder.setPostImage(thumb_image_url);
                        holder.setDescText(desc_data);

                        Log.e("Blocked posts ", "no blocked posts");
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //get likes count
        firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                        try {
                            if (!documentSnapshots.isEmpty()) {
                                //some likes
                                int count = documentSnapshots.size();
                                holder.setLikeView(count);
                            } else {
                                //no likes
                                holder.setLikeView(0);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }

                    }
                });

        //get comment count
        firebaseFirestore.collection("Posts/" + blogPostID + "/Comments")
                .addSnapshotListener((Activity) context, new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        try {
                            if (!documentSnapshots.isEmpty()) {
                                //some comments
                                int count = documentSnapshots.size();
                                holder.setCmntView(count);
                            } else {
                                //no comments
                                holder.setCmntView(0);
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });

        //get likes on start
        firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                .document(currentUserId).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {

                try {
                    if (documentSnapshot.exists()) {
                        holder.likeImage.setImageResource(R.mipmap.like_pink);
                    } else {
                        holder.likeImage.setImageResource(R.mipmap.like_grey);
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //menu in card implantation and click listener
        sharedPref = new SharedPref(context);

        holder.menuBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context wrapper;
                if (sharedPref.loadNightModeState()) {
                    wrapper = new ContextThemeWrapper(context, R.style.popUpThemeDark);
                } else {
                    wrapper = new ContextThemeWrapper(context, R.style.popUpThemeLight);
                }
                PopupMenu popupMenu = new PopupMenu(wrapper, view);
                MenuInflater inflater = popupMenu.getMenuInflater();
                inflater.inflate(R.menu.post_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.post_menu_report_btn:
                                report(current_user_id, position, blogPostID);
                                break;
                            case R.id.post_menu_block_btn:
                                block(current_user_id, post_user_id);
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
                likeFeature();
                notifyDataSetChanged();
            }

            private void likeFeature() {
                firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                        .document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                try {
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
                                                            firebaseFirestore.collection("Users").document(current_user_id).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                                                                @Override
                                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                    if (documentSnapshot.exists()) {
                                                                        Log.e("Like notificaiton", "Like Notification current user entered");

                                                                        final String current_user_name = documentSnapshot.getString("name");
                                                                        Log.e("Like notificaiton", "Like Notification current user name " + current_user_name);

                                                                        final Map<String, Object> notificaitonMap = new HashMap<>();
                                                                        notificaitonMap.put("post_id", blogPostID);
                                                                        notificaitonMap.put("timestamp", FieldValue.serverTimestamp());
                                                                        notificaitonMap.put("message", "<b>" + current_user_name + "</b> <br>Liked your photo");
                                                                        Log.e("Notification user", "like notification post user id " + post_user_id);
                                                                        firebaseFirestore.collection("Users/" + post_user_id + "/Notification").add(notificaitonMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                                            @Override
                                                                            public void onComplete(@NonNull Task<DocumentReference> task) {
                                                                                if (task.isSuccessful()) {
                                                                                    firebaseFirestore.collection("Users/").document(post_user_id).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                                                                                        @Override
                                                                                        public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                                                            if (documentSnapshot.exists()) {
                                                                                                String token = documentSnapshot.getString("token");

                                                                                                Data data = new Data(blogPostID, "no");
                                                                                                com.rahulgaur.bloggersblog.notification.notificationServices.Notification notification = new com.rahulgaur.bloggersblog.notification.notificationServices.Notification("Likes", current_user_name + " Liked your Photo", "com.rahulgaur.bloggersblog.fcmClick");
                                                                                                Sender sender = new Sender(notification, token,data); //send notification to itself
                                                                                                Log.e("Sender Token", " " + token);
                                                                                                apiService.sendNotification(sender)
                                                                                                        .enqueue(new Callback<MyResponse>() {
                                                                                                            @Override
                                                                                                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                                                                                                try {
                                                                                                                    if (response.body().success == 1) {
                                                                                                                        Log.e("Notification service ", "Success");
                                                                                                                    } else {
                                                                                                                        Log.e("Notification service ", "Failed");
                                                                                                                    }
                                                                                                                } catch (NullPointerException ne) {
                                                                                                                    Log.e("Notificaiton", "Exception " + ne.getMessage());
                                                                                                                }
                                                                                                            }

                                                                                                            @Override
                                                                                                            public void onFailure(Call<MyResponse> call, Throwable t) {
                                                                                                                Log.e("Notification service ", "Failed");
                                                                                                            }
                                                                                                        });
                                                                                                Log.e("Like notificaiton", "Like Notification Added");

                                                                                            }
                                                                                        }
                                                                                    });
                                                                                } else {
                                                                                    Log.e("Like notificaiton", "Like Notification failed");
                                                                                }
                                                                            }
                                                                        });
                                                                    } else {
                                                                        Log.e("Like notificaiton", "Like Notification document not found");
                                                                    }
                                                                }
                                                            });
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            }
        });
        holder.likeView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                likeFeature();
                notifyDataSetChanged();
            }

            private void likeFeature() {
                firebaseFirestore.collection("Posts/" + blogPostID + "/Likes")
                        .document(currentUserId).get()
                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                try {
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
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });

            }
        });


        //delete feature
        holder.deleteImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFeature();
                notifyDataSetChanged();
            }

            private void deleteFeature() {
                //deleting photos from storage
                progressDialog.setMessage("Deleting Post Please Wait....");
                progressDialog.show();
                progressDialog.setCanceledOnTouchOutside(false);

                firebaseFirestore.collection("Posts").document(blogPostID).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull final Task<DocumentSnapshot> task) {
                        try {
                            if (task.getResult().exists()) {
                                final FirebaseStorage storageReference = FirebaseStorage.getInstance();
                                StorageReference delfile = storageReference.getReferenceFromUrl(thumb_image_url);
                                delfile.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        ///the post deleted from the storage
                                        try {
                                            if (task.isSuccessful()) {

                                                firebaseFirestore.collection("Posts").document(blogPostID).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        try {
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
                                                        } catch (Exception e) {
                                                            e.printStackTrace();
                                                        }
                                                    }
                                                });
                                            } else {
                                                Toast.makeText(context, "Error while deleting post.", Toast.LENGTH_SHORT).show();
                                                progressDialog.dismiss();
                                                Log.e("deleting post", "error deleting thumbnail from storage " + task.getException().getMessage());
                                                //error in thumbnail deletion
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });

        //comment feature
        holder.cmntImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToUser(blogPostID);
            }
        });
        holder.cmntView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToUser(blogPostID);
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToUser(blogPostID);
            }
        });

        //profile feature
        holder.profile.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("CommitTransaction")
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts")
                        .document(blogPostID).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        try {
                            if (documentSnapshot.exists()) {
                                post_user_id = documentSnapshot.getString("user_id");
                                if (post_user_id.equals(current_user_id)) {
                                    Toast.makeText(context, "Please select profile option from bottom..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent i = new Intent(context, UserAccount.class);
                                    i.putExtra("post_user_id", post_user_id);
                                    context.startActivity(i);
                                    Log.e("Post", "Post user id " + post_user_id);
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
        holder.userView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts")
                        .document(blogPostID).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        try {
                            if (documentSnapshot.exists()) {
                                post_user_id = documentSnapshot.getString("user_id");
                                if (post_user_id.equals(current_user_id)) {
                                    Toast.makeText(context, "Please select profile option from bottom..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent i = new Intent(context, UserAccount.class);
                                    i.putExtra("post_user_id", post_user_id);
                                    context.startActivity(i);
                                    Log.e("Post", "Post user id " + post_user_id);
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
        holder.dateView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                firebaseFirestore.collection("Posts")
                        .document(blogPostID).addSnapshotListener((Activity) context, new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        try {
                            if (documentSnapshot.exists()) {
                                post_user_id = documentSnapshot.getString("user_id");
                                if (post_user_id.equals(current_user_id)) {
                                    Toast.makeText(context, "Please select profile option from bottom..", Toast.LENGTH_SHORT).show();
                                } else {
                                    Intent i = new Intent(context, UserAccount.class);
                                    i.putExtra("post_user_id", post_user_id);
                                    context.startActivity(i);
                                    Log.e("Post", "Post user id " + post_user_id);
                                }
                            }
                        } catch (Exception e1) {
                            e1.printStackTrace();
                        }
                    }
                });
            }
        });
    }

    private void sendToUser(String blogPostID) {
        Intent i = new Intent(context, Comments.class);
        i.putExtra("blog_post_id", blogPostID);
        context.startActivity(i);
    }

    //report feature
    private void report(final String current_user_id, final int position, final String post_id) {
        /*
        report the user
        if 5 people report the user it will give the user warning
        and if after warning 5 more people report it will ban the user from the app
        */
        final AlertDialog alertDialog;

        if (sharedPref.loadNightModeState()) {
            new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_DARK);
        } else {
            new AlertDialog.Builder(context, AlertDialog.THEME_HOLO_LIGHT);
        }
        alertDialog = new AlertDialog.Builder(context)
                .setTitle("Caution!!")
                .setMessage("With great power comes great responsibility, \n" +
                        "this post will be removed from your home. " +
                        "It will also reduce the reputation of the user and may also ban them. " +
                        "Please use your power carefully")
                .setPositiveButton("Report", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Map<String, Object> map = new HashMap<>();
                        map.put("timestamp", FieldValue.serverTimestamp());
                        firebaseFirestore.collection("Posts/" + post_id + "/Report").document(current_user_id).set(map)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        try {
                                            if (task.isSuccessful()) {
                                                try {
                                                    postList.remove(position);
                                                    user_list.remove(position);
                                                    notifyDataSetChanged();
                                                } catch (IndexOutOfBoundsException e) {
                                                    Log.e("Reported", "Exception while removing list " + e.getMessage());
                                                }
                                                Log.e("Reported", "Post " + post_id + " reported by " + current_user_id);
                                                Log.e("report", "Report clicked");
                                                Toast.makeText(context, "Reported..", Toast.LENGTH_SHORT).show();
                                            } else {
                                                Log.e("report", "error: " + task.getException().getMessage());
                                            }
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e("report", "No clicked");
                    }
                }).show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFFF0000);
    }

    //block feature
    private void block(final String current_user_id, final String post_user) {
        final AlertDialog alertDialog;
        alertDialog = new AlertDialog.Builder(context)
                .setTitle("Caution!!")
                .setMessage("This action can not be undone, \n it will remove all the post of this particular user" +
                        "\n you will not see future posts from this user ")
                .setPositiveButton("Block", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e("block ", "block clicked");
                        Map<String, Object> map = new HashMap<>();
                        map.put("user_id", post_user);
                        map.put("timestamp", FieldValue.serverTimestamp());
                        firebaseFirestore.collection("Users/" + current_user_id + "/Block").document(post_user).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                try {
                                    if (task.isSuccessful()) {
                                        notifyDataSetChanged();
                                        Toast.makeText(context, "Blocked..", Toast.LENGTH_SHORT).show();
                                        Log.e("block ", "user " + current_user_id + " blocked " + post_user);
                                    } else {
                                        Log.e("block ", "block error");
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Log.e("block", "No clicked");
                    }
                }).show();
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFFF0000);
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
            deleteImage = mView.findViewById(R.id.comment_delete_imageView);
            likeImage = mView.findViewById(R.id.comment_likeView);
            likeView = mView.findViewById(R.id.like_tv);
            cmntImage = mView.findViewById(R.id.cmnt_imageView);
            cmntView = mView.findViewById(R.id.cmnt_tv);
            reportBtn = mView.findViewById(R.id.post_menu_report_btn);
            blockBtn = mView.findViewById(R.id.post_menu_block_btn);
            profile = mView.findViewById(R.id.profile_view);
            dateView = mView.findViewById(R.id.date_tv);
            userView = mView.findViewById(R.id.username_tv);
            imageView = mView.findViewById(R.id.post_imageView);
        }

        @SuppressLint("SetTextI18n")
        void setLikeView(final int likeCount) {
            likeView.setText(likeCount + "");
        }

        @SuppressLint("SetTextI18n")
        void setCmntView(final int cmntCount) {
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
            descView = mView.findViewById(R.id.comment_DescTV);
            descView.setText(text);
        }

        @SuppressLint("CheckResult")
        void setPostImage(String downloadUri) {
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.ic_launcher_background);
            try {
                Glide.with(context).applyDefaultRequestOptions(placeholder).load(downloadUri).into(imageView);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @SuppressLint("CheckResult")
        void setProfileImage(String downloadUri) {
            RequestOptions placeholder = new RequestOptions();
            placeholder.placeholder(R.drawable.default_usr);
            try {
                Glide.with(context).applyDefaultRequestOptions(placeholder).load(downloadUri).into(profile);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        void setUserText(String text) {
            userView.setText(text);
        }

        void setTime(StringBuilder date) {
            dateView.setText(date);
        }

    }
}