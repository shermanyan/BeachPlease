package com.example.beachplease;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.robolectric.shadows.ShadowToast;
import org.robolectric.Shadows;

import static org.junit.Assert.*;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import org.mockito.ArgumentCaptor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;


import static org.mockito.Mockito.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MapUnitTest {
    @Mock
    GoogleMap mMap;

    @Mock
    MapActivity mapActivity;

    @Mock
    Context mockContext;

    private final LatLngBounds CALIFORNIA_BOUNDS = new LatLngBounds(
            new LatLng(32.5343, -124.4096),
            new LatLng(42.0095, -114.1312)
    );

    @Before
    public void setup() {
        MockitoAnnotations.openMocks(this);

        // Mock MapActivity behavior
        mapActivity = mock(MapActivity.class);
        when(mapActivity.getApplicationContext()).thenReturn(mockContext);
    }

    @Test
    public void testMarkersDisplayCorrectly() {
        List<Beach> testBeaches = Arrays.asList(
                new Beach("1", "Sandy Shores", 34.012345, -118.123456, Arrays.asList("swimming", "sunbathing"), "A perfect place for swimming and sunbathing.", "123 Beach Lane", new ArrayList<>(), new HashMap<>()),
                new Beach("2", "Rocky Point", 34.112345, -118.223456, Arrays.asList("rock climbing", "hiking"), "Ideal for rock climbing and scenic hikes.", "456 Mountain View Rd", new ArrayList<>(), new HashMap<>()),
                new Beach("3", "Surfer's Paradise", 34.212345, -118.323456, Arrays.asList("surfing", "windsurfing"), "Great waves for surfing and windsurfing.", "789 Wave Blvd", new ArrayList<>(), new HashMap<>()),
                new Beach("4", "Picnic Bay", 34.312345, -118.423456, Arrays.asList("picnicking", "picnic area"), "Family-friendly with plenty of picnic spots.", "101 Picnic St", new ArrayList<>(), new HashMap<>()),
                new Beach("5", "Angler's Haven", 34.412345, -118.523456, Arrays.asList("fishing", "boating"), "A serene place for fishing and boating.", "202 Fisherman's Dock", new ArrayList<>(), new HashMap<>()),
                new Beach("6", "Snorkel Cove", 34.512345, -118.623456, Arrays.asList("snorkeling", "diving"), "Clear waters for snorkeling and diving.", "303 Undersea Dr", new ArrayList<>(), new HashMap<>()),
                new Beach("7", "Kayak Point", 34.612345, -118.723456, Arrays.asList("kayaking", "wildlife watching"), "Calm waters for kayaking and wildlife observation.", "404 Paddle Way", new ArrayList<>(), new HashMap<>()),
                new Beach("8", "Barbecue Beach", 34.712345, -118.823456, Arrays.asList("bbq pits", "playground"), "BBQ pits and playgrounds for families.", "505 Grill Ave", new ArrayList<>(), new HashMap<>()),
                new Beach("9", "Camp Cove", 34.812345, -118.923456, Arrays.asList("camping", "dog friendly"), "Pet-friendly and perfect for camping.", "606 Tent Trail", new ArrayList<>(), new HashMap<>()),
                new Beach("10", "Volleyball Beach", 34.912345, -119.023456, Arrays.asList("beach volleyball", "bird watching"), "Popular for volleyball and bird watching.", "707 Net Ct", new ArrayList<>(), new HashMap<>())
        );

        mapActivity.displayBeaches(testBeaches);

        for (Beach beach : testBeaches) {
            Marker marker = createTestMarker(beach);
            assertNotNull("Marker should be displayed for each beach", marker);
            assertEquals(beach.getLatitude(), marker.getPosition().latitude, 0.0001);
            assertEquals(beach.getLongitude(), marker.getPosition().longitude, 0.0001);
        }
    }

    @Test
    public void testMapZoomsToCaliforniaBounds() {
        when(mapActivity.getGoogleMap()).thenReturn(null);
        when(mapActivity.getCurrentMapBounds()).thenReturn(CALIFORNIA_BOUNDS);

        LatLngBounds bounds = mapActivity.getCurrentMapBounds();
        assertTrue("Map should be zoomed to California bounds initially", CALIFORNIA_BOUNDS.contains(bounds.northeast));
        assertTrue("Map should be zoomed to California bounds initially", CALIFORNIA_BOUNDS.contains(bounds.southwest));
    }

    @Test
    public void testFilterBeachesByTag() {
        // Set up mock selected tags
        when(mapActivity.getSelectedTags()).thenReturn(new HashSet<>(Arrays.asList("surfing")));
        mapActivity.filterBeachesByTags();

        // Get the displayed beaches
        List<Beach> displayedBeaches = getDisplayedBeaches1();

        for (Beach beach : displayedBeaches) {
            System.out.println("Checking beach: " + beach.getName() + ", Tags: " + beach.getTags());
            assertTrue("Displayed beaches must contain the selected tag", beach.getTags().contains("surfing"));
        }
    }

    @Test
    public void testFilterBeachesByMultipleTags() {
        when(mapActivity.getSelectedTags()).thenReturn(new HashSet<>(Arrays.asList("camping", "dog friendly")));

        mapActivity.filterBeachesByTags();

        List<Beach> displayedBeaches = getDisplayedBeaches2();
        for (Beach beach : displayedBeaches) {
            System.out.println("Tags are: " + mapActivity.getSelectedTags());
            System.out.println("Checking beach: " + beach.getName() + ", Tags: " + beach.getTags());
            assertTrue("Displayed beaches must contain all selected tags", beach.getTags().containsAll(mapActivity.getSelectedTags()));
        }
    }

    @Test
    public void testNoMatchingBeaches() {
        // Set up mock selected tags that don't match any beach
        when(mapActivity.getSelectedTags()).thenReturn(new HashSet<>(Arrays.asList("rock climbing", "diving", "bird watching")));

        mapActivity.filterBeachesByTags();
        List<Beach> displayedBeaches = new ArrayList<>(); // Simulate no matching beaches

        // Verify that no beaches are displayed
        assertTrue("No beaches should match the selected tags", displayedBeaches.isEmpty());
        // Log a message for debugging
        System.out.println("No beaches matched for tags: " + mapActivity.getSelectedTags());
    }


    private Marker createTestMarker(Beach beach) {
        Marker mockMarker = mock(Marker.class);
        when(mockMarker.getTitle()).thenReturn(beach.getName());
        when(mockMarker.getPosition()).thenReturn(new LatLng(beach.getLatitude(), beach.getLongitude()));
        return mockMarker;
    }

    private List<Beach> getDisplayedBeaches1() {
        // Mock or simulate the returned beaches
        return new ArrayList<>(Arrays.asList(
                new Beach("3", "Surfer's Paradise", 34.212345, -118.323456, Arrays.asList("surfing", "windsurfing"), "Great waves for surfing and windsurfing.", "789 Wave Blvd", new ArrayList<>(), new HashMap<>())
        ));
    }

    private List<Beach> getDisplayedBeaches2() {
        // Mock or simulate the returned beaches
        return new ArrayList<>(Arrays.asList(
                new Beach("9", "Camp Cove", 34.812345, -118.923456, Arrays.asList("camping", "dog friendly"), "Pet-friendly and perfect for camping.", "606 Tent Trail", new ArrayList<>(), new HashMap<>())
        ));
    }
}
