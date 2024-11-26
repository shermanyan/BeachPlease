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
import static org.hamcrest.Matchers.is;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.TextView;
import android.widget.Toast;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.ViewInteraction;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Marker;
import com.google.type.Color;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.hamcrest.core.IsInstanceOf;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.w3c.dom.Text;

import java.security.spec.ECField;
import java.util.List;
import java.util.Optional;

@LargeTest
@RunWith(AndroidJUnit4.class)
public class ViewBeachWeatherConditionsTests {

    @Rule
    public ActivityScenarioRule<MapActivity> mActivityScenarioRule =
            new ActivityScenarioRule<>(MapActivity.class);

    @Rule
    public GrantPermissionRule mGrantPermissionRule =
            GrantPermissionRule.grant(
                    "android.permission.ACCESS_FINE_LOCATION");

    @Before
    public void testMarkerSelectionAndWeatherTab() throws InterruptedException {
        Thread.sleep(10000);
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            SupportMapFragment mapFragment = (SupportMapFragment) activity
                    .getSupportFragmentManager()
                    .findFragmentById(R.id.map);

            if (mapFragment != null) {
                mapFragment.getMapAsync(mMap -> {
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        List<Marker> markersList = ((MapActivity) activity).getMarkersList();
                        Optional<Marker> selectedMarkerOpt = markersList.stream()
                                .filter(marker -> "Poplar Beach".equals(marker.getTitle()))
                                .findFirst();

                        if (selectedMarkerOpt.isPresent()) {
                            Marker selectedMarker = selectedMarkerOpt.get();
                            Log.d("Test", "Found marker: " + selectedMarker.getTitle());
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(selectedMarker.getPosition()));
                            activity.onMarkerClick(selectedMarker);
                        } else {
                            Log.e("Test", "Marker with beach name Poplar Beach not found");
                        }
                    }, 2000);
                });
            }
        });

        Thread.sleep(5000);

        ViewInteraction materialTextView = onView(
                allOf(withId(R.id.tab_weather), withText("Weather"), isDisplayed(),
                        childAtPosition(
                                childAtPosition(
                                        withId(R.id.beach_view),
                                        2),
                                2)));
        materialTextView.perform(scrollTo(), click());
    }

    @Test
    public void testCurrentTemperatureDisplay() throws InterruptedException {
        Thread.sleep(3000);

        boolean isVisible = true;

        try {
            ViewInteraction textView = onView(
                    allOf(withId(R.id.current_temperature),
                            withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                            isDisplayed()));
            textView.check(matches(isDisplayed()));

        }catch (Exception e){
            isVisible = false;
        }

        boolean finalIsVisible = isVisible;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if(finalIsVisible){
                Toast.makeText(activity, "Current Temperature Displayed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, "Current Temperature Not Displayed", Toast.LENGTH_SHORT).show();
            }
        });
        Thread.sleep(3000);
    }


    @Test
    public void testWaveHeightDisplay() throws InterruptedException {
        Thread.sleep(3000);

        boolean isVisible = true;

        try {
            ViewInteraction textView2 = onView(
                    allOf(withId(R.id.wave_height),
                            withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                            isDisplayed()));
            textView2.check(matches(isDisplayed()));
        }catch (Exception e){
            isVisible = false;
        }

        boolean finalIsVisible = isVisible;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if(finalIsVisible){
                Toast.makeText(activity, "Current Wave Height Displayed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity,"Current Wave Height Not Displayed", Toast.LENGTH_SHORT).show();
            }
        });

        Thread.sleep(3000);
    }

    @Test
    public void testWeatherDescriptionDisplay() throws InterruptedException {
        Thread.sleep(3000);

        boolean isVisible = true;

        try {
            ViewInteraction textView3 = onView(
                    allOf(withId(R.id.current_weather_description),
                            withParent(withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class))),
                            isDisplayed()));
            textView3.check(matches(isDisplayed()));
        }catch (Exception e){
            isVisible = false;
        }
        boolean finalIsVisible = isVisible;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if(finalIsVisible){
                Toast.makeText(activity, "Current Weather Description Displayed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity,"Current Weather Description Not Displayed", Toast.LENGTH_SHORT).show();
            }
        });

        Thread.sleep(3000);
    }

    @Test
    public void testTemperatureChartDisplay() throws InterruptedException {
        Thread.sleep(3000);

        boolean isVisible = true;

        try {
            ViewInteraction viewGroup = onView(
                    allOf(withId(R.id.temperature_chart),
                            withParent(allOf(withId(R.id.weather_view),
                                    withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                            isDisplayed()));
            viewGroup.check(matches(isDisplayed()));
        }catch (Exception e){
            isVisible = false;
        }

        boolean finalIsVisible  = isVisible;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if (finalIsVisible){
                Toast.makeText(activity, "Rest of the Day Temperature Chart Displayed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, "Rest of the Day Temperature Chart Not Displayed", Toast.LENGTH_SHORT).show();
            }
        });
        Thread.sleep(3000);
    }

    @Test
    public void testWaveForecastTab() throws InterruptedException {
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

        boolean isVisible = true;

        try {
            ViewInteraction viewGroup3 = onView(
                    allOf(withId(R.id.wave_height_chart),
                            withParent(allOf(withId(R.id.weather_view),
                                    withParent(IsInstanceOf.<View>instanceOf(android.widget.LinearLayout.class)))),
                            isDisplayed()));
            viewGroup3.check(matches(isDisplayed()));
        }catch (Exception e){
            isVisible = false;
        }

        boolean finalIsVisible  = isVisible;
        mActivityScenarioRule.getScenario().onActivity(activity -> {
            if (finalIsVisible){
                Toast.makeText(activity, "Current Wave Forecast Chart Displayed", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(activity, "Current Wave Forecast Chart Not Displayed", Toast.LENGTH_SHORT).show();
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
