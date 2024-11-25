package com.example.beachplease;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Marker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.UiObjectNotFoundException;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import com.google.android.gms.maps.SupportMapFragment;
import static androidx.test.espresso.Espresso.pressBack;
import java.util.Optional;
import java.util.List;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

@RunWith(AndroidJUnit4.class)
public class TagFilteringTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Test
    public void testMultipleTagFilter() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(3000); // Wait for map to load

        // Handle location permission dialog
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        UiObject allowAlwaysButton = device.findObject(new UiSelector().text("While using the app"));
        if (allowAlwaysButton.exists()) {
            allowAlwaysButton.click();
        }

        Thread.sleep(5000);
        onView(withId(R.id.map)).check(matches(isDisplayed()));

        // Click on a tags
        onView(withText("swimming")).perform(click());
        onView(withText("sunbathing")).perform(click());
        onView(withText("rock climbing")).check(matches(isDisplayed()));
        onView(withId(R.id.map)).perform(click());
        Thread.sleep(2000);

        // Tags shown in beach page
        onView(withText("rock climbing")).check(doesNotExist());
        onView(withText("swimming")).check(matches(isDisplayed()));
        onView(withText("sunbathing")).check(matches(isDisplayed()));

    }
}