package com.example.beachplease;


import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.Manifest;
import android.content.pm.PackageManager;
import androidx.annotation.NonNull;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MapActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Beach> beaches;
    private Set<String> selectedTags = new HashSet<>();

    private LinearLayout tagLayout;
    private FusedLocationProviderClient fusedLocationClient;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private LatLng sampleLocation = new LatLng(34.0114, -118.4956); //Fabricated user location

    //CA boundaries for zoom function
    private static final LatLngBounds CALIFORNIA_BOUNDS = new LatLngBounds(
            new LatLng(32.5343, -124.4096), //Southwest corner
            new LatLng(42.0095, -114.1312)  //Northeast corner
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

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

        initializeBeaches();

        //Google Map
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Tag bar layout
        tagLayout = findViewById(R.id.tagLayout);
        setupTagButtons(Arrays.asList("swimming", "sunbathing", "surfing", "picnicking", "fishing", "hiking",
                "boating", "snorkeling", "kayaking", "wildlife watching", "picnic area",
                "bbq pits", "playground", "camping", "beach volleyball", "windsurfing",
                "rock climbing", "diving", "bird watching", "dog friendly"));

        //Request permission runtime
        requestLocationPermission();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        //Location permission
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            getUserLocation();
        } else {
            Toast.makeText(this, "Location permission is required to show beaches nearby", Toast.LENGTH_SHORT).show();
        }

        displayNearestBeaches(5);
    }

    private void requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getUserLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);  // Call to super

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Check if permission is granted before calling getUserLocation
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    getUserLocation();
                }
            } else {
                Toast.makeText(this, "Location permission denied.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void getUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                sampleLocation = new LatLng(location.getLatitude(), location.getLongitude());
                                displayNearestBeaches(5);
                            } else {
                                Toast.makeText(MapActivity.this, "Unable to get your location", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    //Fabricated beach information, substitute with database operation after implemented
    private void initializeBeaches() {
        beaches = new ArrayList<>();
        beaches.add(new Beach("1", "Sunny Cove Beach", 34.0123, -118.4956, Arrays.asList("swimming", "sunbathing")));
        beaches.add(new Beach("2", "Surf Point Beach", 34.0114, -118.4979, Arrays.asList("surfing", "picnicking")));
        beaches.add(new Beach("3", "Ocean Breeze Beach", 34.0150, -118.4912, Arrays.asList("swimming", "fishing")));
        beaches.add(new Beach("4", "Rocky Bay Beach", 34.0180, -118.4985, Arrays.asList("hiking", "wildlife watching")));
        beaches.add(new Beach("5", "Crystal Sands", 34.0195, -118.4860, Arrays.asList("boating", "snorkeling")));
        beaches.add(new Beach("6", "Pelican Point", 34.0205, -118.4902, Arrays.asList("kayaking", "bird watching")));
        beaches.add(new Beach("7", "Seaside Escape", 34.0140, -118.4932, Arrays.asList("picnic area", "bbq pits")));
        beaches.add(new Beach("8", "Adventure Bay", 34.0210, -118.4948, Arrays.asList("playground", "camping")));
        beaches.add(new Beach("9", "Coral Cove", 34.0165, -118.4899, Arrays.asList("beach volleyball", "dog friendly")));
        beaches.add(new Beach("10", "Lagoon Beach", 34.0130, -118.4915, Arrays.asList("windsurfing", "diving")));
        beaches.add(new Beach("11", "Golden Sands Beach", 34.0220, -118.5020, Arrays.asList("swimming", "sunbathing")));
        beaches.add(new Beach("12", "Moonlight Bay", 34.0255, -118.5056, Arrays.asList("surfing", "fishing")));
        beaches.add(new Beach("13", "Ocean Breeze", 34.0195, -118.5092, Arrays.asList("hiking", "wildlife watching")));
        beaches.add(new Beach("14", "Paradise Point", 34.0234, -118.4923, Arrays.asList("boating", "snorkeling")));
        beaches.add(new Beach("15", "Pelican Shores", 34.0270, -118.4884, Arrays.asList("kayaking", "bird watching")));
        beaches.add(new Beach("16", "Shoreline Park Beach", 34.0305, -118.4956, Arrays.asList("picnic area", "bbq pits")));
        beaches.add(new Beach("17", "Dolphin Cove", 34.0310, -118.4895, Arrays.asList("swimming", "boating")));
        beaches.add(new Beach("18", "Coral Beach", 34.0350, -118.4820, Arrays.asList("diving", "dog friendly")));
        beaches.add(new Beach("19", "Sandpiper Beach", 34.0378, -118.4782, Arrays.asList("surfing", "kayaking")));
        beaches.add(new Beach("20", "Blue Horizon Beach", 34.0420, -118.4750, Arrays.asList("windsurfing", "beach volleyball")));
        beaches.add(new Beach("21", "Laguna Beach", 34.0455, -118.4706, Arrays.asList("swimming", "sunbathing")));
        beaches.add(new Beach("22", "Emerald Bay Beach", 34.0490, -118.4650, Arrays.asList("boating", "camping")));
        beaches.add(new Beach("23", "Sunset Shores", 34.0532, -118.4601, Arrays.asList("diving", "rock climbing")));
        beaches.add(new Beach("24", "High Tide Beach", 34.0555, -118.4565, Arrays.asList("fishing", "picnicking")));
        beaches.add(new Beach("25", "Driftwood Beach", 34.0585, -118.4530, Arrays.asList("snorkeling", "kayaking")));
        beaches.add(new Beach("26", "Seabreeze Beach", 34.0610, -118.4495, Arrays.asList("bird watching", "hiking")));
        beaches.add(new Beach("27", "Starfish Cove", 34.0650, -118.4440, Arrays.asList("camping", "playground")));
        beaches.add(new Beach("28", "Seaside Haven", 34.0695, -118.4405, Arrays.asList("beach volleyball", "dog friendly")));
        beaches.add(new Beach("29", "Whale Watch Beach", 34.0730, -118.4370, Arrays.asList("snorkeling", "sunbathing")));
        beaches.add(new Beach("30", "Coastal Haven", 34.0770, -118.4325, Arrays.asList("kayaking", "windsurfing")));

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

    private void displayBeaches(List<Beach> beachesToDisplay) {
        mMap.clear();
        for (Beach beach : beachesToDisplay) {
            Marker marker = mMap.addMarker(new MarkerOptions()
                    .position(new LatLng(beach.getLatitude(), beach.getLongitude()))
                    .title(beach.getName()));
            marker.setTag(beach);
        }
        mMap.setOnMarkerClickListener(this::onMarkerClick);
    }

    //Adjust zoom based on nearest beaches
    private void adjustMapZoom(List<Beach> beaches) {
        LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
        for (Beach beach : beaches) {
            boundsBuilder.include(new LatLng(beach.getLatitude(), beach.getLongitude()));
        }
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 100));
    }

    //Beach onclick
    private boolean onMarkerClick(Marker marker) {
        Beach selectedBeach = (Beach) marker.getTag();
        Intent intent = new Intent(MapActivity.this, BeachDetailActivity.class);
        intent.putExtra("beach", selectedBeach);
        startActivity(intent);
        return true;
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
            tagButton.setBackgroundColor(Color.LTGRAY);
            tagButton.setOnClickListener(v -> onTagButtonClick(tagButton, tag));
            tagLayout.addView(tagButton);
        }
    }

    //Button onclick
    private void onTagButtonClick(Button button, String tag) {
        if (selectedTags.contains(tag)) {
            selectedTags.remove(tag);
            button.setBackgroundColor(Color.LTGRAY); // Unselected (Gray)
        } else {
            selectedTags.add(tag);
            button.setBackgroundColor(Color.YELLOW); // Selected (Highlighted)
        }
        filterBeachesByTags();
    }

    //Tag filter functionality
    private void filterBeachesByTags() {
        if (selectedTags.isEmpty()) {
            displayNearestBeaches(5);
            return;
        }

        List<Beach> filteredBeaches = new ArrayList<>();
        for (Beach beach : beaches) {
            if (beach.getTags().containsAll(selectedTags)) {
                filteredBeaches.add(beach);
            }
        }

        if (filteredBeaches.isEmpty()) {
            Toast.makeText(this, "No beaches match the selected tags.", Toast.LENGTH_SHORT).show();
            mMap.clear();
            mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(CALIFORNIA_BOUNDS, 100));
        } else {
            displayBeaches(filteredBeaches);
            adjustMapZoom(filteredBeaches.size() > 5 ? filteredBeaches.subList(0, 5) : filteredBeaches);
        }
    }
}
