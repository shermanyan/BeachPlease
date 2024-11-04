package com.example.beachplease;

import android.util.Log;

public class UserSession {
    private static User currentUser;

    public static void login(User user) {

        if (user != null){
            Log.d("UserSession", "Logging in user: " + user.getId() + ", " + user.getUserName());
            currentUser = user;
        }else{
            Log.e("UserSession", "Attempted to login with null user");
        }
    }

    public static void logout() {
        currentUser = null;
    }

    public static User getCurrentUser() {
        if (currentUser == null) {
            Log.d("UserSession", "getCurrentUser: No user logged in");
        } else {
            Log.d("UserSession", "getCurrentUser: " + currentUser.getId() + ", " + currentUser.getUserName());
        }
        return currentUser;
    }
}