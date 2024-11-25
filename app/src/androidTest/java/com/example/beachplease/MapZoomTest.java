package com.example.beachplease;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.UiObjectNotFoundException;

import com.google.android.gms.maps.SupportMapFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.util.Log;

import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;
import static androidx.test.espresso.action.ViewActions.click;

import java.util.concurrent.atomic.AtomicReference;

@RunWith(AndroidJUnit4.class)
public class MapZoomTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Test
    public void testMapZoom() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(3000); // Wait for map to load

        // Handle location permission dialog
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        UiObject allowAlwaysButton = device.findObject(new UiSelector().text("While using the app"));
        if (allowAlwaysButton.exists()) {
            allowAlwaysButton.click();
        }

        Thread.sleep(6000); // Wait for the map to load
        AtomicReference<Float> initialZoom = new AtomicReference<>((float) 0);

        activityRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    // Get the initial zoom level
                    initialZoom.set(googleMap.getCameraPosition().zoom);
                    Log.d("Test", "Initial Zoom Level: " + initialZoom);


                });
            }
        });

        // Click on "sunbathing" tag
        Thread.sleep(1000);
        onView(withText("sunbathing")).perform(click());
        Thread.sleep(3000);

        activityRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {

                    // Get the new zoom level after clicking "sunbathing"
                    float newZoom = googleMap.getCameraPosition().zoom;
                    Log.d("Test", "New Zoom Level after tag click: " + newZoom);

                    // Assert that the zoom level has changed
                    assert initialZoom.get() != newZoom : "Map zoom did not change after clicking 'sunbathing'.";


                });
            }
        });
    }
}