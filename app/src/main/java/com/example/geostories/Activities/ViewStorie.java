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
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewStorie extends AppCompatActivity {
    private FirebaseFirestore db;
    private boolean addDone = false;

    //Screen
    private TextView storieOwner;
    private TextView tittle;
    private TextView description;
    private TextView views;
    private VideoView video;
    private TextView route1;
    private TextView route2;

    private String creator;
    private Boolean hasViewed = true;
    private String storieBefore;



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
                    if(!value.getString("route").equals("")){ //Si la historia se encuentra en una ruta
                        db.collection("routes").document(value.getString("route")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task2) { //Obtenemos ruta
                                for(String storiesInRoute :(ArrayList<String>)task2.getResult().get("stories")){ //Lista de historias en ruta
                                    int posRoute = ((ArrayList<String>) task2.getResult().get("stories")).indexOf(value.getString("storieId"));
                                    setTittleOnRoute(posRoute, (ArrayList<String>) task2.getResult().get("stories"));
                                }
                            }
                        });
                    }
                }else{
                    showError();
                }
            }
        });

    }
    //Sumar visita cuando el usuario no sea el owner
    private void addNewView(FirebaseFirestore db, DocumentSnapshot value) {
        Double newView = value.getDouble("storieViews") + 1;
        db.collection("stories").document(value.get("storieId").toString()).update("storieViews", newView);
        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    HashMap<String,Object> updateStoriesViewed = new HashMap<>();
                    HashMap<String, Object>  storieMap;
                    ArrayList<String> storiesViewed = new ArrayList<>();
                    if(task.getResult().get("storiesViewed") != null){
                        storieMap = (HashMap<String, Object>) task.getResult().get("storiesViewed");
                        storiesViewed = (ArrayList<String>) storieMap.get("storiesViewed");
                        if(storiesViewed.indexOf(value.getString("storieId")) == -1){
                            storiesViewed.add(value.getString("storieId"));
                            updateStoriesViewed.put("storiesViewed", storiesViewed);
                            db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("storiesViewed", updateStoriesViewed);
                        }
                    }else{
                        storiesViewed.add(value.getString("storieId"));
                        updateStoriesViewed.put("storiesViewed", storiesViewed);
                        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("storiesViewed", updateStoriesViewed);
                    }
                }else{
                    showError();
                }
            }
        });
    }

    private void putInfoOnScreen(FirebaseFirestore db, DocumentSnapshot value) {
        storieOwner = findViewById(R.id.textViewStorieOwner);
        route1 = findViewById(R.id.textViewRouteStorie1);
        route2 = findViewById(R.id.textViewRouteStorie2);
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
        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                //Comprobar si la historia forma parte de una ruta y si el usuario ha visto la anterior.
                if(task.getResult().get("storiesViewed")!=null){
                    HashMap<String, Object> storieMap = (HashMap<String, Object>) task.getResult().get("storiesViewed");
                    proveIfViewed((ArrayList<String>)storieMap.get("storiesViewed"), value.getString("storieId"));
                }else{
                    proveIfViewed2(value.getString("storieId"));
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
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                // yourMethod();
                checkVideo(video, mediaController);
            }
        }, 5000);

    }

    private void checkVideo(VideoView video, MediaController mediaController) {
        Log.d("GeoStories","El checkVideo se encuentra en : " + hasViewed.toString());
        if(!hasViewed){
            video.stopPlayback();
            MediaController mediaC = new MediaController(this);
            mediaC.setEnabled(false);
            video.setMediaController(mediaC);
        }
    }

    //Si el usuario no ha visto nada y actualStorie forma parte de una ruta
    private void proveIfViewed2(String storieId) {
        db.collection("stories").document(storieId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().getString("route")!= null){
                    db.collection("routes").document(task.getResult().getString("route")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                            if(((ArrayList<String>) task2.getResult().get("stories")).indexOf(storieId) > 0){
                                hasViewed = false;
                            }else{
                                hasViewed = true;
                            }
                        }
                    });
                }else{
                    hasViewed = true;
                }
            }
        });
    }
    //Si el usuario si ha visto alguna historia y actualStorie forma parte de una historia
    private void proveIfViewed(ArrayList<String> storiesViewed, String storieId) {
        db.collection("stories").document(storieId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.getResult().getString("route") != null){
                    db.collection("routes").document(task.getResult().getString("route")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task2) {
                            int position = ((ArrayList<String>) task2.getResult().get("stories")).indexOf(storieId);
                            if(position > 0){
                                ArrayList<String> storiesInRoute = (ArrayList<String>)task2.getResult().get("stories");
                                storieBefore = storiesInRoute.get(position-1);
                                Log.d("GeoStories", "HISTORIA ANTERIOR: " + storieBefore + " Debulucion de if: " + storiesViewed.indexOf(storieBefore) +
                                        " Historias vistas por usuario: " + storiesViewed.toString());
                                if(storiesViewed.indexOf(storieBefore) != -1){
                                    hasViewed = true;
                                }else{
                                    Log.d("GeoStories", "Aqui lo pone a falso");
                                    hasViewed = false;
                                }
                            }else{
                                hasViewed = true;
                            }
                        }
                    });
                }else{
                    hasViewed = true;
                }
            }
        });
    }

    private void setTittleOnRoute(int posRoute, ArrayList<String> storiesInRoute) {
        if(posRoute == 0){
            hasViewed = true;
             db.collection("stories").document(storiesInRoute.get(1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            route1.setText("Siguiente parte de Historia: " + task.getResult().getString("storieTittle"));
                        }
                    });
        }else{
            if(posRoute == storiesInRoute.size()-1){
                db.collection("stories").document(storiesInRoute.get(posRoute-1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route1.setText("Parte anterior de Historia: " + task.getResult().getString("storieTittle"));
                    }
                });
            }else{
                db.collection("stories").document(storiesInRoute.get(posRoute-1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route1.setText("Parte anterior de Historia: " + task.getResult().getString("storieTittle"));
                    }
                });
                db.collection("stories").document(storiesInRoute.get(posRoute+1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route2.setText("Siguiente parte de Historia: " + task.getResult().getString("storieTittle"));
                    }
                });
            }
        }
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