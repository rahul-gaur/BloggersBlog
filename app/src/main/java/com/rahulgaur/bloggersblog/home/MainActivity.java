package com.rahulgaur.bloggersblog.home;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.account.AccountFragment;
import com.rahulgaur.bloggersblog.notification.NotificationFragment;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String current_user_id;
    FirebaseFirestore firebaseFirestore;

    private HomeFragment homeFrag;
    private NotificationFragment notiFrag;
    private AccountFragment accountFrag;

    private SharedPref sharedPref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()){
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FirebaseUser current_user = auth.getCurrentUser();
        assert current_user != null;
        current_user_id = current_user.getUid();

        FloatingActionButton addPost = findViewById(R.id.main_add_post);

        if (auth.getCurrentUser() != null) {

            homeFrag = new HomeFragment();
            notiFrag = new NotificationFragment();
            accountFrag = new AccountFragment();

            initializeFragment();

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

        assert current_user != null;
    }

    private void sendToNewPost() {
        Intent i = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    private void initializeFragment() {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_fragLayout, homeFrag);
        fragmentTransaction.add(R.id.main_fragLayout, notiFrag);
        fragmentTransaction.add(R.id.main_fragLayout, accountFrag);

        fragmentTransaction.hide(notiFrag);
        fragmentTransaction.hide(accountFrag);

        fragmentTransaction.commit();

    }

    private void sendToAccount() {
        Intent i = new Intent(MainActivity.this, Account.class);
        startActivity(i);
    }

    private void fragmentReplace(Fragment fragment) {

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == homeFrag) {
            fragmentTransaction.hide(accountFrag);
            fragmentTransaction.hide(notiFrag);
        }
        if (fragment == accountFrag) {
            fragmentTransaction.hide(homeFrag);
            fragmentTransaction.hide(notiFrag);
        }
        if (fragment == notiFrag) {
            fragmentTransaction.hide(accountFrag);
            fragmentTransaction.hide(homeFrag);
        }
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }
}