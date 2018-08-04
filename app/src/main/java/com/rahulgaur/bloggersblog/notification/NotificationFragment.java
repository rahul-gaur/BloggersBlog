package com.rahulgaur.bloggersblog.notification;


import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.crashlytics.android.Crashlytics.TAG;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {

    private SwipeRefreshLayout swipeRefreshLayout;
    private Button clearBtn;
    private android.support.v7.widget.Toolbar toolbar;
    private SharedPref sharedPref;
    private List<NotificationList> notificationLists;
    private NotificationRecyclerAdapter notificationRecyclerAdapter;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String current_user_id;

    public NotificationFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        sharedPref = new SharedPref(Objects.requireNonNull(getContext()));
        if (sharedPref.loadNightModeState()) {
            getActivity().setTheme(R.style.darkTheme);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        swipeRefreshLayout = view.findViewById(R.id.frag_noti_swipeRefresh);

        toolbar = view.findViewById(R.id.noti_frag_toolbar);

        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Notification");

        firebaseFirestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        current_user_id = auth.getCurrentUser().getUid();
        clearBtn = view.findViewById(R.id.noti_frag_clearBtn);

        //adMob
        AdView adView;
        MobileAds.initialize(getContext(), "ca-app-pub-5119226630407445/5412380450");
        adView = view.findViewById(R.id.notificationAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        notificationLists = new ArrayList<>();

        notificationRecyclerAdapter = new NotificationRecyclerAdapter(notificationLists);

        final RecyclerView notiRecyclerView = view.findViewById(R.id.notification_recyclerView);
        notiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        notiRecyclerView.setAdapter(notificationRecyclerAdapter);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
                notiRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
                notiRecyclerView.setAdapter(notificationRecyclerAdapter);
                notificationRecyclerAdapter.notifyDataSetChanged();
            }
        });

        //notification retrieving
        Query sortNotification = firebaseFirestore.collection("Users/" + current_user_id + "/Notification").orderBy("timestamp", Query.Direction.DESCENDING);
        sortNotification.addSnapshotListener((Activity) Objects.requireNonNull(getContext()), new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(QuerySnapshot documentSnapshots, FirebaseFirestoreException e) {
                try {
                    if (!documentSnapshots.isEmpty()) {
                        for (DocumentChange doc : documentSnapshots.getDocumentChanges()) {
                            if (doc.getType() == DocumentChange.Type.ADDED) {
                                String notification_id = doc.getDocument().getId();
                                NotificationList notificationList = doc.getDocument().toObject(NotificationList.class).withID(notification_id);
                                notificationLists.add(notificationList);
                                //notificationRecyclerAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
        });

        //clear button function
        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog alertDialog;

                if (sharedPref.loadNightModeState()) {
                    new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_DARK);
                } else {
                    new AlertDialog.Builder(getContext(), AlertDialog.THEME_HOLO_LIGHT);
                }
                alertDialog = new AlertDialog.Builder(getContext())
                        .setTitle("Caution!!")
                        .setMessage("This will completely remove all of your notifications and you will not get them back")
                        .setPositiveButton("Clear", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.e("notification Cleared", "Cleared clicked");

                                firebaseFirestore.collection("Users/" + current_user_id + "/Notification").document().delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()){
                                                    //notification deleted
                                                    Toast.makeText(getContext(), "Cleared", Toast.LENGTH_SHORT).show();
                                                    notificationRecyclerAdapter.notifyDataSetChanged();
                                                } else {
                                                    //some error
                                                    Log.e(TAG, "onComplete: some error "+task.getException() );
                                                }
                                            }
                                        });
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.e("notification Cleared", "No clicked");
                            }
                        }).show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(0xFFFF0000);
            }
        });

        return view;
    }
}
