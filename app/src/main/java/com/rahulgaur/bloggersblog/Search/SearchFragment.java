package com.rahulgaur.bloggersblog.Search;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
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

import java.util.ArrayList;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 */
public class SearchFragment extends Fragment {
    private String name;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private ArrayList<SearchList> userList;
    private RecyclerView recyclerView;
    private SearchRecyclerAdapter searchRecyclerAdapter;

    public SearchFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        final TextInputLayout textInputLayout = view.findViewById(R.id.searchFrag_textlayout);
        EditText editText = view.findViewById(R.id.searchFrag_textView);

        userList = new ArrayList<>();

        recyclerView = view.findViewById(R.id.searchFrag_recyclerView);
        searchRecyclerAdapter = new SearchRecyclerAdapter(userList);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(searchRecyclerAdapter);

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int start, int before, int count) {
                if (count > 0) {
                    String name = StringUtils.capitalize(charSequence.toString());
                    Log.e("Search", "name " + name);
                    CollectionReference ref = firebaseFirestore.collection("Users");
                    Query query = ref.orderBy("name").startAt(name).endAt(name + "\uf8ff");
                    query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                userList.removeAll(userList);
                                for (DocumentSnapshot documentSnapshot : task.getResult()) {
                                    String user_id = documentSnapshot.getId();
                                    SearchList user = documentSnapshot.toObject(SearchList.class).withID(user_id);
                                    userList.add(user);
                                    String user_name = documentSnapshot.getString("name");
                                    Log.e("Search", "Name " + user_name + " user id " + user_id);
                                    searchRecyclerAdapter.notifyDataSetChanged();
                                }
                            } else {
                                Log.e("Search", "No data");
                                userList.removeAll(userList);
                            }
                        }
                    });
                } else {
                    Log.e("Search", "count = " + count);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        return view;
    }
}
