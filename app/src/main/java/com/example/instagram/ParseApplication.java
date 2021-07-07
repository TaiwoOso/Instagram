package com.example.instagram;

import android.app.Application;

import com.parse.Parse;

public class ParseApplication extends Application {

    // Initializes Parse SDK as soon as the application is created
    @Override
    public void onCreate() {
        super.onCreate();

        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("5OkG8Y21sbhDcI87G5eGdYlY14nNZcXiTuSxkWDl")
                .clientKey("7eT0zxpUT8QhX18xpINuu42XWgOWwribnNCp3ktz")
                .server("https://parseapi.back4app.com")
                .build()
        );
    }
}
