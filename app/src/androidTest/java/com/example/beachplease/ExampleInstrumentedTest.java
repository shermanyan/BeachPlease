package com.example.beachplease;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import static org.hamcrest.core.IsNot.not;

import androidx.test.espresso.Espresso;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.firebase.auth.FirebaseAuth;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */



@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    @Rule
    public ActivityScenarioRule<LoginActivity> activityRule =
            new ActivityScenarioRule<>(LoginActivity.class);

    private FirebaseAuth mAuth;

    @Before
    public void setup(){
        Intents.init();
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null){
            mAuth.signOut();
        }
    }

    @After
    public void clean(){
        Intents.release();

        if(mAuth.getCurrentUser() != null) {
            mAuth.signOut();
        }
    }


    @Test
    public void testEmptyFieldsValidation() throws InterruptedException {

        // Clear inputs to simulate empty fields
        onView(withId(R.id.loginEmailInput)).perform(clearText());
        onView(withId(R.id.loginPasswordInput)).perform(clearText());

        // Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);

        // Check if we are still in the login activity
        // if user is not redirected to map activity, login failed
        onView(withId(R.id.loginEmailInput))
                .check(matches(isDisplayed()));
    }


    @Test
    public void testSemiEmptyFields() throws InterruptedException {
        //  part 1: user entered only email
        onView(withId(R.id.loginEmailInput)).perform(typeText("mj@gmail.com"));
        onView(withId(R.id.loginPasswordInput)).perform(clearText());  // clear password field

        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2000); // Wait for Toast

        // if user is not redirected to map activity, login failed
        onView(withId(R.id.loginEmailInput)).check(matches(isDisplayed()));

        // part 2: user entered only password
        onView(withId(R.id.loginPasswordInput)).perform(typeText("samplepassword"));
        onView(withId(R.id.loginEmailInput)).perform(clearText()); // clear email field

        onView(withId(R.id.loginButton)).perform(click());
        Thread.sleep(2000); // Wait for Toast


         // if user is not redirected to map activity, login failed
        onView(withId(R.id.loginPasswordInput)).check(matches(isDisplayed()));
    }

    @Test
    public void testLoginWithCorrectCredentials() throws InterruptedException {
        // Enter correct email and password
        onView(withId(R.id.loginEmailInput)).perform(typeText("mj@gmail.com"));
        onView(withId(R.id.loginPasswordInput)).perform(typeText("heehee"));

        // Close the keyboard to simulate user behavior
        Espresso.closeSoftKeyboard();

        // Click the login button
        onView(withId(R.id.loginButton)).perform(click());

        Thread.sleep(2000);


        // Verify that the MapActivity is displayed
        onView(withId(R.id.map))
                .check(matches(isDisplayed()));
    }



}