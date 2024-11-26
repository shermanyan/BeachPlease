package com.example.beachplease;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mockStatic;

import android.util.Log;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockedStatic;

public class UserSessionTest {

    private User testUser;

    @Before
    public void setUp() {
        testUser = new User(
                "Test",
                "User",
                "testuser",
                "test@example.com",
                "12345"
        );
    }

    @After
    public void tearDown() {
        UserSession.logout();
    }

    @Test
    public void testLoginWithValidUser() {
        UserSession.login(testUser);

        assertEquals(testUser, UserSession.getCurrentUser());
    }

    @Test
    public void testLoginWithNullUser() {

        try (MockedStatic<Log> mockedLog = mockStatic(Log.class)) {
            mockedLog.when(() -> Log.e(anyString(), anyString())).thenReturn(0);

            UserSession.login(null);

            mockedLog.verify(() -> Log.e(eq("UserSession"), eq("Attempted to login with null user")));

            assertNull(UserSession.getCurrentUser());
        }
    }

    @Test
    public void testLogout() {
        UserSession.login(testUser);
        UserSession.logout();
        assertNull(UserSession.getCurrentUser());
    }

    @Test
    public void testGetCurrentUserWhenNotLoggedIn() {
        assertNull(UserSession.getCurrentUser());
    }
}
