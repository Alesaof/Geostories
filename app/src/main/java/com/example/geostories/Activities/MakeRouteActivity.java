package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.example.geostories.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class MakeRouteActivity extends AppCompatActivity {
    //Screen Elements
    private ListView lv;
    private Button makeRouteButton;
    private ProgressBar progressBar;

    //Firebase
    private FirebaseFirestore db;

    //Elements for routes
    private HashMap<String, Object> route;
    private ArrayList<String>  storiesToRoute;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_route);
        Toolbar myToolbar = findViewById(R.id.toolbar_makeRoute);
        setSupportActionBar(myToolbar);
        this.setTitle("Crear ruta");

        setUp();
    }

    private void setUp() {
        lv = findViewById(R.id.listViewStoriesToRoute);
        progressBar = findViewById(R.id.progressBarUploadRoute);
        progressBar.setVisibility(View.INVISIBLE);
        db = FirebaseFirestore.getInstance();
        Log.d("GeoStories", "Nombre del usuario de crear ruta: " + getIntent().getExtras().getString("ActualUser"));
        //Seteo en el ListView
        db.collection("stories").whereEqualTo("userOwner", getIntent().getExtras().getString("ActualUser"))
                .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    Object[] userStoriess= new Object[task.getResult().size()];
                    int i = 0;
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        userStoriess[i] = document.getString("storieTittle");
                        i++;
                    }
                    setStoriesArray(userStoriess);
                } else {
                    Log.d("GeoStories", "Error getting documents: ", task.getException());
                }
            }
        });
        //Manejo de los items seleccionados en el ListView
        storiesToRoute = new ArrayList<>();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                db.collection("stories").whereEqualTo("storieTittle", lv.getItemAtPosition(position)).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                storiesToRoute.add(document.getString("storieId"));
                                view.setBackgroundColor(Color.BLUE);

                                /*Resources r = view.getResources();
                                for(Resources a: r.getR){

                                }
                                view.getResources().getText((int)lv.getItemIdAtPosition(position));*/
                            }
                        } else {
                            Log.d("GeoStories", "Error getting documents: ", task.getException());
                        }
                    }
                });
            }
        });

        makeRouteButton = findViewById(R.id.buttonMakeRouteInActivity);
        makeRouteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);
                route = new HashMap<>();
                UUID routeIdRandom = UUID.randomUUID();
                String routeId = getIntent().getExtras().getString("ActualUser") + "-r" + routeIdRandom.toString();
                route.put("RouteId", routeId);
                route.put("stories", storiesToRoute);
                route.put("userOwner",  getIntent().getExtras().getString("ActualUser"));
                Log.d("GeoStories", "Mapa de ruta: " + route.toString());
                for(String storieId: storiesToRoute){
                    db.collection("stories").document(storieId).update("route", routeId);
                }

                db.collection("routes").document(routeId).set(route).addOnCompleteListener(new OnCompleteListener<Void>() {
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

    private void setStoriesArray(Object[] userStories) {
        ArrayAdapter<Object> storiesToSelectAdapter = new ArrayAdapter<Object>(this, R.layout.list_item_storiestoroute, userStories);
        lv.setAdapter(storiesToSelectAdapter);
    }

    private void backMap() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("ActualUser", getIntent().getExtras().getString("actualUser"));
        intent.putExtra("ActualLocation", (Location)getIntent().getExtras().get("ActualLocation"));
        startActivity(intent);
    }

    //Toolbar
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
                intentProfile.putExtra("actualUser", getIntent().getExtras().getString("actualUser"));
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