package com.rahulgaur.bloggersblog.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rahulgaur.bloggersblog.ThemeAndSettings.DayNightTheme;
import com.rahulgaur.bloggersblog.R;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

    private DayNightTheme dayNightTheme = new DayNightTheme();
    private ImageView imageView;
    private EditText postDescET;
    private Button uploadBtn;
    private Uri postUri = null;
    private ProgressBar progressBar;
    private StorageReference storageReference;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth auth;
    private String current_user_id;
    private Bitmap compressedImageFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        imageView = findViewById(R.id.new_post_imageView);
        postDescET = findViewById(R.id.new_post_descET);
        uploadBtn = findViewById(R.id.new_post_uploadBtn);

        //adMob
        AdView adView;
        MobileAds.initialize(this, "ca-app-pub-5119226630407445/4712709782");
        adView = findViewById(R.id.newAccountAd);
        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        auth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        current_user_id = auth.getCurrentUser().getUid();

        progressBar = findViewById(R.id.new_post_progressBar);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ImagePicker();
            }
        });

        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String post_desc = postDescET.getText().toString();
                if (!TextUtils.isEmpty(post_desc)) {
                    progressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(NewPostActivity.this, "Please wait..", Toast.LENGTH_LONG).show();
                    uploadBtn.setEnabled(false);
                    uploadBtn.setClickable(false);
                    final String randomName = UUID.randomUUID().toString();
                    StorageReference filePath = storageReference.child("post_images").child(randomName + ".jpg");
                    filePath.putFile(postUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            final String download_uri = task.getResult().getDownloadUrl().toString();

                            if (task.isSuccessful()) {

                                File newImageFile = new File(postUri.getPath());

                                //compress image
                                try {
                                    compressedImageFile = new Compressor(NewPostActivity.this)
                                            .setMaxHeight(450)
                                            .setMaxWidth(800)
                                            .setQuality(10)
                                            .compressToBitmap(newImageFile);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                                byte[] dataThumb = baos.toByteArray();

                                UploadTask uploadTask = storageReference
                                        .child("post_images/thumbs")
                                        .child(randomName + ".jpg").putBytes(dataThumb);

                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        String downloadThumbUri = String.valueOf(taskSnapshot.getDownloadUrl());
                                        Map<String, Object> postMap = new HashMap<>();

                                        postMap.put("thumb_image_url", downloadThumbUri);
                                        postMap.put("desc", post_desc);
                                        postMap.put("user_id", current_user_id);
                                        postMap.put("timestamp", FieldValue.serverTimestamp());
                                        postMap.put("post_name", randomName);

                                        firebaseFirestore.collection("Posts").add(postMap)
                                                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<DocumentReference> task) {
                                                        if (task.isSuccessful()) {
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                            Toast.makeText(NewPostActivity.this,
                                                                    "Post Added",
                                                                    Toast.LENGTH_SHORT).show();
                                                            sendToMain();
                                                        } else {
                                                            String msg = task.getException().getMessage();
                                                            Toast.makeText(
                                                                    NewPostActivity.this,
                                                                    "Error: " + msg,
                                                                    Toast.LENGTH_SHORT).show();
                                                            progressBar.setVisibility(View.INVISIBLE);
                                                        }
                                                    }
                                                });

                                    }
                                });

                                uploadTask.addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        //error handling
                                    }
                                });

                            } else {
                                uploadBtn.setEnabled(true);
                                uploadBtn.setClickable(true);
                                postDescET.setText(null);
                                String msg = task.getException().getMessage();
                                Toast.makeText(NewPostActivity.this, "Error: "
                                        + msg, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        }
                    });

                } else {
                    progressBar.setVisibility(View.INVISIBLE);
                    Toast.makeText(NewPostActivity.this,
                            "Please Fill the Description", Toast.LENGTH_SHORT).show();
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

    private void sendToMain() {
        Intent i = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void ImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(1024, 576)
                .setAspectRatio(16, 9)
                .start(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postUri = result.getUri();
                imageView.setImageURI(postUri);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
