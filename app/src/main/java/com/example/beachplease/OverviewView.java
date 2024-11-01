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

import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OverviewView extends LinearLayout {

    private static final List<String> TAGS = Arrays.asList(
            "swimming", "sunbathing", "surfing", "picnicking", "fishing", "hiking",
            "boating", "snorkeling", "kayaking", "wildlife watching", "picnic area",
            "bbq pits", "playground", "camping", "beach volleyball", "windsurfing",
            "rock climbing", "diving", "bird watching", "dog friendly"
    );

    private FlexboxLayout tagContainer;
    private List<TextView> tags;

    public OverviewView(Context context) {
        super(context);
        init(context);
    }

    private void init(Context context) {
        // Inflate the layout for the overview view
        LayoutInflater.from(context).inflate(R.layout.overview_view, this, true);
        tagContainer = findViewById(R.id.tag_container);

        findViewById(R.id.address_container).setOnLongClickListener(v -> {
            TextView addressTextView = findViewById(R.id.beach_address); // Assuming address_text is the TextView ID
            String address = addressTextView.getText().toString();

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


        setupTagButtons();
    }

    private void setupTagButtons() {
        for (String tag : TAGS) {
            View tagItem = LayoutInflater.from(getContext()).inflate(R.layout.tag_item, tagContainer, false);
            TextView tagName = tagItem.findViewById(R.id.tag_name);
            TextView tagCounter = tagItem.findViewById(R.id.tag_counter);

            tagName.setText(tag);

            int tagCount = getTagCounter(tag);
            updateTag(tagItem, tagCounter, tagCount);

            tagItem.setOnClickListener(v -> {
                int count = Integer.parseInt(tagCounter.getText().toString());
                count++;
                updateTag(tagItem, tagCounter, count);
            });

            tagItem.setOnLongClickListener(v -> {
                int count = Integer.parseInt(tagCounter.getText().toString());
                if (count > 0) {
                    count--;
                    updateTag(tagItem, tagCounter, count);
                }
                return true;
            });

            tagContainer.addView(tagItem);
        }
    }

    private void updateTag(View tagName, TextView tagCounter, int count) {
        // Update the tag counter
        tagCounter.setText(String.valueOf(count));

        if (count > 0) {
            tagName.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_roundedbox_selected));
        } else {
            tagName.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.tag_roundedbox));
        }
        // TODO: Update count in the database
    }

    private int getTagCounter(String tag) {
        // TODO: Retrieve count from the database

        Random random = new Random(); // Random object to generate random numbers

        int count = random.nextInt(5); // Generates a number from 1 to 10

        return count;
    }
}
