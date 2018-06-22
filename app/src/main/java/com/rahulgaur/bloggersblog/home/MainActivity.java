package com.rahulgaur.bloggersblog.home;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.notification.NotificationFragment;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.account.AccountFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String current_user_id;
    FirebaseFirestore firebaseFirestore;

    private homeFragment homeFrag;
    private NotificationFragment notiFrag;
    private AccountFragment accountFrag;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseUser current_user = auth.getCurrentUser();

        Toolbar toolbar = findViewById(R.id.main_toolbar);
        FloatingActionButton addPost = findViewById(R.id.main_add_post);

        homeFrag = new homeFragment();
        notiFrag = new NotificationFragment();
        accountFrag = new AccountFragment();

        if (auth.getCurrentUser() != null) {

            fragmentReplace(homeFrag);

            BottomNavigationView bottomNavigationView = findViewById(R.id.main_bottomNev);

            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                    switch (item.getItemId()) {
                        case R.id.bottom_home:
                            fragmentReplace(homeFrag);
                            return true;
                        case R.id.bottom_notification:
                            fragmentReplace(notiFrag);
                            return true;
                        case R.id.bottom_profile:
                            fragmentReplace(accountFrag);
                            return true;
                        default:
                            return false;
                    }
                }
            });

            firebaseFirestore = FirebaseFirestore.getInstance();

            addPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToNewPost();
                }
            });

        }

        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setTitle("Blogger's Blog");

        assert current_user != null;
    }

    private void sendToNewPost() {
        Intent i = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(i);
    }

    private void logout() {
        auth.signOut();
        sendUserToWelcome();
    }

    @Override
    protected void onStart() {
        super.onStart();

        network();

        FirebaseUser current_user = auth.getCurrentUser();

        if (current_user == null) {
            sendUserToWelcome();
        } else {
            current_user_id = auth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        if (!task.getResult().exists()) {
                            sendToAccount();
                        }
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (current_user_id == null) {
            sendUserToWelcome();
        }
    }

    private void sendUserToWelcome() {
        Intent i = new Intent(MainActivity.this, WelcomePage.class);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.toolbar_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.profile_menu:
                sendToAccount();
                break;
            case R.id.setting_AppBar:
                break;
            case R.id.LogOut_app_bar:
                logout();
                return true;
            default:
                return false;
        }
        return false;
    }

    private void network() {
        String answer = null;
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        if (null != activeNetwork) {
            if (activeNetwork.getType() == ConnectivityManager.TYPE_WIFI)
                answer = "You are connected to a WiFi Network";
            if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE)
                answer = "You are connected to a Mobile Network";
        } else
            answer = "No internet Connectivity";
    }

    private void sendToAccount() {
        Intent i = new Intent(MainActivity.this, Account.class);
        startActivity(i);
    }

    private void fragmentReplace(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_fragLayout, fragment);
        fragmentTransaction.commit();

    }

}