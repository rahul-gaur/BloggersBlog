package com.rahulgaur.bloggersblog.Search;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SearchView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.blogPost.User;

import org.apache.commons.lang3.StringUtils;

import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    String name;
    FirebaseFirestore firebaseFirestore;
    FirebaseAuth auth;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        Button searchBtn = view.findViewById(R.id.searchFrag_btn);
        final TextInputLayout textInputLayout = view.findViewById(R.id.searchFrag_textlayout);
        EditText editText = view.findViewById(R.id.searchFrag_textView);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                name = textInputLayout.getEditText().getText().toString();

                if (name.isEmpty() || name.equals("") || name.equals(" ")) {
                    textInputLayout.setError("Please write some name");
                } else {
                    name = StringUtils.capitalize(name);
                    Log.e("Search", "name entered "+name);
                    CollectionReference ref = firebaseFirestore.collection("Users");
                    Query query = ref.orderBy("name").startAt(name).endAt(name+"\uf8ff");
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()){
                                for (DocumentSnapshot documentSnapshot : task.getResult()){
                                    User user = documentSnapshot.toObject(User.class);
                                    String user_name = documentSnapshot.getString("name");
                                    Log.e("Search", "Name "+user_name);
                                }
                            } else {
                                Log.e("Search", "No data");
                            }
                        }
                    });

                }
            }
        });

        return view;
    }
}
