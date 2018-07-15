package com.rahulgaur.bloggersblog.ThemeAndSettings;


import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class BlockFragment extends Fragment {
    private SharedPref sharedPref;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private String current_user_id;
    private List<BlockList> list;
    private blockAdapter blockAdapter;

    public BlockFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadNightModeState()) {
            getActivity().setTheme(R.style.darkTheme);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }
        View view = inflater.inflate(R.layout.fragment_block, container, false);

        list = new ArrayList<>();

        blockAdapter = new blockAdapter(list);

        RecyclerView recyclerView = view.findViewById(R.id.block_recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(blockAdapter);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();

        current_user_id = firebaseAuth.getCurrentUser().getUid();

        firebaseFirestore.collection("Users/" + current_user_id + "/Block")
                .addSnapshotListener((Activity) getContext(), new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                        if (!documentSnapshots.isEmpty()) {
                            for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                                if (doc.getType() == DocumentChange.Type.ADDED) {
                                    String user_id = doc.getDocument().getId();
                                    Log.e("BlockFragment","user_id "+user_id);
                                    BlockList blockList = doc.getDocument().toObject(BlockList.class)
                                            .withID(user_id);
                                    list.add(blockList);
                                    blockAdapter.notifyDataSetChanged();
                                }
                            }
                        }
                    }
                });

        return view;
    }

}