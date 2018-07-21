package com.rahulgaur.bloggersblog.welcome;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.home.MainActivity;

import java.util.Objects;

public class WelcomePage extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private TextView passTV, forgotPass;
    private ProgressBar progressBar;
    private String email, pass;
    private ImageView backImage;
    private ObjectAnimator forwardObjectAnimator;
    private TextInputLayout emailLayout, passLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome_page);

        Button login = findViewById(R.id.login_loginBtn);
        Button register = findViewById(R.id.login_regBtn);

        progressBar = findViewById(R.id.login_progressBar);

        passTV = findViewById(R.id.login_pass);
        TextView emailTV = findViewById(R.id.login_email);
        backImage = findViewById(R.id.welcome_imageView);
        emailLayout = findViewById(R.id.login_emailLayout);
        passLayout = findViewById(R.id.login_passLayout);
        forgotPass = findViewById(R.id.welcome_forgotPass);

        forgotPass.setEnabled(false);

        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        setAnimation();
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendToRegister();
            }
        });

        emailTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                forgotPass.setVisibility(View.VISIBLE);
                forgotPass.setEnabled(true);
            }
        });

        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                email = Objects.requireNonNull(emailLayout.getEditText()).getText().toString();

                if (!email.isEmpty()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d("Welcome page ", "Email sent.");
                                        Toast.makeText(WelcomePage.this, "Email send", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(WelcomePage.this, "Please write Your Email Address", Toast.LENGTH_SHORT).show();
                }
            }
        });

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = Objects.requireNonNull(emailLayout.getEditText()).getText().toString().trim().toLowerCase();
                pass = Objects.requireNonNull(passLayout.getEditText()).getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pass)) {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(WelcomePage.this, "Please wait..", Toast.LENGTH_SHORT).show();
                    auth.signInWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(WelcomePage.this, "Logging you in..", Toast.LENGTH_SHORT).show();
                                sendToMain();
                            } else {
                                String msg = Objects.requireNonNull(task.getException()).getMessage();
                                passLayout.setError("Some Error Try Again!");
                                Toast.makeText(WelcomePage.this, "Error: " + msg, Toast.LENGTH_LONG).show();
                                progressBar.setVisibility(View.INVISIBLE);
                                passTV.setText(null);
                            }
                        }
                    });
                } else {
                    Toast.makeText(WelcomePage.this, "Please Fill Both Fields", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void setAnimation() {
        forwardObjectAnimator = ObjectAnimator.ofFloat(backImage, "x", -1000);
        forwardObjectAnimator.setDuration(6000);
        forwardObjectAnimator.start();
    }

    private void setWelcomeImage() {
        try {
            Glide.with(getApplicationContext()).load(R.drawable.b).into(backImage);
        } catch (Exception e) {
            Log.e("WelcomePage ", "Error with glide " + e.getMessage());
        }
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
        } else {
            setWelcomeImage();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void sendToMain() {
        Intent i = new Intent(WelcomePage.this, MainActivity.class);
        startActivity(i);
        finish();
    }
}