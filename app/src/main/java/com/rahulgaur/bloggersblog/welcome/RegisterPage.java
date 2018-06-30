package com.rahulgaur.bloggersblog.welcome;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.DayNightTheme;
import com.rahulgaur.bloggersblog.account.Account;
import com.rahulgaur.bloggersblog.home.MainActivity;

import java.util.Objects;

public class RegisterPage extends AppCompatActivity {

    private ImageView backImage;
    private ObjectAnimator objectAnimator;
    private DayNightTheme dayNightTheme = new DayNightTheme();

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode()==AppCompatDelegate.MODE_NIGHT_YES){
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_page);

        Button reg = findViewById(R.id.reg_regBtn);

        final ProgressBar progressBar = findViewById(R.id.reg_progressBar);

        backImage = findViewById(R.id.account_backImage);

        final TextView emailTV = findViewById(R.id.reg_email);
        final TextView passTV = findViewById(R.id.reg_pass);
        final TextView passConfirmTV = findViewById(R.id.reg_passConfirm);


        objectAnimator = ObjectAnimator.ofFloat(backImage,"x",-1000);
        objectAnimator.setDuration(6000);
        objectAnimator.start();

        reg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailTV.getText().toString();
                String pass = passTV.getText().toString();
                String passConfirm = passConfirmTV.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(passConfirm)) {
                    if (pass.equals(passConfirm)) {
                        progressBar.setVisibility(View.VISIBLE);
                        Toast.makeText(RegisterPage.this, "Please wait.....", Toast.LENGTH_SHORT).show();
                        auth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    sendToRegister();
                                } else {
                                    String msg = Objects.requireNonNull(task.getException()).getMessage();
                                    progressBar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(RegisterPage.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        Toast.makeText(RegisterPage.this, "Password didn't match..", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterPage.this, "Please fill all the fields..", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void nightMode(String mode) {
        if (mode.equals("night")) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 7){
            GoogleSignInResult googleSignInResult = Auth.GoogleSignInApi
                    .getSignInResultFromIntent(data);
            if (googleSignInResult.isSuccess()){
                GoogleSignInAccount googleSignInAccount = googleSignInResult
                        .getSignInAccount();
                FirebaseUserAuth(googleSignInAccount);
            }
        }
    }

    public void FirebaseUserAuth(GoogleSignInAccount googleSignInAccount){
        Toast.makeText(this, "google id token "+ googleSignInAccount.getIdToken(), Toast.LENGTH_SHORT).show();
        AuthCredential authCredential = GoogleAuthProvider.getCredential(googleSignInAccount.getIdToken(),null);
        Toast.makeText(this, ""+authCredential.getProvider(), Toast.LENGTH_SHORT).show();
        auth.signInWithCredential(authCredential)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            Toast.makeText(RegisterPage.this, "welcome", Toast.LENGTH_SHORT).show();
                            sendToRegister();
                        } else {
                            String msg = task.getException().getMessage();
                            Toast.makeText(RegisterPage.this, "Error: "+msg, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
/*

    private void googleSignIn() {
        Intent AuthIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient);
        startActivityForResult(AuthIntent,7);
    }
*/

    private void sendToRegister() {
        Intent i = new Intent(RegisterPage.this, Account.class);
        startActivity(i);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser user = auth.getCurrentUser();
        if (!(user == null)) {
            sendToMain();
        } else {
            setBackImage();
        }
    }

    private void setBackImage() {
        try{
            Glide.with(getApplicationContext()).load(R.drawable.i).into(backImage);
        } catch (Exception e){
            Log.e("registerPage","some error in glide image update");
        }
    }

    private void sendToMain() {
        Intent i = new Intent(RegisterPage.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}
