package com.example.geostories.Activities;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.geostories.R;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

public class HomeActivity extends AppCompatActivity {

    FirebaseFirestore db;
    private Button homeMapButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Home");
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.toolbar_home);
        setSupportActionBar(myToolbar);
        Bundle extras = getIntent().getExtras();
        Log.d("Geostories", "A ver si funciona");
        String res = getIntent().getExtras().getString("actualUser");
        Log.d("Geostories","Email del usuario Actual: " + res);
        setUp();

    }

    private void setUp() {
        db = FirebaseFirestore.getInstance();
        DocumentReference res = db.collection("users").document(getIntent().getExtras().getString("actualUser"));
        res.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    String userName = value.getString("userName");
                    TextView welcomeText = findViewById(R.id.homeWelcomeText);
                    welcomeText.setText("Bienvenido a GeoStories " + userName);
                }
            }
        });
        homeMapButton = findViewById(R.id.homeMapButton);
        homeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
}