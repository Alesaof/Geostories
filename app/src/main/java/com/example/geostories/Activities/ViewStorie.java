package com.example.geostories.Activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.widget.TextView;

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
                }else{
                    showError();
                }
            }
        });

    }

    private void putInfoOnScreen(FirebaseFirestore db, DocumentSnapshot value) {
        tittle = findViewById(R.id.textViewTittleViewStorie);
        tittle.setText(value.getString("storieTittle"));
        description = findViewById(R.id.textViewDescriptionViewStorie);
        description.setText(value.getString("storieDescription"));
        views = findViewById(R.id.textViewVisitsViewStorie);
        long storieViewsCast = Math.round(value.getDouble("storieViews"));
        views.setText("Visitas: "+ storieViewsCast);
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