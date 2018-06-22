package com.rahulgaur.bloggersblog.comment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Comments extends AppCompatActivity {

    private EditText comment_field;
    private ImageView comment_post_btn;
    private RecyclerView comment_list;

    private List<commentList> cmntList;

    private String blog_post_id;
    private String current_user_id;

    private commentsRecyclerAdapter commentsRecyclerAdapter;

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        android.support.v7.widget.Toolbar commentToolbar = findViewById(R.id.cmntToolbar);
        setSupportActionBar(commentToolbar);
        getSupportActionBar().setTitle("Comments");

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        cmntList = new ArrayList<>();

        commentsRecyclerAdapter = new commentsRecyclerAdapter(cmntList);

        comment_field = findViewById(R.id.cmntEditText);
        comment_post_btn = findViewById(R.id.cmntPostImageView);
        comment_list = findViewById(R.id.cmntRecyclerView);

        comment_list.setLayoutManager(new LinearLayoutManager(Comments.this));
        comment_list.setAdapter(commentsRecyclerAdapter);

        current_user_id = auth.getCurrentUser().getUid();
        blog_post_id = getIntent().getStringExtra("blog_post_id");

        //comments retrieving
        firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments")
                .addSnapshotListener(Comments.this,new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {

                if (!documentSnapshots.isEmpty()){
                    for (DocumentChange doc : documentSnapshots.getDocumentChanges()){
                        if (doc.getType()==DocumentChange.Type.ADDED){
                            String commentid = doc.getDocument().getId();
                            commentList commentList = doc.getDocument().toObject(com.rahulgaur.bloggersblog.comment.commentList.class)
                                    .withID(commentid);
                            cmntList.add(commentList);
                            commentsRecyclerAdapter.notifyDataSetChanged();
                        }
                    }
                }

            }
        });

        //comment posting
        comment_post_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String comment_message = comment_field.getText().toString();

                if (!comment_message.isEmpty()){

                    Map<String, Object> commentMap = new HashMap<>();
                    commentMap.put("message", comment_message);
                    commentMap.put("user_id",current_user_id);
                    commentMap.put("timestamp", FieldValue.serverTimestamp());

                    firebaseFirestore.collection("Posts/"+blog_post_id+"/Comments")
                            .add(commentMap)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if (!task.isSuccessful()){
                                Toast.makeText(Comments.this, "Error while posting your comment", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(Comments.this, "Comment posted ", Toast.LENGTH_SHORT).show();
                                 comment_field.setText(null);
                                 comment_field.clearFocus();
                            }
                        }
                    });

                }else {
                    Toast.makeText(Comments.this, "Please write the comment", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}