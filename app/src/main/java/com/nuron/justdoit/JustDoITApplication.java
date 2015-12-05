package com.nuron.justdoit;

import android.app.Application;

import com.parse.Parse;
import com.parse.ParseFacebookUtils;

/**
 * Created by nuron on 29/11/15.
 */
public class JustDoITApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, getResources().getString(R.string.PARSE_APPLICATION_ID),
                getResources().getString(R.string.PARSE_CLIENT_ID));
        ParseFacebookUtils.initialize(this);
    }

}