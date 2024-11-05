package com.example.beachplease;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.ContextCompat;

import com.google.android.flexbox.FlexboxLayout;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class OverviewView extends LinearLayout {

    private static final String API_KEY = "AIzaSyASAkr7HnHVzdOPChZtm2PIMKXQKgd8aZI";

    private FlexboxLayout tagContainer;
    private HashMap<String, Integer> tagNumber;
    private DatabaseReference beachRef;
    private final Beach beach;
    private TextView moreButton;
    private boolean showAllTags = false;


    public OverviewView(Context context, Beach beach) {
        super(context);
        this.beach = beach;
        init(context);
    }

    private void init(Context context) {


        // Inflate the layout for the overview view
        LayoutInflater.from(context).inflate(R.layout.overview_view, this, true);
        tagContainer = findViewById(R.id.tag_container);
        moreButton = findViewById(R.id.more_button);

        //reference to the beach tagNumber in database
        beachRef = FirebaseDatabase.getInstance().getReference("beaches").child(beach.getId());

        //initial tag counts and setup display
        loadInitialTagCounts();

        moreButton.setOnClickListener(v -> toggleTagDisplay());

        ((TextView) findViewById(R.id.beach_address)).setText(beach.getFormattedAddress());
        ((TextView) findViewById(R.id.beach_description)).setText(beach.getDescription());

        // Set hours
        List<String> hours = beach.getHours();
        if(hours.size() == 1){
            findViewById(R.id.hours_container).setVisibility(View.GONE);
            findViewById(R.id.no_hour).setVisibility(View.VISIBLE);

        } else {

            ((TextView)findViewById(R.id.monday_hours)).setText(hours.get(0));
            ((TextView)findViewById(R.id.tuesday_hours)).setText(hours.get(1));
            ((TextView)findViewById(R.id.wednesday_hours)).setText(hours.get(2));
            ((TextView)findViewById(R.id.thursday_hours)).setText(hours.get(3));
            ((TextView)findViewById(R.id.friday_hours)).setText(hours.get(4));
            ((TextView)findViewById(R.id.saturday_hours)).setText(hours.get(5));
            ((TextView)findViewById(R.id.sunday_hours)).setText(hours.get(6));

        }

        findViewById(R.id.address_container).setOnLongClickListener(v -> {

            // Create a URI for the address
            Uri gmmIntentUri = Uri.parse("geo:0,0?q=" + Uri.encode(beach.getFormattedAddress()));

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
                displayTags(false); // Show only top 2 tags initially
//                displaySortedTags();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to load tag counts", Toast.LENGTH_SHORT).show();
            }
        });
    }

    //display tags sorted in descending order
    private void displayTags(boolean showAll) {
        tagContainer.removeAllViews();

        List<Map.Entry<String, Integer>> sortedTags = new ArrayList<>(tagNumber.entrySet());
        Collections.sort(sortedTags, (a, b) -> b.getValue().compareTo(a.getValue()));

        int tagsToShow = showAll ? sortedTags.size() : Math.min(2, sortedTags.size());

        for (int i = 0; i < tagsToShow; i++) {
            Map.Entry<String, Integer> entry = sortedTags.get(i);
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
        updateMoreButtonLabel();
    }

    private void updateMoreButtonLabel() {
        moreButton.setText(showAllTags ? "Less" : "More");
    }

    //toggle text more less
    private void toggleTagDisplay() {
        showAllTags = !showAllTags;
        displayTags(showAllTags);
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
        displayTags(showAllTags);
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

}