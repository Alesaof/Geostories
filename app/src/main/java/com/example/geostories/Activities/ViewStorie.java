package com.example.geostories.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;

import com.example.geostories.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class ViewStorie extends AppCompatActivity {
    private FirebaseFirestore db;

    //Screen
    private TextView tittle;
    private TextView description;
    private TextView views;
    private VideoView video;

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
                    addNewView(db, value);
                }else{
                    showError();
                }
            }
        });

    }
    //Sumar visita cuando el usuario no sea el owner ----> POR HACER <-----------------------------
    private void addNewView(FirebaseFirestore db, DocumentSnapshot value) {

    }

    private void putInfoOnScreen(FirebaseFirestore db, DocumentSnapshot value) {
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
}