package com.foreverrafs.starfm.util;

// Created by Emperor95 on 11/19/2018.

import android.content.Context;
import android.content.SharedPreferences;

public class PrefManager {
    SharedPreferences pref;
    SharedPreferences.Editor editor;
    Context _context;

    // shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "MY_APPLICATION";

    private static final String IS_FIRST_TIME_LAUNCH = "IsFirstTimeLaunch";
    private static final String STATUS = "status_set";

    public PrefManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }

    public void setFirstTimeLaunch(boolean isFirstTime) {
        editor.putBoolean(IS_FIRST_TIME_LAUNCH, isFirstTime);
        editor.commit();
    }

    public boolean isFirstTimeLaunch() {
        return pref.getBoolean(IS_FIRST_TIME_LAUNCH, true);
    }

    public boolean isStatusSet() {
        return pref.contains(STATUS);
    }

    public String getStatus() {
        return pref.getString(STATUS, "");
    }

    public void setStatus(String status) {
        editor.putString(STATUS, status);
        editor.commit();
    }

    /**
     * User can decide to start the audio stream immediately the app is launched or not
     * @param value
     */
    public void setAutoPlayOnStart(boolean value) {
        editor.putBoolean(Constants.AUTOPLAY_ON_START, value);
        editor.commit();
    }


    public boolean getAutoPlayOnStart() {
        return pref.getBoolean(Constants.AUTOPLAY_ON_START, true);
    }
}
