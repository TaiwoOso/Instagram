package com.example.instagram;

import android.app.Application;

import com.example.instagram.models.Post;
import com.example.instagram.models.User;
import com.parse.Parse;
import com.parse.ParseObject;
import com.parse.ParseUser;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        // Register parse models
        ParseObject.registerSubclass(Post.class);

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("5OkG8Y21sbhDcI87G5eGdYlY14nNZcXiTuSxkWDl")
                .clientKey("7eT0zxpUT8QhX18xpINuu42XWgOWwribnNCp3ktz")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
