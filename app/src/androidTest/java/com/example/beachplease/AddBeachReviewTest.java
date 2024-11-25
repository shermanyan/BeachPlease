package com.example.beachplease;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.annotation.VisibleForTesting;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.Marker;
import android.os.Handler;
import android.os.Looper;
import java.util.Optional;
import java.util.List;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class AddBeachReviewTest {

    @Rule
    public ActivityScenarioRule<MapActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void beachReviewTest() throws InterruptedException {
        Thread.sleep(5000);

        ViewInteraction appCompatImageView = onView(
                allOf(withId(R.id.userIcon), withContentDescription("User Profile"),
                        childAtPosition(
                                allOf(withId(R.id.toolbar),
                                        childAtPosition(
                                                withClassName(is("android.widget.RelativeLayout")),
                                                0)),
                                0),
                        isDisplayed()));
        appCompatImageView.perform(click());

        Thread.sleep(5000);

        ViewInteraction materialButton = onView(
                allOf(withId(R.id.login), withText(R.string.login),
                        childAtPosition(
                                allOf(withId(R.id.main),
                                        childAtPosition(
                                                withId(android.R.id.content),
                                                0)),
                                3),
                        isDisplayed()));
        materialButton.perform(click());

        Thread.sleep(3000);

        ViewInteraction textInputEditText = onView(
                allOf(withId(R.id.loginEmailInput),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.loginemail),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText.perform(replaceText("mj@gmail.com"), closeSoftKeyboard());

        Thread.sleep(2000);

        ViewInteraction textInputEditText2 = onView(
                allOf(withId(R.id.loginPasswordInput),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.loginpassword),
                                        0),
                                0),
                        isDisplayed()));
        textInputEditText2.perform(replaceText("heehee"), closeSoftKeyboard());

        Thread.sleep(2000);

        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(5000);


        onView(withId(R.id.map)).check(matches(isDisplayed()));


        // Google maps marker click simulation
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(mMap -> {
                    // Add a short delay for Firebase data to load
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        List<Marker> markersList = ((MapActivity) activity).getMarkersList();

                        // Search for selected marker by title
                        Optional<Marker> selectedMarkerOpt = markersList.stream()
                                .filter(marker -> "Montara State Beach".equals(marker.getTitle()))
                                .findFirst();

                        if (selectedMarkerOpt.isPresent()) {
                            Marker selectedMarker = selectedMarkerOpt.get();
                            Log.d("Test", "Found marker: " + selectedMarker.getTitle());

                            // Move camera to marker position
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));

                            // call marker click method from map activity
                            activity.onMarkerClick(selectedMarker);

                            Log.d("Test", "Marker with beach name Montara State Beach found");
                        } else {
                            Log.e("Test", "Marker with beach name Montara State Beach not found");
                        }
                    }, 2000);
                });
            }
        });

        Thread.sleep(5000);

        // Wait for BeachDetailActivity to launch
        Thread.sleep(3000);

        // Switch from Overview to Reviews Tab

        ViewInteraction materialTextView = onView(
                allOf(withId(R.id.tab_reviews), withText("Reviews"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.beach_view),
                                        2),
                                1)));
        materialTextView.perform(scrollTo(), click());


        Thread.sleep(3000);

        ViewInteraction linearLayout = onView(
                childAtPosition(
                        allOf(withId(R.id.beach_view),
                                childAtPosition(
                                        withClassName(is("android.widget.ScrollView")),
                                        0)),
                        5));
        linearLayout.perform(scrollTo(), click());

        Thread.sleep(3000);

        // Post a review with empty fields
        ViewInteraction materialButton3 = onView(
                allOf(withId(R.id.review_submit_button), withText("Post"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1),
                                1),
                        isDisplayed()));
        materialButton3.perform(click());

        Thread.sleep(3000);

        // Post a review with non empty fields
        ViewInteraction appCompatEditText = onView(
                allOf(withId(R.id.review_text_input),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        0),
                                2),
                        isDisplayed()));
        appCompatEditText.perform(replaceText("Great Beach!"), closeSoftKeyboard());

        ViewInteraction materialButton4 = onView(
                allOf(withId(R.id.review_submit_button), withText("Post"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.RelativeLayout")),
                                        1),
                                1),
                        isDisplayed()));
        materialButton4.perform(click());
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }
}
