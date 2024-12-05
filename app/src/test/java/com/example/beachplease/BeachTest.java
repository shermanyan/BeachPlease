package com.example.beachplease;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BeachTest {

    @Mock
    private Parcel mockParcel;

    private Beach testBeach;

    @Before
    public void setUp() {
        // Initialize mock objects using Mockito
        MockitoAnnotations.initMocks(this);

        // Setup a test Beach object with dummy data
        List<String> tags = new ArrayList<>(Arrays.asList("sandy", "beautiful", "family-friendly"));
        List<String> hours = new ArrayList<>(Arrays.asList("8:00 AM - 6:00 PM", "9:00 AM - 5:00 PM"));
        Map<String, Integer> tagNumber = new HashMap<>();
        tagNumber.put("sandy", 5);
        tagNumber.put("beautiful", 8);

        testBeach = new Beach("1", "Sunny Beach", 34.0522, -118.2437, tags,
                "A beautiful beach.", "123 Beach St, Beach City", hours, tagNumber);
    }

    @Test
    public void testBeachParcel() {
        // Use doNothing() for void methods
        doNothing().when(mockParcel).writeString(testBeach.getId());
        doNothing().when(mockParcel).writeString(testBeach.getName());
        doNothing().when(mockParcel).writeDouble(testBeach.getLatitude());
        doNothing().when(mockParcel).writeDouble(testBeach.getLongitude());
        doNothing().when(mockParcel).writeStringList(testBeach.getTags());
        doNothing().when(mockParcel).writeString(testBeach.getDescription());
        doNothing().when(mockParcel).writeString(testBeach.getFormattedAddress());
        doNothing().when(mockParcel).writeStringList(testBeach.getHours());
        doNothing().when(mockParcel).writeMap(testBeach.getTagNumber());

        // Call the writeToParcel method
        testBeach.writeToParcel(mockParcel, 0);

        // Verify the interactions
        verify(mockParcel).writeString(testBeach.getId());
        verify(mockParcel).writeString(testBeach.getName());
        verify(mockParcel).writeDouble(testBeach.getLatitude());
        verify(mockParcel).writeDouble(testBeach.getLongitude());
        verify(mockParcel).writeStringList(testBeach.getTags());
        verify(mockParcel).writeString(testBeach.getDescription());
        verify(mockParcel).writeString(testBeach.getFormattedAddress());
        verify(mockParcel).writeStringList(testBeach.getHours());
        verify(mockParcel).writeMap(testBeach.getTagNumber());
    }
}