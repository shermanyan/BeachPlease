package com.example.beachplease;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class ExampleUnitTest {

    private SignUpActivity signUpActivity;

    @Before
    public void setup(){
        signUpActivity = new SignUpActivity();
    }

    // Email Validation
    @Test
    public void testEmailValidation_Valid() {
        String email1 = "sample@example.com";
        String email2 = "aj@yahoo.com";
        String email3 = "johnson@hotmail.com";
        String email4 = "Trojans123@gmail.com";
        String email5 = "csci310@usc.edu";
        assertTrue("email1 should be accepted",
                signUpActivity.isValidEmail(email1));
        assertTrue("email2 should be accepted",
                signUpActivity.isValidEmail(email2));
        assertTrue("email3 should be accepted",
                signUpActivity.isValidEmail(email3));
        assertTrue("email4 should be accepted",
                signUpActivity.isValidEmail(email4));
        assertTrue("email5 should be accepted",
                signUpActivity.isValidEmail(email5));

    }

    @Test
    public void testEmailValidation_Invalid() {
        String email1 = "invalid.email@";
        String email2 = "@@noway.com";
        String email3 = "mjgmail.com";
        String email4 = "com@.com@noway@gmail.com";
        String email5 = "figurethisout";

        assertFalse("email1 should be rejected",
                signUpActivity.isValidEmail(email1));
        assertFalse("email2 should be rejected",
                signUpActivity.isValidEmail(email2));
        assertFalse("email3 should be rejected",
                signUpActivity.isValidEmail(email3));
        assertFalse("email4 should be rejected",
                signUpActivity.isValidEmail(email4));
        assertFalse("email5 should be rejected",
                signUpActivity.isValidEmail(email5));
    }

    // Password Validation
    @Test
    public void testPasswordValidation() {
        // Less than 6 characters
        String pass1 = "pass";
        String pass2 = "1";
        String pass3 = "123";

        // More than 6 characters
        String pass4 = "password";
        String pass5 = "itworks";
        String pass6 = "qwertyasdfg";

        assertFalse("Password too short",
                pass1.length() >= 6);
        assertFalse("Password too short",
                pass2.length() >= 6);
        assertFalse("Password too short",
                pass3.length() >= 6);

        assertTrue("Valid password",
                pass4.length() >= 6);
        assertTrue("Valid password",
                pass5.length() >= 6);
        assertTrue("Valid password",
                pass6.length() >= 6);

    }

    // Name Validation, should apply for both First and Last names
    @Test
    public void testNameValidation() {
        String name1 = "John";
        String name2 = "Doe";
        String name3 = "john3";
        String name4 = "123";
        String name5 = "TrojAn";
        String name6 = "trojaNNN";
        String name7 = "JANE";
        String name8 = "Robert";

        String regex = "^[A-Z][a-z]*$";

        assertTrue("Valid: matches all criteria",
                name1.matches(regex));

        assertTrue("Valid: matches all criteria",
                name2.matches(regex));

        assertFalse("Invalid: name is lowercase and contains a number",
                name3.matches(regex));

        assertFalse("Invalid: name consists of numbers only",
                name4.matches(regex));

        assertFalse("Invalid: name contains more than 1 uppercase letter",
                name5.matches(regex));

        assertFalse("Invalid: name starts with lowercase and contains more than 1 uppercase letter",
                name6.matches(regex));

        assertFalse("Invalid: name is all uppercase",
                name7.matches(regex));

        assertTrue("Valid: matches all criteria",
                name8.matches(regex));
    }





}
