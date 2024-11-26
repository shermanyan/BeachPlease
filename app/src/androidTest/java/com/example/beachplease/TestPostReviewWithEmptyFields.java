package com.example.beachplease;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withClassName;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.ViewInteraction;
import androidx.test.espresso.intent.Intents;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.auth.FirebaseAuth;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class TestPostReviewWithEmptyFields {

    private FirebaseAuth mAuth;
    @Rule
    public ActivityScenarioRule<MapActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant("android.permission.ACCESS_FINE_LOCATION");

    @Before
    public void setup() {
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }

    @Before
    public void navigateToReviewsTab() throws InterruptedException {
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

        // Simulate marker click and navigate to BeachDetailActivity
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(mMap -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        List<Marker> markersList = ((MapActivity) activity).getMarkersList();

                        Optional<Marker> selectedMarkerOpt = markersList.stream()
                                .filter(marker -> "Montara State Beach".equals(marker.getTitle()))
                                .findFirst();

                        if (selectedMarkerOpt.isPresent()) {
                            Marker selectedMarker = selectedMarkerOpt.get();
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));
                            activity.onMarkerClick(selectedMarker);
                        }
                    }, 2000);
                });
            }
        });

        Thread.sleep(5000);

        // Switch to Reviews Tab
        ViewInteraction reviewsTab = onView(
                allOf(withId(R.id.tab_reviews), withText("Reviews")));
        reviewsTab.perform(scrollTo(), click());

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
    }


    @Test
    public void testPostReviewWithEmptyFields() throws InterruptedException {
        Thread.sleep(5000);

        boolean canAddReview = false;

        // Attempt to post a review with empty fields
        try {
            ViewInteraction postButton = onView(
                    allOf(withId(R.id.review_submit_button), withText("Post"),
                            isDisplayed()));
            postButton.perform(click());
        } catch (Exception e) {
            canAddReview = true;
        }

        boolean finalCanAddReview = canAddReview;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if (!finalCanAddReview) {
                Toast.makeText(activity, "Test Passed! Failed to add empty review", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(activity, "Test Failed!", Toast.LENGTH_SHORT).show();

            }
        });

        Thread.sleep(3000);
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