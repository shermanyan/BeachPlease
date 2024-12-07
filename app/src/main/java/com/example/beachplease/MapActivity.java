package com.example.beachplease;

import android.Manifest;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.Priority;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Beach> beaches;
    private final Set<String> selectedTags = new HashSet<>();
    private TextView loadingText;

    // Create a list to store all markers for testing
    private final List<Marker> markers= new ArrayList<>();

    private static final LatLng USC_LOCATION = new LatLng(34.0224, -118.2851);

    private LinearLayout tagLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final int REQUEST_CHECK_SETTINGS = 2;
    private LatLng sampleLocation; //Fabricated user location

    //CA boundaries for zoom function
    public static final LatLngBounds CALIFORNIA_BOUNDS = new LatLngBounds(
            new LatLng(32.5343, -124.4096), //Southwest corner
            new LatLng(42.0095, -114.1312)  //Northeast corner
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        initializeBeaches();

        //Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //UserIcon and functionality
        ImageView userIcon = findViewById(R.id.userIcon);
        userIcon.setOnClickListener(v -> {
            //Substitute class after implemented
            Intent intent = new Intent(MapActivity.this, UserProfileActivity.class);
            startActivity(intent);
        });

        //Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Tag bar layout
        tagLayout = findViewById(R.id.tagLayout);
        setupTagButtons(Arrays.asList("swimming", "sunbathing", "rock climbing", "surfing", "picnicking", "fishing", "hiking",
                "boating", "snorkeling", "kayaking", "wildlife watching", "picnic area",
                "bbq pits", "playground", "camping", "beach volleyball", "windsurfing",
                 "diving", "bird watching", "dog friendly"));

        //Request permission runtime
        requestLocationPermission();


    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;


        //Listener to wait for the map layout to be complete
        final View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();

        if (mapView != null) {
            mapView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                    //Initialize to California bounds
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(CALIFORNIA_BOUNDS, 100));
                }
            });
        }
//        findViewById(R.id.loading_overlay).setVisibility(View.VISIBLE);
//        loadingText.setVisibility(View.VISIBLE);
        //Location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
//            getUserLocation();
            checkLocationSettings();
        } else {
            requestLocationPermission();
//            Toast.makeText(this, "Location permission is required to show beaches nearby", Toast.LENGTH_SHORT).show();
        }

//        displayNearestBeaches(5);
    }

    public GoogleMap getGoogleMap() {
        return mMap;
    }

    private void checkLocationSettings() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateIntervalMillis(500)
                .build();

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);

        SettingsClient settingsClient = LocationServices.getSettingsClient(this);
        Task<LocationSettingsResponse> task = settingsClient.checkLocationSettings(builder.build());

        task.addOnSuccessListener(locationSettingsResponse -> getUserLocation());

        task.addOnFailureListener(e -> {
            if (e instanceof ResolvableApiException) {
                try {
                    ResolvableApiException resolvable = (ResolvableApiException) e;
                    resolvable.startResolutionForResult(MapActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException sendEx) {
                    Log.e("MapActivity", "Error showing location settings dialog", sendEx);
                }
            } else {
                Toast.makeText(this, "Please enable location settings to view nearby beaches", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestLocationPermission() {

        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST_CODE);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Permission granted
                checkLocationSettings();
            } else {
                //Permission denied
//                requestLocationPermission();
//                Toast.makeText(this, "Location permission denied. Defaulting to Grand Ave.", Toast.LENGTH_SHORT).show();
//                fallbackToGrandAve();
            }
        }
    }



    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationRequest locationRequest = new LocationRequest.Builder(
                    Priority.PRIORITY_HIGH_ACCURACY, 1000)
                    .setMinUpdateIntervalMillis(500)
                    .setMaxUpdates(1)
                    .build();

            fusedLocationClient.requestLocationUpdates(locationRequest, new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                        Location location = locationResult.getLastLocation();
                        sampleLocation = new LatLng(location.getLatitude(), location.getLongitude());
                        Log.d("MapActivity", "Fetched Location: " + sampleLocation);
                        //Defreeze the screen
                        findViewById(R.id.loading_overlay).setVisibility(View.GONE);
//                        loadingText.setVisibility(View.GONE);
                        displayNearestBeaches(5);
//                        verifyAddress(sampleLocation);
                    }
                    fusedLocationClient.removeLocationUpdates(this);
                }
            }, Looper.getMainLooper());
        }
    }



    //Fabricated beach information, substitute with database operation after implemented
    private void initializeBeaches() {
        FirebaseDatabase database = FirebaseDatabase.getInstance("https://beachplease-439517-default-rtdb.firebaseio.com/");
        DatabaseReference ref = database.getReference("beaches");

        beaches = new ArrayList<>();

        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                beaches.clear();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String id = snapshot.child("id").getValue(String.class);
                    String name = snapshot.child("name").getValue(String.class);
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);

                    List<String> tags = new ArrayList<>();
                    for (DataSnapshot tagSnapshot : snapshot.child("tags").getChildren()) {
                        tags.add(tagSnapshot.getValue(String.class));
                    }


                    Map<String, Integer> tagNumber = new HashMap<>();
                    if (snapshot.hasChild("tagNumber")) {
                        for (DataSnapshot tagNumSnapshot : snapshot.child("tagNumber").getChildren()) {
                            tagNumber.put(tagNumSnapshot.getKey(), tagNumSnapshot.getValue(Integer.class));
                        }
                    }

                    List<String> hours = new ArrayList<>();
                    if (snapshot.hasChild("hours")) {
                        for (DataSnapshot hourSnapshot : snapshot.child("hours").getChildren()) {
                            hours.add(hourSnapshot.getValue(String.class));
                        }
                    }

                    String formattedAddress = snapshot.hasChild("formattedAddress")
                            ? snapshot.child("formattedAddress").getValue(String.class)
                            : "";

                    String description = snapshot.hasChild("description")
                            ? snapshot.child("description").getValue(String.class)
                            : "";


                    Beach beach = new Beach(id, name, latitude, longitude, tags, description, formattedAddress, hours, tagNumber);
                    beaches.add(beach);

                    Log.d("MapActivity", "Beach added: " + beach.getName());
                }
            }

            @Override
            public void onCancelled(DatabaseError error) {
                Log.e("MapActivity", "Failed to load beaches from Firebase", error.toException());
            }
        });
    }

    //Show at least 5 nearest beaches
    private void displayNearestBeaches(int count) {
        if (mMap == null) return;

        LatLng userLocation = sampleLocation;

        //Sort beaches
        List<Beach> sortedBeaches = new ArrayList<>(beaches);
        Collections.sort(sortedBeaches, Comparator.comparingDouble(beach -> distanceFrom(userLocation, new LatLng(beach.getLatitude(), beach.getLongitude()))));

        //Pick beaches to zoom in
        List<Beach> nearestBeaches = sortedBeaches.subList(0, Math.min(count, sortedBeaches.size()));

        // Log the names and distances of the 5 nearest beaches
        for (Beach beach : nearestBeaches) {
            double distance = distanceFrom(userLocation, new LatLng(beach.getLatitude(), beach.getLongitude()));
            Log.d("MapActivity", "Nearest Beach: " + beach.getName() + ", Distance: " + distance + " meters");
        }
        displayBeaches(beaches);

//        Log.d("MapActivity", "Zooming to fit " + nearestBeaches.size() + " nearest beaches");
        adjustMapZoom(nearestBeaches);
    }

    //Distance calculator
    private double distanceFrom(LatLng start, LatLng end) {
        float[] results = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude, end.longitude, results);
        return results[0];
    }

    public void displayBeaches(List<Beach> beachesToDisplay) {
        mMap.clear();
        //Force refresh
        Marker dummyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
        dummyMarker.remove();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            for (Beach beach : beachesToDisplay) {
                Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(new LatLng(beach.getLatitude(), beach.getLongitude()))
                        .title(beach.getName()));
                marker.setTag(beach);
                markers.add(marker);
            }
            mMap.setOnMarkerClickListener(this::onMarkerClick);
        }, 100);

    }

    //Adjust zoom based on nearest beaches
    private void adjustMapZoom(List<Beach> beaches) {
        if (beaches == null || beaches.isEmpty()) {
            // If no beaches to show, reset the map to California bounds
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(CALIFORNIA_BOUNDS, 100));
            return;
        }

        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Beach beach : beaches) {
            boundsBuilder.include(new LatLng(beach.getLatitude(), beach.getLongitude()));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));

    }

    //Beach onclick
//    protected boolean onMarkerClick(Marker marker) { // private -> protected for testing
//        Beach selectedBeach = (Beach) marker.getTag();
//        Intent intent = new Intent(MapActivity.this, BeachDetailActivity.class);
//        intent.putExtra("beach", selectedBeach);
//        startActivity(intent);
//        return true;
//    }


    protected  boolean onMarkerClick(Marker marker){
        Beach selectedBeach = (Beach) marker.getTag();

//         Inflate the custom dialog layout
        View dialogView = LayoutInflater.from(this).inflate(R.layout.overview_view, new OverviewView(this, selectedBeach));

        TextView dialogBeachName = dialogView.findViewById(R.id.beach_name);
        View dialogBeachNameDivider = dialogView.findViewById(R.id.beach_name_divider);
//        View dialogNavbar = dialogView.findViewById(R.id.beach_navbar);

        assert selectedBeach != null;
        dialogBeachName.setText(selectedBeach.getName());
        dialogBeachName.setVisibility(View.VISIBLE);
        dialogBeachNameDivider.setVisibility(View.VISIBLE);
//        dialogNavbar.setVisibility(View.VISIBLE);

        // Create and show dialog
        AlertDialog alert = new AlertDialog.Builder(this)
                .setView(dialogView)
                .setPositiveButton("See More", (dialog, which) -> viewBeachDetails(selectedBeach))
                .setNegativeButton("Go back", (dialog, which) -> dialog.dismiss())
                .show();

       Window window = alert.getWindow();
       if (window != null) {
           window.setLayout(1000, 1500);
           window.setBackgroundDrawable(ContextCompat.getDrawable(this, R.drawable.rounded_box_modal));
       }

       return true;

    }

    private void viewBeachDetails(Beach beach){
        Intent intent = new Intent(MapActivity.this, BeachDetailActivity.class);
        intent.putExtra("beach", beach);
        startActivity(intent);
    }

    //Beach Markers list
    public List<Marker> getMarkersList() {
        return markers;
    }

    //Tag bar buttons
    private void setupTagButtons(List<String> tags) {
        tagLayout.removeAllViews();

        for (String tag : tags) {
            Button tagButton = new Button(this);
            tagButton.setText(tag);
            tagButton.setPadding(16, 8, 16, 8);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(8, 0, 8, 0);
            tagButton.setLayoutParams(params);
            tagButton.setBackgroundResource(R.drawable.tag_button_background);
            tagButton.setOnClickListener(v -> onTagButtonClick(tagButton, tag));
            tagLayout.addView(tagButton);
        }
    }

    //Button onclick
    private void onTagButtonClick(Button button, String tag) {
        if (selectedTags.contains(tag)) {
            //Already selected
            selectedTags.remove(tag);
            button.setBackgroundResource(R.drawable.tag_button_background);
        } else {
            //Not selected
            selectedTags.add(tag);
            button.setBackgroundResource(R.drawable.tag_button_selected_background);
        }
        button.invalidate();
        //Refresh list
        filterBeachesByTags();
    }

    //Tag filter functionality
    public void filterBeachesByTags() {
        mMap.clear();

        if (selectedTags.isEmpty()) {
            displayNearestBeaches(5);
            return;
        }

        List<Beach> filteredBeaches = new ArrayList<>();
        for (Beach beach : beaches) {
            if (beach.getTags() != null && beach.getTags().containsAll(selectedTags)) {
                filteredBeaches.add(beach);
            }
        }

        if (filteredBeaches.isEmpty()) {
            Toast.makeText(this, "No beaches match the selected tags", Toast.LENGTH_SHORT).show();
            //Force refresh
            Marker dummyMarker = mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)));
            dummyMarker.remove();

            //delay and clear
            new Handler(Looper.getMainLooper()).postDelayed(() -> mMap.clear(), 100);
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(CALIFORNIA_BOUNDS, 100));
            mMap.clear();
        } else {
            displayBeaches(filteredBeaches);
            adjustMapZoom(filteredBeaches.size() > 5 ? filteredBeaches.subList(0, 5) : filteredBeaches);
        }
    }

    public void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public Set<String> getSelectedTags() {
        return selectedTags;
    }

    public LatLngBounds getCurrentMapBounds() {
        if (mMap != null) {
            return mMap.getProjection().getVisibleRegion().latLngBounds;
        }
        return null; // Handle cases where googleMap is not initialized
    }

}
