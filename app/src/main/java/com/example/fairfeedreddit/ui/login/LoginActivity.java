package com.example.fairfeedreddit.ui.login;

import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.google.firebase.analytics.FirebaseAnalytics;

import net.dean.jraw.oauth.StatefulAuthHelper;

import butterknife.BindView;
import butterknife.ButterKnife;


public class LoginActivity extends AppCompatActivity {

    @BindView(R.id.login_pb)
    ProgressBar progressBar;

    @BindView(R.id.login_wv)
    WebView webView;

    private static final String[] AUTH_SCOPES = new String[]{ "read", "identity", "mysubreddits", "subscribe"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        clearWebViewData();

        // Get a StatefulAuthHelper instance to manage interactive authentication
        final StatefulAuthHelper helper = App.getAccountHelper().switchToNewUser();

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                if (helper.isFinalRedirectUrl(url)) {
                    webView.stopLoading();
                    webView.setVisibility(View.GONE);
                    new AuthenticateTask(LoginActivity.this, helper).execute(url);
                    FirebaseAnalytics.getInstance(getApplicationContext()).logEvent(FirebaseAnalytics.Event.LOGIN, null);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                progressBar.setVisibility(View.GONE);
            }
        });

        String authUrl = helper.getAuthorizationUrl(true, true, AUTH_SCOPES);
        webView.loadUrl(authUrl);
    }

    private void clearWebViewData() {
        webView.clearCache(true);
        webView.clearHistory();
        webView.getSettings().setDomStorageEnabled(true);
        webView.setWebChromeClient(new WebChromeClient());

        // Credit: https://stackoverflow.com/a/31950789/5826864
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().removeAllCookies(null);
            CookieManager.getInstance().flush();
        } else {
            CookieSyncManager syncManager = CookieSyncManager.createInstance(getApplicationContext());
            syncManager.startSync();
            CookieManager cookieManager = CookieManager.getInstance();
            cookieManager.removeAllCookie();
            cookieManager.removeSessionCookie();
            syncManager.stopSync();
            syncManager.sync();
        }
    }

}
