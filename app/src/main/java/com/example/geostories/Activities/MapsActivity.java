package com.example.geostories.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.geostories.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.example.geostories.Activities.Services.InfoWindow;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private Button uploadStorieButton;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;

    private static final String TAG = "GeoStories";
    private GoogleMap map;
    private CameraPosition cameraPosition;
    // A default location (Sydney, Australia) and default zoom to use when location permission is
    // not granted.
    private final LatLng defaultLocation = new LatLng(-33.8523341, 151.2106085);
    private static final int DEFAULT_ZOOM = 15;
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private boolean locationPermissionGranted;
    // Keys for storing activity state.
    private static final String KEY_CAMERA_POSITION = "camera_position";
    private static final String KEY_LOCATION = "location";
    //Firebase
    private FirebaseFirestore db;
    //Marcadores
    Context context;
    ImageButton imgButton;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps_activiy);
        Toolbar myToolbar = findViewById(R.id.toolbar_map);
        setSupportActionBar(myToolbar);
        this.setTitle("Mapa");
        context = this;


        // Retrieve location and camera position from saved instance state.
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION);
        }

        // Construct a FusedLocationProviderClient.
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);



    }

    /**
     * Saves the state of the map when the activity is paused.
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (map != null) {
            outState.putParcelable(KEY_CAMERA_POSITION, map.getCameraPosition());
            outState.putParcelable(KEY_LOCATION, lastKnownLocation);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        //Este codigo creo que tiene que ir en un metodo que se llame desde el setup
        map = googleMap;
        // Prompt the user for permission.
        getLocationPermission();
        // Turn on the My Location layer and the related control on the map.
        updateLocationUI();
        // Get the current location of the device and set the position of the map.
        getDeviceLocation();

        uploadStorieButton = findViewById(R.id.uploadStorieMapActivity);
        uploadStorieButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), UploadStorieActivity.class);
                intent.putExtra("latitude", "" + lastKnownLocation.getLatitude());
                intent.putExtra("longitude", "" + lastKnownLocation.getLongitude());
                intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
                startActivity(intent);
            }
        });
        //Poner marcadores en el mapa.
        db = FirebaseFirestore.getInstance();
        db.collection("stories").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful()){
                    for (QueryDocumentSnapshot document : task.getResult()){
                        double storieLatitude = Double.parseDouble(document.get("storieLatitude").toString());
                        double storieLongitude = Double.parseDouble(document.get("storieLongitude").toString());
                        long storieViewsCast = Math.round(document.getDouble("storieViews"));
                        Marker m = map.addMarker(new MarkerOptions()
                                .position(new LatLng(storieLatitude, storieLongitude))
                                .title(document.get("storieTittle").toString())
                                .snippet(document.get("storieDescription").toString() + "\nVisitas: " + storieViewsCast));
                        if(lastKnownLocation.getLatitude() < (storieLatitude + 0.002) && lastKnownLocation.getLatitude() > (storieLatitude - 0.002)
                                && lastKnownLocation.getLongitude() > (storieLongitude - 0.002) &&
                                lastKnownLocation.getLongitude() < (storieLongitude + 0.002)){
                                    m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        }else{
                            m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                        }
                        m.setTag(document.getId());


                        GoogleMap.InfoWindowAdapter infoWindow = new InfoWindow(context);
                        map.setInfoWindowAdapter(infoWindow);
                        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
                            @Override
                            public void onInfoWindowClick(Marker m) {
                                Log.d("GeoStories","Ha en el infoView para ver historia");
                                Intent intent = new Intent(context, ViewStorie.class);
                                intent.putExtra("ActualUser", getIntent().getExtras().getString("ActualUser"));
                                intent.putExtra("ActualStorie", m.getTag().toString());
                                if(lastKnownLocation.getLatitude() < (m.getPosition().latitude + 0.002) && lastKnownLocation.getLatitude() > (m.getPosition().latitude - 0.002)
                                        && lastKnownLocation.getLongitude() > (m.getPosition().longitude - 0.002) &&
                                        lastKnownLocation.getLongitude() < (m.getPosition().longitude + 0.002)){
                                    startActivity(intent);
                                }else{
                                    Snackbar.make(MapsActivity.this, findViewById(android.R.id.content),"Debe acercarse más", Snackbar.LENGTH_LONG).show();
                                }

                            }
                        });
                        Log.d("GeoStories","Ha entrado en poner marcadores");
                    }
                }
            }
        });

        /*
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        Task<Location> locationResult = fusedLocationProviderClient.getLastLocation();
        locationResult.addOnCompleteListener(this, new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                if (task.isSuccessful()) {
                    // Set the map's camera position to the current location of the device.
                    lastKnownLocation = task.getResult();
                    if (lastKnownLocation != null) {
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()), 16.0f));
                    }
                }
            }
        });
        /*googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(28.113904, -15.450768), 16.0f));
        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(28.113904, -15.450768))
                .title("Marker"));
        /*LatLng sydney = new LatLng(-34, 151);
        GoogleMap mMap = googleMap;
        mMap.addMarker(new MarkerOptions()
                .position(sydney)
                .title("Marker in Sydney"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/
    }

    /**
     * Prompts the user for permission to use the device location.
     */
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

    /**
     * Updates the map's UI settings based on whether the user has granted location permission.
     */
    private void updateLocationUI() {
        if (map == null) {
            return;
        }
        try {
            if (locationPermissionGranted) {
                map.setMyLocationEnabled(true);
                map.getUiSettings().setMyLocationButtonEnabled(true);
            } else {
                map.setMyLocationEnabled(false);
                map.getUiSettings().setMyLocationButtonEnabled(false);
                lastKnownLocation = null;
                getLocationPermission();
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage());
        }
    }

    /**
     * Gets the current location of the device, and positions the map's camera.
     */
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
                            if (lastKnownLocation != null) {
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                        new LatLng(lastKnownLocation.getLatitude(),
                                                lastKnownLocation.getLongitude()), DEFAULT_ZOOM));
                            }
                        } else {
                            Log.d(TAG, "Current location is null. Using defaults.");
                            Log.e(TAG, "Exception: %s", task.getException());
                            map.moveCamera(CameraUpdateFactory
                                    .newLatLngZoom(defaultLocation, DEFAULT_ZOOM));
                            map.getUiSettings().setMyLocationButtonEnabled(false);
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Log.e("Exception: %s", e.getMessage(), e);
        }
    }

    //Este codigo es para que aparezca el menu extensible
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_toolbar, menu);
        return super.onCreateOptionsMenu(menu);
    }
}