package com.example.fairfeedreddit.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.example.fairfeedreddit.App;

import java.util.List;

import static com.example.fairfeedreddit.utils.AppConstants.DEFAULT_SHOW_LESS_OFTEN_POSTS;
import static com.example.fairfeedreddit.utils.AppConstants.SHOW_LESS_OFTEN_POSTS_FOR_X_KEY;

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

    public static int getShowLessOftenPosts() {
        List<String> usernames = App.getTokenStore().getUsernames();
        if (!usernames.isEmpty()) {
            String showLessOftenPostsKey = String.format(SHOW_LESS_OFTEN_POSTS_FOR_X_KEY, usernames.get(0));
            return preferences.getInt(showLessOftenPostsKey, DEFAULT_SHOW_LESS_OFTEN_POSTS);
        }
        return DEFAULT_SHOW_LESS_OFTEN_POSTS;
    }

    public static void setShowLessOftenPosts(int postsPerDay) {
        List<String> usernames = App.getTokenStore().getUsernames();
        if (!usernames.isEmpty()) {
            String showLessOftenPostsKey = String.format(SHOW_LESS_OFTEN_POSTS_FOR_X_KEY, usernames.get(0));
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(showLessOftenPostsKey, postsPerDay);
            editor.apply();
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
