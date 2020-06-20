package com.rafaelescaleira.droidnews.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class UserModel {

    public String email;
    public String name;

    public UserModel() {}

    public UserModel(String email, String name) {
        this.email = email;
        this.name = name;
    }

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("email", email);
        result.put("name", name);

        return result;
    }
}
