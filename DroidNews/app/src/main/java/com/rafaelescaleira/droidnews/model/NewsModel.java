package com.rafaelescaleira.droidnews.model;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class NewsModel {

    public String uuid;
    public String user;
    public String tag;
    public String date;
    public String image;
    public String title;
    public String message;

    public NewsModel(String uuid, String user, String tag, String date, String image, String title, String message) {
        this.uuid = uuid;
        this.user = user;
        this.tag = tag;
        this.date = date;
        this.image = image;
        this.title = title;
        this.message = message;
    }

    public NewsModel() {}

    @Exclude
    public Map<String, Object> toMap() {

        HashMap<String, Object> result = new HashMap<>();
        result.put("uuid", uuid);
        result.put("user", user);
        result.put("tag", tag);
        result.put("date", date);
        result.put("image", image);
        result.put("title", title);
        result.put("message", message);

        return result;
    }
}
