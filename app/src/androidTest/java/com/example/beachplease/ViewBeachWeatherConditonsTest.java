package com.example.beachplease;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Toast;

import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Optional;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewBeachWeatherConditonsTest {

    @Rule
    public ActivityScenarioRule<MapActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Test
    public void viewBeachWeatherConditonsTest() throws InterruptedException {

        Thread.sleep(10000);
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
                                .filter(marker -> "Poplar Beach".equals(marker.getTitle()))
                                .findFirst();

                        if (selectedMarkerOpt.isPresent()) {
                            Marker selectedMarker = selectedMarkerOpt.get();
                            Log.d("Test", "Found marker: " + selectedMarker.getTitle());

                            // Move camera to marker position
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));

                            // call marker click method from map activity
                            activity.onMarkerClick(selectedMarker);

                            Log.d("Test", "Marker with beach name Poplar Beach found");
                        } else {
                            Log.e("Test", "Marker with beach name Poplar Beach not found");
                        }
                    }, 2000);
                });
            }
        });

        mActivityScenarioRule.getScenario().onActivity(activity -> {
            // Simulate showing Toast for retrieved temperature
//            String temperature = "53°F"; // This can be retrieved from a TextView during the test
//            Toast.makeText(activity, "Temperature: " + temperature + " exists", Toast.LENGTH_SHORT).show();

        });


        Thread.sleep(5000);
        ViewInteraction materialTextView = onView(
                allOf(withId(R.id.tab_weather), withText("Weather"),isDisplayed(),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.beach_view),
                                        2),
                                2)));
        materialTextView.perform(scrollTo(), click());

        Thread.sleep(3000);

        ViewInteraction textView = onView(
                allOf(withId(R.id.current_temperature),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                        isDisplayed()));
        textView.check(matches(isDisplayed()));
        // Verify Current Temperature is displayed
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            Toast.makeText(activity, "Current Temperature is displayed", Toast.LENGTH_SHORT).show();
        });

        Thread.sleep(3000);

        ViewInteraction textView2 = onView(
                allOf(withId(R.id.wave_height),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                        isDisplayed()));
        textView2.check(matches(isDisplayed()));
        // Verify Current Temperature is displayed
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            Toast.makeText(activity, "Current Wave Height is displayed", Toast.LENGTH_SHORT).show();
        });

        Thread.sleep(3000);

        ViewInteraction textView3 = onView(
                allOf(withId(R.id.current_weather_description),
                        withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                        isDisplayed()));
        textView3.check(matches(isDisplayed()));
        // Verify Current Temperature is displayed
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            Toast.makeText(activity, "Current Weather Description is displayed", Toast.LENGTH_SHORT).show();
        });

        Thread.sleep(3000);

        ViewInteraction viewGroup = onView(
                allOf(withId(R.id.temperature_chart),
                        withParent(allOf(withId(R.id.weather_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                        isDisplayed()));
        viewGroup.check(matches(isDisplayed()));
        // Verify Current Temperature is displayed
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            Toast.makeText(activity, "Temperature for rest of the day is displayed", Toast.LENGTH_SHORT).show();
        });
        Thread.sleep(3000);

        ViewInteraction viewGroup2 = onView(
                allOf(withId(R.id.temperature_chart),
                        withParent(allOf(withId(R.id.weather_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                        isDisplayed()));
        viewGroup2.check(matches(isDisplayed()));

        Thread.sleep(3000);

        ViewInteraction materialTextView2 = onView(
                allOf(withId(R.id.tab_waveheight), withText("Wave Forecast"),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.weather_view),
                                        2),
                                2),
                        isDisplayed()));
        materialTextView2.perform(click());
        // Verify Current Temperature is displayed
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            Toast.makeText(activity, "Current weather forecast is displayed", Toast.LENGTH_SHORT).show();
        });

        Thread.sleep(3000);

        ViewInteraction viewGroup3 = onView(
                allOf(withId(R.id.wave_height_chart),
                        withParent(allOf(withId(R.id.weather_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                        isDisplayed()));
        viewGroup3.check(matches(isDisplayed()));

        Thread.sleep(3000);

        ViewInteraction viewGroup4 = onView(
                allOf(withId(R.id.wave_height_chart),
                        withParent(allOf(withId(R.id.weather_view),
                                withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                        isDisplayed()));
        viewGroup4.check(matches(isDisplayed()));
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
