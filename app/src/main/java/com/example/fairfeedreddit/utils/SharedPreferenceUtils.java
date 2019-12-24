package com.example.fairfeedreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class SharedPreferenceUtils {
    private static SharedPreferences preferences;

    private static final String IS_USER_LOGGED_IN = "is_user_logged_in";

    public static void initialize(Context context) {
        if (preferences == null) {
            synchronized (SharedPreferenceUtils.class) {
                if (preferences == null) {
                    preferences = PreferenceManager.getDefaultSharedPreferences(context);
                }
            }
        }
    }

    public static boolean isUserLoggedIn() {
        return preferences.getBoolean(IS_USER_LOGGED_IN, false);
    }

    public static void setIsUserLogged(boolean isUserLogged) {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(IS_USER_LOGGED_IN, isUserLogged);
        editor.apply();
    }
}
