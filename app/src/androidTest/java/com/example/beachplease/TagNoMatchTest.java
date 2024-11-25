package com.example.beachplease;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.UiObjectNotFoundException;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import android.view.WindowManager;

import androidx.test.espresso.Root;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLngBounds;


@RunWith(AndroidJUnit4.class)
public class TagNoMatchTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Test
    public void testNoMatchBehavior() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(3000); // Wait for map to load

        // Handle location permission dialog
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        UiObject allowAlwaysButton = device.findObject(new UiSelector().text("While using the app"));
        if (allowAlwaysButton.exists()) {
            allowAlwaysButton.click();
        }

        Thread.sleep(5000); // Wait for map and tags to load

        // Click on a tag that has no matching beaches
        onView(withText("rock climbing")).perform(click());
        Thread.sleep(2000);

        // Use zoom range to check for tag no match
        activityRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity.getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(googleMap -> {
                    googleMap.setOnCameraIdleListener(() -> {
                        // Get the current visible region of the map
                        LatLngBounds currentBounds = googleMap.getProjection().getVisibleRegion().latLngBounds;

                        // Check if it matches exactly with CALIFORNIA_BOUNDS
                        boolean isExactBounds = currentBounds.equals(MapActivity.CALIFORNIA_BOUNDS);

                        onView(withId(R.id.map)).check(matches(isDisplayed())); // Ensure map is visible
                        assert isExactBounds : "Map is not zoomed exactly to California bounds.";
                    });
                });
            }
        });
    }
}