package com.rahulgaur.bloggersblog.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rahulgaur.bloggersblog.R;
import com.rahulgaur.bloggersblog.ThemeAndSettings.SharedPref;
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

import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {

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
    private SharedPref sharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sharedPref = new SharedPref(this);
        if (sharedPref.loadNightModeState()) {
            setTheme(R.style.darkTheme);
        } else {
            setTheme(R.style.AppTheme);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        imageView = findViewById(R.id.new_post_imageView);
        postDescET = findViewById(R.id.new_post_descET);
        uploadBtn = findViewById(R.id.new_post_uploadBtn);
        final TextInputLayout textInputLayout = findViewById(R.id.new_post_descETEditTextLayout);

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
                final String post_desc = StringUtils.capitalize(Objects.requireNonNull(textInputLayout.getEditText()).getText().toString());
                if (!TextUtils.isEmpty(post_desc) && !postUri.toString().isEmpty()) {
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

                                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                                    compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 50, baos);
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

                                            firebaseFirestore.collection("Posts").add(postMap)
                                                    .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                        @Override
                                                        public void onComplete(@NonNull Task<DocumentReference> task) {
                                                            try {
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
                                                            } catch (Exception e) {
                                                                e.printStackTrace();
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


                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

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

    private void sendToMain() {
        Intent i = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    private void ImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .start(NewPostActivity.this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                postUri = result.getUri();
                Log.e("NewPost","postImageUri "+postUri);
                try{
                    Glide.with(NewPostActivity.this)
                            .load(postUri)
                            .into(imageView);
                } catch (Exception e){
                    Log.e("NewPost","Glide Exception "+e.getMessage());
                }

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();
            }
        }
    }
}
