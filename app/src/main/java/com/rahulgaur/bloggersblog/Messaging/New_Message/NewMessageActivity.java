package com.rahulgaur.bloggersblog.Messaging.New_Message;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;

import java.util.ArrayList;

public class NewMessageActivity extends AppCompatActivity {

    private ArrayList<MessageItem> list;
    private String TAG = "NewMessageActivity";
    private New_Message_adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_message);

        Toolbar toolbar = findViewById(R.id.new_message_toolBar);
        toolbar.setTitle("New Message");

        RecyclerView recyclerView = findViewById(R.id.new_message_recyclerView);

        list = new ArrayList<>();

        adapter = new New_Message_adapter(list);

        recyclerView.setLayoutManager(new LinearLayoutManager(NewMessageActivity.this));
        recyclerView.setAdapter(adapter);


        FirebaseAuth auth = FirebaseAuth.getInstance();
        final FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

        String current_user = auth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users/"+current_user+"/Following").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                try {
                    if (task.isSuccessful()){
                        for (final DocumentSnapshot doc : task.getResult()){
                            try {
                                if (doc.exists()){
                                    String following_id = doc.getId();
                                    firebaseFirestore.collection("Users")
                                            .document(following_id)
                                            .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                                                @Override
                                                public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                                                    try {
                                                        if (documentSnapshot.exists()){
                                                            String id = documentSnapshot.getId();
                                                            final MessageItem item = doc.toObject(MessageItem.class).withID(id);
                                                            String name = documentSnapshot.getString("name");
                                                            String thumb_image = documentSnapshot.getString("thumb_image");

                                                            Log.e(TAG, "onEvent: name "+name);

                                                            Log.e(TAG, "onEvent: list size "+list.size() );

                                                            item.setName(name);
                                                            item.setThumb_image(thumb_image);
                                                            list.add(item);

                                                            adapter.notifyDataSetChanged();
                                                        }
                                                    } catch (Exception e1) {
                                                        e1.printStackTrace();
                                                    }
                                                }
                                            });
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
