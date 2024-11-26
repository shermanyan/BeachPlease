package com.example.beachplease;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import android.os.Parcel;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReviewTest {

    @Mock
    private Parcel mockParcel;

    private Review testReview;

    @Before
    public void setUp() {
        // Initialize mock objects using Mockito
        MockitoAnnotations.initMocks(this);

        // Setup a test Review object with dummy data
        List<String> imageUrls = Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg");
        testReview = new Review("John Doe", "1", "2024-11-25", "Great beach!", 5.0f, "user123");
        testReview.setImageUrls(imageUrls);
        testReview.setReviewId("review123");
    }

    @Test
    public void testReadFromParcel() {
        // Simulate the behavior of the Parcel when reading values
        when(mockParcel.readString()).thenReturn(testReview.getBeachId())
                .thenReturn(testReview.getDate())
                .thenReturn(testReview.getReviewText())
                .thenReturn(testReview.getUserId())
                .thenReturn(testReview.getUsername())
                .thenReturn(testReview.getReviewId());
        when(mockParcel.readFloat()).thenReturn(testReview.getStars());
        when(mockParcel.createStringArrayList()).thenReturn((ArrayList<String>) testReview.getImageUrls());

        // Call the constructor that reads from the Parcel
        Review result = new Review(mockParcel);

        // Verify the values were correctly set
        assertEquals(testReview.getBeachId(), result.getBeachId());
        assertEquals(testReview.getDate(), result.getDate());
        assertEquals(testReview.getReviewText(), result.getReviewText());
        assertEquals(testReview.getStars(), result.getStars(), 0.0f);
        assertEquals(testReview.getUserId(), result.getUserId());
        assertEquals(testReview.getUsername(), result.getUsername());
        assertEquals(testReview.getReviewId(), result.getReviewId());
        assertEquals(testReview.getImageUrls(), result.getImageUrls());
    }
}
