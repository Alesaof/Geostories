package com.example.geostories.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.app.Activity;
import android.content.Intent;
import android.media.session.MediaController;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;

public class UploadStorieActivity extends AppCompatActivity {

    private UploadTask uploadTask;
    private MediaController mc;
    private Uri uriVideo;
    private Button uploadVideoButton;
    private ProgressBar progressBar;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;
    //private UploadTask uploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_storie);
        this.setTitle("Subir Historia");

        //Toolbar Menu
        Toolbar myToolbar = findViewById(R.id.toolbar_uploadStorie);
        setSupportActionBar(myToolbar);

        setup();



    }

    private void setup() {
        progressBar = findViewById(R.id.progressBarUploadStorieActivity);
        storageRef = FirebaseStorage.getInstance().getReference("Storie");
        databaseReference = FirebaseDatabase.getInstance().getReference("Stories");


        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        uriVideo = uri;
                        Log.d("GeoStories", "uriVideo: " + uriVideo + "|" + getIntent().getExtras().getString("ActualUser"));

                        StorageReference stoRef = storageRef.child("Stories/"+getIntent().getExtras().getString("ActualUser")+"/"+uriVideo.getLastPathSegment());
                        uploadTask = stoRef.putFile(uriVideo);

                        // Register observers to listen for when the download is done or if it fails
                        uploadTask.addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle unsuccessful uploads
                                Log.d("GeoStories", "ERROR VIDEO");
                            }
                        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                                // ...
                                Log.d("GeoStories", "VIDEO SUBIDO");

                            }
                        });
                    }
                });
        uploadVideoButton = findViewById(R.id.uploadVideoUploadStorieActivity);
        uploadVideoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // You can do the assignment inside onAttach or onCreate, i.e, before the activity is displayed
                mGetContent.launch("video/*");
            }
        });
    }
}