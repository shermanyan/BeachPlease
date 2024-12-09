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
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.platform.app.InstrumentationRegistry.getInstrumentation;

import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject;
import androidx.test.uiautomator.UiSelector;
import androidx.test.uiautomator.UiObjectNotFoundException;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static org.hamcrest.Matchers.allOf;
import android.view.View;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;


@RunWith(AndroidJUnit4.class)
public class MarkerClickTest {

    @Rule
    public ActivityScenarioRule<MapActivity> activityRule =
            new ActivityScenarioRule<>(MapActivity.class);

    // Custom Matcher: withIndex
    private static class PositionMatcher {
        public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
            return new TypeSafeMatcher<View>() {
                private int currentIndex = 0;

                @Override
                public void describeTo(Description description) {
                    description.appendText("with index: " + index);
                    matcher.describeTo(description);
                }

                @Override
                protected boolean matchesSafely(View view) {
                    return matcher.matches(view) && currentIndex++ == index;
                }
            };
        }
    }
    @Test
    public void testClickMarker() throws InterruptedException, UiObjectNotFoundException {
        Thread.sleep(3000); // Wait for map to load

        // Handle location permission dialog
        UiDevice device = UiDevice.getInstance(getInstrumentation());

        UiObject allowAlwaysButton = device.findObject(new UiSelector().text("While using the app"));
        if (allowAlwaysButton.exists()) {
            allowAlwaysButton.click();
        }

        Thread.sleep(5000); // Wait for map and tags to load

        // Click on a beach
        onView(withText("swimming")).perform(click());
        onView(withText("sunbathing")).perform(click());
        onView(withContentDescription("Beach title")).check(doesNotExist());
        onView(withId(R.id.map)).perform(click());
        Thread.sleep(3000);
        onView(PositionMatcher.withIndex(
                allOf(
                        withContentDescription("Hour Icon"),
                        isDescendantOfA(withId(16908290)) // Replace with the correct parent ID
                ),
                0)) // Replace 0 with the correct index
                .check(matches(isDisplayed()));
//                .perform(click());
    }
}
