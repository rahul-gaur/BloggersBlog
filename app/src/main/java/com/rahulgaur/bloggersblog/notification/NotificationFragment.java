package com.rahulgaur.bloggersblog.notification;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.DayNightTheme;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class NotificationFragment extends Fragment {
    private SwipeRefreshLayout swipeRefreshLayout;
    private android.support.v7.widget.Toolbar toolbar;
    private DayNightTheme dayNightTheme = new DayNightTheme();

    public NotificationFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            getActivity().setTheme(R.style.darkTheme);
        } else {
            getActivity().setTheme(R.style.AppTheme);
        }

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_notification, container, false);

        swipeRefreshLayout = view.findViewById(R.id.frag_noti_swipeRefresh);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        toolbar = view.findViewById(R.id.noti_frag_toolbar);

        ((AppCompatActivity) Objects.requireNonNull(getActivity())).setSupportActionBar(toolbar);

        Objects.requireNonNull(((AppCompatActivity) getActivity()).getSupportActionBar()).setTitle("Notification");


        return view;
    }

    public void nightMode(String mode) {
        if (mode.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

}
