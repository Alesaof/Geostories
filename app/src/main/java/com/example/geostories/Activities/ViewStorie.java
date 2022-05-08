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
import com.google.android.material.snackbar.Snackbar;
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
    private Boolean greenDone = false;



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
                            db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("viewsDone", storiesViewed.size());
                        }
                        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("viewsDone", storiesViewed.size());
                    }else{
                        storiesViewed.add(value.getString("storieId"));
                        updateStoriesViewed.put("storiesViewed", storiesViewed);
                        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("storiesViewed", updateStoriesViewed);
                        db.collection("users").document(getIntent().getExtras().getString("ActualUser")).update("viewsDone", storiesViewed.size());
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
            Snackbar.make(ViewStorie.this, findViewById(android.R.id.content),"Debe ver las historias anteriores para continuar la ruta", Snackbar.LENGTH_LONG).show();
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
                if(task.getResult().getString("route") != null && task.getResult().getString("route") != ""){
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
                            route1.setText("Siguiente historia en ruta: " + task.getResult().getString("storieTittle"));
                            setLinkOnRoute(task.getResult(), route1, posRoute, storiesInRoute, "next");
                        }
                    });
        }else{
            if(posRoute == storiesInRoute.size()-1){
                db.collection("stories").document(storiesInRoute.get(posRoute-1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route1.setText("Historia anterior en ruta: " + task.getResult().getString("storieTittle"));
                        setLinkOnRoute(task.getResult(), route1, posRoute, storiesInRoute, "back");
                    }
                });
            }else{
                db.collection("stories").document(storiesInRoute.get(posRoute-1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route1.setText("Parte anterior de Historia: " + task.getResult().getString("storieTittle"));
                        setLinkOnRoute(task.getResult(), route1, posRoute, storiesInRoute, "back");
                    }
                });
                db.collection("stories").document(storiesInRoute.get(posRoute+1)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        route2.setText("Siguiente parte de Historia: " + task.getResult().getString("storieTittle"));
                        setLinkOnRoute(task.getResult(), route2, posRoute, storiesInRoute, "next");
                    }
                });
            }
        }
    }
    //La idea es que aqui se añadan los links a las historias en ruta y dependiendo donde se encuentre el usuario haga una cosa u otra y si su situacion en la ruta.
    private void setLinkOnRoute(DocumentSnapshot result, TextView routeSelected, int posRoute, ArrayList<String> storiesInRoute, String nextorback) {
        switch (nextorback){
            case "next":
                if(hasViewed){
                    Location lastKnownLocation =  (Location)getIntent().getExtras().get("ActualLocation");
                    double storieLatitude = Double.parseDouble(result.get("storieLatitude").toString());
                    double storieLongitude = Double.parseDouble(result.get("storieLongitude").toString());
                    if(lastKnownLocation.getLatitude() < ( storieLatitude + 0.002) &&
                            lastKnownLocation.getLatitude() > (storieLatitude - 0.002)
                            && lastKnownLocation.getLongitude() > (storieLongitude - 0.002) &&
                            lastKnownLocation.getLongitude() < (storieLongitude + 0.002)) {
                        routeSelected.setTextColor((Color.parseColor("#FF0B4F6C")));
                        routeSelected.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ViewStorie.class);
                                intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
                                intent.putExtra("ActualStorie", result.getId());
                                intent.putExtra("ActualLocation", lastKnownLocation);
                                startActivity(intent);
                            }
                        });
                    }else{
                        //Codigo para cuando no se encuentra la siguiente al alcance
                        routeSelected.setTextColor((Color.parseColor("#34B30A")));
                        routeSelected.setText("Fuera de alcance -" + routeSelected.getText());
                        routeSelected.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openMapWithStorieLocation(result, v);
                            }
                        });

                    }
                }
            case "back":
                if(hasViewed){
                    Location lastKnownLocation =  (Location)getIntent().getExtras().get("ActualLocation");
                    double storieLatitude = Double.parseDouble(result.get("storieLatitude").toString());
                    double storieLongitude = Double.parseDouble(result.get("storieLongitude").toString());
                    if(lastKnownLocation.getLatitude() < ( storieLatitude + 0.002) &&
                            lastKnownLocation.getLatitude() > (storieLatitude - 0.002)
                            && lastKnownLocation.getLongitude() > (storieLongitude - 0.002) &&
                            lastKnownLocation.getLongitude() < (storieLongitude + 0.002)) {
                        routeSelected.setTextColor((Color.parseColor("#FF0B4F6C")));
                        routeSelected.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(v.getContext(), ViewStorie.class);
                                intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
                                intent.putExtra("ActualStorie", result.getId());
                                intent.putExtra("ActualLocation", lastKnownLocation);
                                startActivity(intent);
                            }
                        });
                    }else{
                        //Codigo para cuando no se encuentra la anterior al alcance
                        routeSelected.setTextColor((Color.parseColor("#34B30A")));
                        routeSelected.setText("Fuera de alcance -" + routeSelected.getText());
                        routeSelected.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                openMapWithStorieLocation(result, v);
                            }
                        });
                    }

                }else{
                    //Codigo para cuando no se le permite ver la historia porque no ha visto la anterior
                    db.collection("users").document(getIntent().getExtras().getString("ActualUser")).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if(task.isSuccessful()){
                                Map<String, Object> storiesViewedActualUserMap = (Map<String, Object>) task.getResult().get("storiesViewed");
                                ArrayList<String> storiesViewedActualUser = (ArrayList<String>) storiesViewedActualUserMap.get("storiesViewed");
                                for(String storieOnRoute: storiesInRoute){
                                    if(storiesViewedActualUser.indexOf(storieOnRoute) == -1){
                                        routeSelected.setTextColor((Color.parseColor("#34B30A")));
                                        if(!greenDone){
                                            greenDone = true;
                                            Log.d("GeoStories", "HISTORIA EN EL BUCLE: " + storieOnRoute);
                                            db.collection("stories").document(storieOnRoute).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    routeSelected.setText("Historia que deberia ver para continuar - " + task.getResult().getString("storieTittle"));
                                                    routeSelected.setOnClickListener(new View.OnClickListener() {
                                                        @Override
                                                        public void onClick(View v) {
                                                            openMapWithStorieLocation(task.getResult(), v);
                                                        }
                                                    });
                                                }
                                            });
                                        }
                                    }
                                }
                            }
                        }
                    });
                }
        }
    }

    private void openMapWithStorieLocation(DocumentSnapshot result, View v) {
        Location lastKnownLocation =  (Location)getIntent().getExtras().get("ActualLocation");
        Location storieFarderLocation = lastKnownLocation;
        double storieLatitude = Double.parseDouble(result.get("storieLatitude").toString());
        double storieLongitude = Double.parseDouble(result.get("storieLongitude").toString());
        storieFarderLocation.setLatitude(storieLatitude);
        storieFarderLocation.setLongitude(storieLongitude);

        Intent intent = new Intent(v.getContext(), MapsActivity.class);
        intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
        intent.putExtra("StorieFarderLocation", storieFarderLocation);
        intent.putExtra("ActualLocation", lastKnownLocation);
        startActivity(intent);
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