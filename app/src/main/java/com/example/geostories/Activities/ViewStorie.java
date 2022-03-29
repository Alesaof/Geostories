package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.Source;

public class ViewStorie extends AppCompatActivity {
    private FirebaseFirestore db;
    private boolean addDone = false;

    //Screen
    private TextView storieOwner;
    private TextView tittle;
    private TextView description;
    private TextView views;
    private VideoView video;

    private String creator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_storie);
        Toolbar myToolbar = findViewById(R.id.toolbar_viewStorie);
        setSupportActionBar(myToolbar);
        this.setTitle("Historia");

        Log.d("GeoStories", "storieId: " + getIntent().getExtras().getString("ActualStorie"));
        db = FirebaseFirestore.getInstance();
        DocumentReference res = db.collection("stories").document(getIntent().getExtras().getString("ActualStorie"));
        res.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    putInfoOnScreen(db, value);
                    if(!value.get("userOwner").equals(getIntent().getExtras().getString("ActualUser")) && !addDone){
                        addNewView(db, value);
                        addDone = true;
                    }

                }else{
                    showError();
                }
            }
        });

    }
    //Sumar visita cuando el usuario no sea el owner ----> POR HACER <-----------------------------
    private void addNewView(FirebaseFirestore db, DocumentSnapshot value) {
        Double newView = value.getDouble("storieViews") + 1;
        db.collection("stories").document(value.get("storieId").toString()).update("storieViews", newView);
    }

    private void putInfoOnScreen(FirebaseFirestore db, DocumentSnapshot value) {
        storieOwner = findViewById(R.id.textViewStorieOwner);
        db.collection("users").document(value.getString("userOwner")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    creator = task.getResult().getString("userName");
                    storieOwner.setText("➱"+"Creador: "+ creator);
                    Log.d("GeoStories", "Ha entrado en creator" + creator);
                }
            }
        });

        storieOwner.setTextColor((Color.parseColor("#FF0B4F6C")));
        storieOwner.setTypeface(null, Typeface.BOLD);
        storieOwner.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), ForeignProfileActivity.class);
                intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
                intent.putExtra("UserVisited", value.getString("userOwner"));
                Location lastKnownLocation = (Location)getIntent().getExtras().get("ActualLocation");
                intent.putExtra("ActualLocation", lastKnownLocation);
                startActivity(intent);
            }
        });

        tittle = findViewById(R.id.textViewTittleViewStorie);
        tittle.setText("Título: "+value.getString("storieTittle"));
        tittle.setTypeface(null, Typeface.BOLD);

        description = findViewById(R.id.textViewDescriptionViewStorie);
        description.setText("•" + value.getString("storieDescription"));
        description.setTypeface(null, Typeface.BOLD);

        views = findViewById(R.id.textViewVisitsViewStorie);
        long storieViewsCast = Math.round(value.getDouble("storieViews"));
        views.setText("Visitas: "+ storieViewsCast);
        views.setTypeface(null, Typeface.BOLD);

        video = findViewById(R.id.videoViewStorie);
        video.setVideoPath(value.getString("videoUri"));
        MediaController mediaController = new MediaController(this);
        mediaController.setAnchorView(video);
        video.setMediaController(mediaController);
        video.start();
    }

    private void showError() {
        Log.d("GeoStories", "Error sacar info de Storie");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.toolbarProfile:
                Intent intentProfile = new Intent(this, HomeActivity.class);
                intentProfile.putExtra("actualUser", getIntent().getExtras().getString("ActualUser"));
                intentProfile.putExtra("ActualLocation", (Location)getIntent().getExtras().get("ActualLocation"));
                startActivity(intentProfile);
                return true;
            case R.id.toolbarMap:
                Intent intentMap = new Intent(this, MapsActivity.class);
                intentMap.putExtra("ActualUser", getIntent().getExtras().getString("actualUser"));
                intentMap.putExtra("ActualLocation", (Location)getIntent().getExtras().get("ActualLocation"));
                startActivity(intentMap);
                return true;
            case R.id.toolbarlogOut:
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}