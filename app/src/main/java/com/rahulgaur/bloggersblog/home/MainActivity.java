package com.rahulgaur.bloggersblog.home;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.Search.SearchFragment;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.account.AccountFragment;
import com.rahulgaur.bloggersblog.home.updateChecker.versionChecker;
import com.rahulgaur.bloggersblog.notification.NotificationFragment;
import com.rahulgaur.bloggersblog.notification.notificationServices.Common;
import com.rahulgaur.bloggersblog.welcome.WelcomePage;

import java.util.HashMap;
import java.util.Map;

import static com.rahulgaur.bloggersblog.home.PostRecyclerAdapter.context;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity.java";
    private FirebaseAuth auth = FirebaseAuth.getInstance();

    private String current_user_id;
    FirebaseFirestore firebaseFirestore;

    private HomeFragment homeFrag;
    private NotificationFragment notiFrag;
    private AccountFragment accountFrag;
    private SearchFragment searchFrag;

    private ImageView homeBtn, notiBtn, searchBtn, addBtn, profileBtn;

    private SharedPref sharedPref;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Common.currentToken = FirebaseInstanceId.getInstance().getToken();

        Log.d("Main Activity", "token " + Common.currentToken);

        final FirebaseUser current_user = auth.getCurrentUser();
        assert current_user != null;
        current_user_id = current_user.getUid();

        homeBtn = findViewById(R.id.home_btn);
        notiBtn = findViewById(R.id.noti_Btn);
        searchBtn = findViewById(R.id.search_Btn);
        addBtn = findViewById(R.id.add_Btn);
        profileBtn = findViewById(R.id.profile_Btn);

        homeBtn.setOnClickListener(this);
        notiBtn.setOnClickListener(this);
        searchBtn.setOnClickListener(this);
        addBtn.setOnClickListener(this);
        profileBtn.setOnClickListener(this);

        checkUpdate();

        if (auth.getCurrentUser() != null) {

            //initializing fragments
            Log.d("Main Activity", "initializing fragments ");
            homeFrag = new HomeFragment();
            notiFrag = new NotificationFragment();
            accountFrag = new AccountFragment();
            searchFrag = new SearchFragment();
            initializeFragment();

            firebaseFirestore = FirebaseFirestore.getInstance();
        }

        assert current_user != null;
    }

    private void checkUpdate() {
        try {
            versionChecker VersionChecker = new versionChecker();
            String versionUpdated = VersionChecker.execute().get();
            Log.i("version code is", versionUpdated);


            PackageInfo packageInfo = null;
            try {
                packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            int version_code = packageInfo.versionCode;
            String version_name = packageInfo.versionName;
            Log.i("updated version code", String.valueOf(version_code) + "  " + version_name);
            if (!version_name.equals(versionUpdated)) {
                String packageName = getApplicationContext().getPackageName();

                AlertDialog alertDialog;
                if (sharedPref.loadNightModeState()) {
                    new android.app.AlertDialog.Builder(MainActivity.this, android.app.AlertDialog.THEME_HOLO_DARK);
                } else {
                    new android.app.AlertDialog.Builder(MainActivity.this, android.app.AlertDialog.THEME_HOLO_LIGHT);
                }

                alertDialog = new AlertDialog.Builder(MainActivity.this)
                        .setTitle("Please Update")
                        .setMessage("A Newer and Updated version of Blogger's Blogs" +
                                "is available to update, Update to improve stablilty" +
                                "and get excited new features")
                        .setPositiveButton("Update", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("https://play.google.com/store/apps/details?id=com.rahulgaur.bloggersblog+&hl=en"));
                                context.startActivity(intent);
                            }
                        })
                        .setNegativeButton("Maybe Later", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.e(TAG, "onClick: No Clicked");
                            }
                        }).show();
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.GREEN);
                alertDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.RED);
                alertDialog.setCanceledOnTouchOutside(false);

                Toast.makeText(getApplicationContext(), "please updated", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.getStackTrace();
        }
    }

    private void sendToNewPost() {
        Intent i = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current_user = auth.getCurrentUser();
        Log.d("Main Activity", "onStart() main");

        if (current_user == null) {
            sendUserToWelcome();
        } else {
            current_user_id = auth.getCurrentUser().getUid();
            firebaseFirestore.collection("Users").document(current_user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    try {
                        if (task.isSuccessful()) {
                            String token = task.getResult().getString("token");
                            final String current_user_token = Common.currentToken = FirebaseInstanceId.getInstance().getToken();

                            Map<String, Object> userMap = new HashMap<>();
                            userMap.put("token", current_user_token);

                            firebaseFirestore.collection("Users").document(current_user_id).update(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    try {
                                        if (task.isSuccessful()) {
                                            Log.e("token main", "token " + current_user_token);
                                        } else {
                                            Log.e("token main", "else ");
                                        }
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            });

                            if (!task.getResult().exists()) {
                                sendToAccount();
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Main Activity", "onResume() ");

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

        Log.d("Main Activity", "initializeFragment() called ");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();

        fragmentTransaction.add(R.id.main_fragLayout, homeFrag);
        fragmentTransaction.add(R.id.main_fragLayout, notiFrag);
        fragmentTransaction.add(R.id.main_fragLayout, accountFrag);
        fragmentTransaction.add(R.id.main_fragLayout, searchFrag);

        fragmentTransaction.hide(notiFrag);
        fragmentTransaction.hide(accountFrag);
        fragmentTransaction.hide(searchFrag);

        fragmentTransaction.commit();

    }

    private void sendToAccount() {
        Intent i = new Intent(MainActivity.this, Account.class);
        startActivity(i);
    }

    public void fragmentReplace(Fragment fragment) {
        Log.d("Main Activity", "Fragment Replacer called");

        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (fragment == homeFrag) {
            fragmentTransaction.hide(accountFrag);
            fragmentTransaction.hide(notiFrag);
            fragmentTransaction.hide(searchFrag);
        }
        if (fragment == accountFrag) {
            fragmentTransaction.hide(homeFrag);
            fragmentTransaction.hide(notiFrag);
            fragmentTransaction.hide(searchFrag);
        }
        if (fragment == notiFrag) {
            fragmentTransaction.hide(accountFrag);
            fragmentTransaction.hide(homeFrag);
            fragmentTransaction.hide(searchFrag);
        }
        if (fragment == searchFrag) {
            fragmentTransaction.hide(accountFrag);
            fragmentTransaction.hide(homeFrag);
            fragmentTransaction.hide(notiFrag);
        }
        fragmentTransaction.show(fragment);
        fragmentTransaction.commit();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home_btn:
                fragmentReplace(homeFrag);
                Log.d("Main Activity", "home btn clicked");
                break;
            case R.id.search_Btn:
                fragmentReplace(searchFrag);
                Log.d("Main Activity", "search btn clicked");
                break;
            case R.id.add_Btn:
                sendToNewPost();
                Log.d("Main Activity", "add btn clicked");
                break;
            case R.id.noti_Btn:
                fragmentReplace(notiFrag);
                Log.d("Main Activity", "noti btn clicked");
                break;
            case R.id.profile_Btn:
                fragmentReplace(accountFrag);
                Log.d("Main Activity", "account btn clicked");
                break;
        }
    }
}