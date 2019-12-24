package com.example.fairfeedreddit;

import android.app.Application;
import android.util.Log;

import net.dean.jraw.android.AndroidHelper;
import net.dean.jraw.android.AppInfoProvider;
import net.dean.jraw.android.ManifestAppInfoProvider;
import net.dean.jraw.android.SharedPreferencesTokenStore;
import net.dean.jraw.android.SimpleAndroidLogAdapter;
import net.dean.jraw.http.LogAdapter;
import net.dean.jraw.http.SimpleHttpLogger;
import net.dean.jraw.oauth.AccountHelper;

import java.util.UUID;

// Credit to JRAW-Android library's example app for OAuth2.0 flow: https://github.com/mattbdean/JRAW-Android/tree/8d92fd555d165e98ae921994683888523d94f268/example-app
public final class App extends Application {

    private static AccountHelper accountHelper;
    private static SharedPreferencesTokenStore tokenStore;

    @Override
    public void onCreate() {
        super.onCreate();

        initializeTokenStore();

        initializeAccountHelper();
    }

    private void initializeTokenStore() {
        tokenStore = new SharedPreferencesTokenStore(getApplicationContext());
        tokenStore.load();
        tokenStore.setAutoPersist(true);
    }

    private void initializeAccountHelper() {
        AppInfoProvider provider = new ManifestAppInfoProvider(getApplicationContext());
        UUID deviceUuid = UUID.randomUUID();
        accountHelper = AndroidHelper.accountHelper(provider, deviceUuid, tokenStore);
        accountHelper.onSwitch(redditClient -> {
            LogAdapter logAdapter = new SimpleAndroidLogAdapter(Log.INFO);
            redditClient.setLogger(
                    new SimpleHttpLogger(SimpleHttpLogger.DEFAULT_LINE_LENGTH, logAdapter));
            return null;
        });
    }

    public static AccountHelper getAccountHelper() {
        return accountHelper;
    }

    public static SharedPreferencesTokenStore getTokenStore() {
        return tokenStore;
    }
}
