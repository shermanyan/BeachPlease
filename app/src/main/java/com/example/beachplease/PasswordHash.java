package com.example.beachplease;

import at.favre.lib.crypto.bcrypt.BCrypt;

public class PasswordHash {

    // default Constructor
    public PasswordHash() {}

    // Hashing User password implementation
    public String hashPassword(String password){
        return BCrypt.withDefaults().hashToString(12, password.toCharArray());
    }

    public boolean verifyPasswordForLogin(String inputPassword, String storedPassword){

        BCrypt.Result result = BCrypt.verifyer().verify(inputPassword.toCharArray(), storedPassword);
        return result.verified;  // return true if verified, false otherwise

    }
}