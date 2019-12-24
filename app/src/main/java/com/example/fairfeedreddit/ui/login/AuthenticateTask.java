package com.example.fairfeedreddit.ui.login;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;

import com.example.fairfeedreddit.ui.MainActivity;
import com.example.fairfeedreddit.utils.SharedPreferenceUtils;

import net.dean.jraw.oauth.OAuthException;
import net.dean.jraw.oauth.StatefulAuthHelper;

import java.lang.ref.WeakReference;

/**
 * An async task that takes a final redirect URL as a parameter and reports the success of
 * authorizing the user.
 */
public final class AuthenticateTask extends AsyncTask<String, Void, Boolean> {
    private final StatefulAuthHelper helper;
    private final WeakReference<Activity> activityRef;

    AuthenticateTask(Activity activity, StatefulAuthHelper helper) {
        this.helper = helper;
        this.activityRef = new WeakReference<>(activity);
    }

    @Override
    protected Boolean doInBackground(String... urls) {
        try {
            helper.onUserChallenge(urls[0]);
            return true;
        } catch (OAuthException e) {
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean success) {
        Activity loginActivity = this.activityRef.get();
        if (loginActivity != null) {
            SharedPreferenceUtils.setIsUserLogged(success);
            if (success) {
                loginActivity.startActivity(new Intent(loginActivity, MainActivity.class));
            }
            loginActivity.finish();
        }
    }
}