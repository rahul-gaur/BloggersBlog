package com.rahulgaur.bloggersblog;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

public class WelcomePage extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private TextView passTV, emailTV;
    private ProgressBar progressBar;
    private String email, pass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        Button login = findViewById(R.id.login_loginBtn);
        Button register = findViewById(R.id.login_regBtn);

        progressBar = findViewById(R.id.login_progressBar);

        passTV = findViewById(R.id.login_pass);
        emailTV = findViewById(R.id.login_email);

        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = emailTV.getText().toString().trim().toLowerCase();
                pass = passTV.getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                    progressBar.setVisibility(View.VISIBLE);

                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                sendToMain();
                            } else {
                                String msg = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(WelcomePage.this, "Error: " + msg, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });
                } else {
                    Toast.makeText(WelcomePage.this, "Please Fill Both Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendToRegister() {
        Intent i = new Intent(WelcomePage.this, RegisterPage.class);
        startActivity(i);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser current_user = auth.getCurrentUser();

        if (current_user != null) {
            sendToMain();
        }
    }

    private void sendToMain() {
        Intent i = new Intent(WelcomePage.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}