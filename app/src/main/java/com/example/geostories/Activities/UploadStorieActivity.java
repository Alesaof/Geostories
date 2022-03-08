package com.example.geostories.Activities;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.EditText;
import android.widget.ProgressBar;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class UploadStorieActivity extends AppCompatActivity {

    private UploadTask uploadTask;
    private MediaController mc;
    private Uri uriVideo;
    private Button uploadVideoButton;
    private ProgressBar progressBar;
    private EditText editTextTittleUploadStorie;
    private EditText editTextDescriptionUploadStorie;
    private StorageReference storageRef;
    private DatabaseReference databaseReference;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String tittle;
    private String description;
    private String latitude;
    private String longitude;
    private UUID storieId;
    private boolean storieIdExist;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_storie);
        this.setTitle("Subir Historia");

        //Toolbar Menu
        Toolbar myToolbar = findViewById(R.id.toolbar_uploadStorie);
        setSupportActionBar(myToolbar);

        latitude = getIntent().getExtras().getString("latitude");
        longitude = getIntent().getExtras().getString("longitude");

        setup();



    }

    private void setup() {
        progressBar = findViewById(R.id.progressBarUploadStorieActivity);
        progressBar.setVisibility(View.INVISIBLE);
        editTextTittleUploadStorie = findViewById(R.id.editTextTittleUploadStorie);
        editTextDescriptionUploadStorie = findViewById(R.id.editTextDescriptionUploadStorie);

        storageRef = FirebaseStorage.getInstance().getReference("Storie");
        //databaseReference = FirebaseDatabase.getInstance().getReference("Stories");


        ActivityResultLauncher<String> mGetContent = registerForActivityResult(new ActivityResultContracts.GetContent(),
                new ActivityResultCallback<Uri>() {
                    @Override
                    public void onActivityResult(Uri uri) {
                        // Handle the returned Uri
                        progressBar.setVisibility(View.VISIBLE);
                        uriVideo = uri;
                        Log.d("GeoStories", "uriVideo: " + uriVideo + "|" + getIntent().getExtras().getString("ActualUser"));

                        StorageReference stoRef = storageRef.child("Stories/"+getIntent().getExtras().getString("ActualUser")+"/"+uriVideo.getLastPathSegment());
                        uploadTask = stoRef.putFile(uriVideo); //Guarda video en firebaseStorage


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
                                progressBar.setVisibility(View.INVISIBLE);
                                Log.d("GeoStories", "VIDEO SUBIDO");
                                tittle = editTextTittleUploadStorie.getText().toString();
                                description = editTextDescriptionUploadStorie.getText().toString();
                                //Para guardar en firebase database
                                Map<String, Object> map = new HashMap<>();
                                map.put("storieTittle", tittle);
                                map.put("storieDescription", description);
                                map.put("storieLatitude", latitude);
                                map.put("storieLongitude", longitude);
                                map.put("userOwner", getIntent().getExtras().getString("ActualUser"));
                               // Uri downloadurl = taskSnapshot.(); AQUI PA LA URI
                                map.put("videoUri", uriVideo.toString()); //No se si esto luego se puede utilizar realmente para visualizar.
                                map.put("storieViews", 0);
                                //Comprobar si el id de la historia ya existe
                                storieId = UUID.randomUUID();
                                storieIdExists();
                                map.put("storieId", getIntent().getExtras().getString("ActualUser")+ "-" +storieId.toString());
                                //databaseReference.child(map.get("storieId").toString()).setValue(map);
                                db.collection("stories").document(map.get("storieId").toString()).set(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if(task.isSuccessful()){
                                            backMap();
                                        }
                                    }
                                });

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

    private void storieIdExists() {
        while(storieIdExist){
            DocumentReference res = db.collection("stories").document(getIntent().getExtras().getString("ActualUser")+ "-" +storieId.toString());
            res.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                    if(value.exists()){
                        storieIdExist = true;
                    }else{
                        storieId = UUID.randomUUID();
                        storieIdExist = false;
                    }
                }
            });
        }
    }

    private void backMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }
}