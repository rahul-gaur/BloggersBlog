package com.rahulgaur.bloggersblog.account;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.dd.processbutton.iml.ActionProcessButton;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
import com.rahulgaur.bloggersblog.home.MainActivity;
import com.rahulgaur.bloggersblog.notification.notificationServices.Common;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

public class Account extends AppCompatActivity {

    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private Uri mainImageURI = null;
    private CircleImageView profile;
    private StorageReference storageReference;
    private ProgressBar progressBar;
    private FirebaseFirestore firebaseFirestore;
    private String user_id;
    private boolean isChanged = false;
    private Bitmap compressedImageFile;
    private SharedPref sharedPref;
    private Toolbar toolbar;
    private String current_user_token;
    private TextInputLayout textInputLayout;
    private ActionProcessButton btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        user_id = auth.getCurrentUser().getUid();

        //adMob
        AdView adView;
        MobileAds.initialize(this, "ca-app-pub-5119226630407445/3355396088");
        adView = findViewById(R.id.newAccountAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        current_user_token = Common.currentToken = FirebaseInstanceId.getInstance().getToken();

        profile = findViewById(R.id.circleImageView);
        btn = findViewById(R.id.account_subBtn_Flat);
        final TextView nameTV = findViewById(R.id.new_post_descET);
        progressBar = findViewById(R.id.account_progressBar);
        textInputLayout = findViewById(R.id.username_editTextLayout);
        toolbar = findViewById(R.id.account_toolBar);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Profile Update");

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();

        progressBar.setVisibility(View.VISIBLE);
        btn.setEnabled(false);

        btn.setMode(ActionProcessButton.Mode.PROGRESS);

        firebaseFirestore.collection("Users").document(user_id).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @SuppressLint("CheckResult")
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult().exists()) {
                        String name = task.getResult().getString("name");
                        String image = task.getResult().getString("thumb_image");

                        mainImageURI = Uri.parse(image);

                        nameTV.setText(name);
                        RequestOptions placeholder = new RequestOptions();
                        placeholder.placeholder(R.drawable.default_usr);
                        Glide.with(Account.this).setDefaultRequestOptions(placeholder).load(image).into(profile);
                    }

                } else {
                    String message = Objects.requireNonNull(task.getException()).getMessage();
                    Toast.makeText(Account.this,
                            "Error: " + message, Toast.LENGTH_SHORT).show();
                }
                progressBar.setVisibility(View.INVISIBLE);
                btn.setEnabled(true);
            }
        });

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //checking if user's android version is greater then or equals M
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    //if the version is >= M then check storage permission, if granted or not.
                    if (ContextCompat.checkSelfPermission(Account.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(Account.this, "Permission Denied", Toast.LENGTH_SHORT).show();
                        ActivityCompat.requestPermissions(Account.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                    } else {
                        ImagePicker();
                    }
                } else {
                    ImagePicker();
                }
            }
        });

        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String name = StringUtils.capitalize(Objects.requireNonNull(textInputLayout.getEditText()).getText().toString());
                progressBar.setVisibility(View.VISIBLE);

                btn.setMode(ActionProcessButton.Mode.ENDLESS);

                if (isChanged) {

                    if (!TextUtils.isEmpty(name) && !mainImageURI.toString().isEmpty()) {


                        StorageReference image_path = storageReference.child("profile_images").child(user_id + ".jpg");

                        image_path.putFile(mainImageURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if (task.isSuccessful()) {
                                    firebaseStore(task, name);
                                } else {
                                    btn.setProgress(-1);
                                    progressBar.setVisibility(View.INVISIBLE);
                                    String message = Objects.requireNonNull(task.getException()).getMessage();
                                    Toast.makeText(Account.this, "Image Error: " + message, Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } else {
                        btn.setProgress(-1);
                        Toast.makeText(Account.this, "Please write your full name..", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    firebaseStore(null, name);
                }
            }
        });
    }

    private void firebaseStore(@NonNull Task<UploadTask.TaskSnapshot> task, final String name) {

        Uri download_uri;

        final String randomName = UUID.randomUUID().toString();

        if (task != null) {
            download_uri = task.getResult().getDownloadUrl();
        } else {
            download_uri = mainImageURI;
        }

        File newImageFile = new File(mainImageURI.getPath());

        try {
            compressedImageFile = new Compressor(Account.this)
                    .setMaxHeight(200)
                    .setMaxWidth(200)
                    .setQuality(50)
                    .compressToBitmap(newImageFile);


            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 50, baos);
            byte[] profileThumb = baos.toByteArray();

            UploadTask uploadTask = storageReference.child("profile_images/thumbs")
                    .child(randomName + ".jpg").putBytes(profileThumb);

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    String downloadThumbUri = taskSnapshot.getDownloadUrl().toString();

                    Map<String, String> userMap = new HashMap<>();
                    userMap.put("thumb_image", downloadThumbUri);
                    userMap.put("name", name);
                    userMap.put("token", current_user_token);

                    firebaseFirestore.collection("Users").document(user_id).set(userMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {

                            if (task.isSuccessful()) {
                                progressBar.setVisibility(View.INVISIBLE);
                                Toast.makeText(Account.this, "Profile updated..", Toast.LENGTH_SHORT).show();
                                Intent i = new Intent(Account.this, MainActivity.class);
                                startActivity(i);
                                finish();
                                btn.setProgress(100);
                            } else {
                                progressBar.setVisibility(View.INVISIBLE);
                                String message = Objects.requireNonNull(task.getException()).getMessage();
                                Toast.makeText(Account.this, "Firestore error: " + message, Toast.LENGTH_SHORT).show();
                                btn.setProgress(-1);
                            }

                        }
                    });

                    progressBar.setVisibility(View.INVISIBLE);

                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void ImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1, 1)
                .start(Account.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mainImageURI = result.getUri();
                profile.setImageURI(mainImageURI);

                isChanged = true;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}