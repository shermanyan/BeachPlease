package com.example.beachplease;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiObjectNotFoundException;
import androidx.test.uiautomator.UiSelector;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;

import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import com.google.android.gms.maps.SupportMapFragment;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

@RunWith(AndroidJUnit4.class)
public class MarkerDisplayTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Test
    public void testMarkersDisplayCorrectly() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(3000); // Wait for map to load

        // Handle location permission dialog
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        UiObject allowAlwaysButton = device.findObject(new UiSelector().text("While using the app"));
        if (allowAlwaysButton.exists()) {
            allowAlwaysButton.click();
        }

        Thread.sleep(5000); // Wait for the map to process location permissions

        activityRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            // Check if any marker on the map
            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        List<Marker> markersList = ((MapActivity) activity).getMarkersList();

                        // Check if the markers list is not empty
                        if (!markersList.isEmpty()) {
                            Log.d("Test", "Markers are present on the map.");
                        } else {
                            Log.e("Test", "No markers are present on the map.");
                        }
                    }, 2000);
                });
            }
        });
    }
}
