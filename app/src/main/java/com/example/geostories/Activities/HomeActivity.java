package com.example.geostories.Activities;


import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.VideoView;

import com.bumptech.glide.Glide;
import com.example.geostories.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import org.w3c.dom.Text;

import java.util.Map;

public class HomeActivity extends AppCompatActivity {

    FirebaseFirestore db;
    FirebaseStorage dbStorage;
    StorageReference storageRef;
    StorageReference storagePath;
    QuerySnapshot querySnapshot;

    //Elementos de pantalla
    private Context context;
    private Button homeMapButton;
    private TextView welcomeText;
    private TextView homeProfileViewsText;
    private TextView homeViewsDoneText;
    private ImageView profilePic;
    private LinearLayout firstLinearLayout;
    private Switch switchNearStories;

    //Usuario actual
    private String userName;
    private long profileViews;
    private long viewsDone;

    //Elementos para ubicacion actual
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final String KEY_LOCATION = "location";
    private boolean locationPermissionGranted;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setTitle("Home");
        setContentView(R.layout.activity_home);
        Toolbar myToolbar = findViewById(R.id.toolbar_home);
        //--------Poner imagen desde ruta--------------------------------------------
        /*Activity thisActivity = this;
        profilePic = findViewById(R.id.profileImage);
        dbStorage = FirebaseStorage.getInstance();
        storageRef = dbStorage.getReferenceFromUrl("gs://geostories-9ae6f.appspot.com/");
        storagePath = storageRef.child("examples/images/asies.jpg");

        storagePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>()
        {
            @Override
            public void onSuccess(Uri downloadUrl)
            {
                //do something with downloadurl
                Log.d("Geostories", "PATH foto : " + downloadUrl);
                Glide.with(thisActivity).load(downloadUrl).into(profilePic);
            }
        });*/
        //----------------------------------------------------------------

        setSupportActionBar(myToolbar);
        Bundle extras = getIntent().getExtras();
        Log.d("Geostories", "A ver si funciona");
        String res = getIntent().getExtras().getString("actualUser");
        Log.d("Geostories","Email del usuario Actual: " + res);
        context = this;

        if(savedInstanceState!=null){
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
        }

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        getLocationPermission();
        getDeviceLocation();
        setUp();

    }




    private void setUp() {
        db = FirebaseFirestore.getInstance();
        DocumentReference res = db.collection("users").document(getIntent().getExtras().getString("actualUser"));
        res.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value.exists()){
                    putInfo(db, value);
                }
            }
        });
        firstLinearLayout = findViewById(R.id.firstLinearLayout);
        db.collection("stories").whereEqualTo("userOwner", getIntent().getExtras().getString("actualUser"))
        .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            querySnapshot = task.getResult();
                            storieSectionInfo(task.getResult());
                        } else {
                            Log.d("GeoStories", "Error getting documents: ", task.getException());
                        }
                    }
                });
        homeMapButton = findViewById(R.id.homeMapButton);
        homeMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), MapsActivity.class);
                intent.putExtra("ActualUser", getIntent().getExtras().getString("actualUser"));
                startActivity(intent);
            }
        });
        switchNearStories = findViewById(R.id.switchNearStories);
        switchNearStories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(switchNearStories.isChecked()){
                    if(lastKnownLocation != null){
                        firstLinearLayout.removeAllViews();
                        TextView right = new TextView(context);
                        right.setText("Ya tiene localizacion" + lastKnownLocation.toString());
                        firstLinearLayout.addView(right);
                    }else{
                        firstLinearLayout.removeAllViews();
                        TextView goMap = new TextView(context);
                        goMap.setText("Debe ir al mapa primero");
                        firstLinearLayout.addView(goMap);
                    }
                }else{
                    storieSectionInfo(querySnapshot);
                }
            }
        });
    }

    private void storieSectionInfo(QuerySnapshot result) {
        firstLinearLayout.removeAllViews();
        for (QueryDocumentSnapshot document : result) {
            LinearLayout secondLinearLayout = new LinearLayout(this);
            secondLinearLayout.setOrientation(0);

            /*VideoView video = new VideoView(this);
            video.setVideoURI(Uri.parse("http://videocdn.bodybuilding.com/video/mp4/62000/62792m.mp4"));
            video.setMediaController(new MediaController(this));
            video.requestFocus();
            video.start();
            secondLinearLayout.addView(video);*/

            TextView storieTittle = new TextView(this);
            storieTittle.setText(" ➩" + (String)document.get("storieTittle"));
            storieTittle.setTextColor((Color.parseColor("#FF0B4F6C")));
            storieTittle.setTypeface(null, Typeface.BOLD);
            storieTittle.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(v.getContext(), ViewStorie.class);
                    intent.putExtra("ActualUser", getIntent().getExtras().getString("actualUser"));
                    intent.putExtra("ActualStorie", document.getId());
                    startActivity(intent);
                }
            });
            secondLinearLayout.addView(storieTittle);

            TextView storieDescription = new TextView(this);
            storieDescription.setText("      " + (String)document.get("storieDescription"));
            storieDescription.setTypeface(null, Typeface.BOLD);
            secondLinearLayout.addView(storieDescription);
            firstLinearLayout.addView(secondLinearLayout);
        }
    }

    private void putInfo(FirebaseFirestore db, DocumentSnapshot value) {
        userName = value.getString("userName");
        welcomeText = findViewById(R.id.homeWelcomeText);
        welcomeText.setText("Bienvenido a GeoStories " + userName);

        profileViews = (long) value.get("profileViews");
        homeProfileViewsText = findViewById(R.id.homeProfileViewsText);
        homeProfileViewsText.setText("Visitas Perfil: " + profileViews);

        viewsDone = (long) value.get("viewsDone");
        homeViewsDoneText = findViewById(R.id.homeViewsDoneText);
        homeViewsDoneText.setText("Visitas Realizadas: " + viewsDone);

        profilePic = findViewById(R.id.profileImage);
        Glide.with(this).load(value.get("profilePicUrl")).into(profilePic);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    //Ubicacion actual
    private void getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
                locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if (task.isSuccessful()) {
                            // Set the map's camera position to the current location of the device.
                            lastKnownLocation = task.getResult();
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }
}

