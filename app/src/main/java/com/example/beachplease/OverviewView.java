package com.example.beachplease;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompletePrediction;
import com.google.android.libraries.places.api.model.OpeningHours;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class OverviewView extends LinearLayout {

    private static final String API_KEY = "AIzaSyASAkr7HnHVzdOPChZtm2PIMKXQKgd8aZI";

    private FlexboxLayout tagContainer;
    private HashMap<String, Integer> tagNumber;
    private DatabaseReference beachRef;
    private final Beach beach;

    private PlacesClient placesClient;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Handler uiHandler;


    public OverviewView(Context context, Beach beach) {
        super(context);
        this.beach = beach;
        init(context);
    }

    private void init(Context context) {

        uiHandler = new Handler(Looper.getMainLooper());

        // Inflate the layout for the overview view
        LayoutInflater.from(context).inflate(R.layout.overview_view, this, true);
        tagContainer = findViewById(R.id.tag_container);

        //reference to the beach tagNumber in database
        beachRef = FirebaseDatabase.getInstance().getReference("beaches").child(beach.getId());

        //initial tag counts and setup display
        loadInitialTagCounts();

        // Get the address of the beach
        String address = getAddressOfBeach();

        // Fetch the beach details using the Places API
        fetchPlaceDetails(address);

        findViewById(R.id.address_container).setOnLongClickListener(v -> {

            // Create a URI for the address
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(address));

            // Create an Intent to launch Google Maps
            Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
            mapIntent.setPackage("com.google.android.apps.maps");

            // Check if there's an app to handle this intent
            if (mapIntent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(mapIntent);
            } else {
                Toast.makeText(context, "Google Maps app is not installed", Toast.LENGTH_SHORT).show();
            }
            return false;
        });


    }

    private String getAddressOfBeach() {
        Geocoder geocoder = new Geocoder(this.getContext(), Locale.getDefault());
        String addressLine = null;
        try {
            List<Address> addresses = geocoder.getFromLocation(beach.getLatitude(), beach.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                addressLine = addresses.get(0).getAddressLine(0);
                ((TextView) findViewById(R.id.beach_address)).setText(addressLine);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return addressLine;
    }

    //initial tag counts and display in descending order
    private void loadInitialTagCounts() {
        beachRef.child("tagNumber").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                tagNumber = new HashMap<>();
                for (DataSnapshot tagSnapshot : dataSnapshot.getChildren()) {
                    String tag = tagSnapshot.getKey();
                    Integer count = tagSnapshot.getValue(Integer.class);
                    if (tag != null && count != null) {
                        tagNumber.put(tag, count);
                    }
                }
                displaySortedTags();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load tag counts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //display tags sorted in descending order
    private void displaySortedTags() {
        tagContainer.removeAllViews();

        //sort tags
        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(tagNumber.entrySet());
        Collections.sort(sortedTags, (a, b) -> b.getValue().compareTo(a.getValue()));

        for (Map.Entry<String, Integer> entry : sortedTags) {
            String tag = entry.getKey();
            int count = entry.getValue();

            View tagItem = LayoutInflater.from(getContext()).inflate(R.layout.tag_item, tagContainer, false);
            TextView tagName = tagItem.findViewById(R.id.tag_name);
            TextView tagCounter = tagItem.findViewById(R.id.tag_counter);

            tagName.setText(tag);
            tagCounter.setText(String.valueOf(count));

            updateTagBackground(tagItem, count);

            //click to increase
            tagItem.setOnClickListener(v -> updateTagCount(tag, tagCounter, true));

            //long click to decrease
            tagItem.setOnLongClickListener(v -> {
                updateTagCount(tag, tagCounter, false);
                return true;
            });

            tagContainer.addView(tagItem);
        }
    }

    private void updateTagBackground(View tagItem, int count) {
        if (count > 0) {
            tagItem.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_roundedbox_selected));
        } else {
            tagItem.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_roundedbox));
        }
    }

    //update tag count and display
    private void updateTagCount(String tag, TextView tagCounter, boolean increment) {
        int currentCount = Integer.parseInt(tagCounter.getText().toString());
        int newCount = increment ? currentCount + 1 : Math.max(0, currentCount - 1);
        tagNumber.put(tag, newCount);

        //database update
        beachRef.child("tagNumber").child(tag).setValue(newCount);
        updateTopTags();

        //update display
        displaySortedTags();
    }

    //to update tags representing the beaches based on tag numbers
    private void updateTopTags() {
        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(tagNumber.entrySet());
        Collections.sort(sortedTags, (a, b) -> b.getValue().compareTo(a.getValue()));

        List<String> topTags = new ArrayList<>();
        for (int i = 0; i < Math.min(2, sortedTags.size()); i++) {
            topTags.add(sortedTags.get(i).getKey());
        }

        //database update
        beachRef.child("tags").setValue(topTags);
    }

    private void fetchPlaceDetails(String address) {
        // Initialize Places API if not already done
        if (!Places.isInitialized()) {
            Places.initialize(getContext(), API_KEY);
        }

        PlacesClient placesClient = Places.createClient(getContext());

        // Create a request for autocomplete predictions
        FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                .setQuery(address)
                .build();

        // Execute the request
        placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener(response -> {
                    List<AutocompletePrediction> predictions = response.getAutocompletePredictions();
                    if (!predictions.isEmpty()) {
                        String placeId = predictions.get(0).getPlaceId();
                        fetchPlaceDetailsById(placeId);  // Fetch details using place ID
                    } else {
                        showToast("No matching place found for address");
                    }
                })
                .addOnFailureListener(e -> {
                    e.printStackTrace();
                    showToast("Failed to fetch place ID");
                });
    }

    // Helper method to show Toast messages
    private void showToast(String message) {
        // Ensure that Toast is shown on the UI thread
        new Handler(Looper.getMainLooper()).post(() -> {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        });
    }


    private void fetchPlaceDetailsById(String placeId) {
        List<Place.Field> placeFields = Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.ADDRESS, Place.Field.OPENING_HOURS);
        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    updateOpeningHours(place.getOpeningHours());  // Custom method to update UI
                })
                .addOnFailureListener(e -> {
                    uiHandler.post(() ->
                            Toast.makeText(getContext(), "Failed to fetch place details", Toast.LENGTH_SHORT).show());
                });
    }



// Method to update the opening hours in the UI
    private void updateOpeningHours(OpeningHours openingHours) {
        if (openingHours == null || openingHours.getWeekdayText() == null) {
            Toast.makeText(getContext(), "No opening hours available", Toast.LENGTH_SHORT).show();
            return;
        }

        List<String> weekdayText = openingHours.getWeekdayText();

        // Map weekday hours to each TextView based on their order (Sunday through Saturday)
        if (weekdayText.size() == 7) {  // Ensure we have data for all days

            // Sunday
            TextView sundayHours = findViewById(R.id.sunday_hours);
            sundayHours.setText(weekdayText.get(0));

            // Monday
            TextView mondayHours = findViewById(R.id.monday_hours);
            mondayHours.setText(weekdayText.get(1));

            // Tuesday
            TextView tuesdayHours = findViewById(R.id.tuesday_hours);
            tuesdayHours.setText(weekdayText.get(2));

            // Wednesday
            TextView wednesdayHours = findViewById(R.id.wednesday_hours);
            wednesdayHours.setText(weekdayText.get(3));

            // Thursday
            TextView thursdayHours = findViewById(R.id.thursday_hours);
            thursdayHours.setText(weekdayText.get(4));

            // Friday
            TextView fridayHours = findViewById(R.id.friday_hours);
            fridayHours.setText(weekdayText.get(5));

            // Saturday
            TextView saturdayHours = findViewById(R.id.saturday_hours);
            saturdayHours.setText(weekdayText.get(6));
        } else {
            Toast.makeText(getContext(), "Invalid opening hours data", Toast.LENGTH_SHORT).show();
        }
    }


}
