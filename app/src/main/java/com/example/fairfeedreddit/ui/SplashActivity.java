package com.example.fairfeedreddit.ui;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

import androidx.appcompat.app.AppCompatActivity;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.ui.login.LoginActivity;
import com.example.fairfeedreddit.utils.SharedPreferenceUtils;
import com.google.android.material.button.MaterialButton;

import net.dean.jraw.RedditClient;

import java.lang.ref.WeakReference;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AppCompatActivity {
    private static final int UI_ANIMATION_DELAY = 800;
    private final Handler handler = new Handler();

    @BindView(R.id.splash_pg)
    ProgressBar progressBar;

    @BindView(R.id.splash_login_btn)
    MaterialButton loginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);

        SharedPreferenceUtils.initialize(getApplicationContext());

        handler.postDelayed(() -> {
            List<String> usernames = App.getTokenStore().getUsernames();
            if (SharedPreferenceUtils.isUserLoggedIn() && !usernames.isEmpty()) {
                new ReAuthenticationTask(SplashActivity.this).execute(usernames.get(0));
            } else {
                showLoginButton();
            }
        }, UI_ANIMATION_DELAY);

        loginButton.setOnClickListener((v) -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private void showLoginButton() {
        progressBar.setVisibility(View.INVISIBLE);
        loginButton.setVisibility(View.VISIBLE);
    }

    private static class ReAuthenticationTask extends AsyncTask<String, Void, RedditClient> {
        private final WeakReference<SplashActivity> activityRef;

        ReAuthenticationTask(SplashActivity activity) {
            this.activityRef = new WeakReference<>(activity);
        }

        @Override
        protected RedditClient doInBackground(String... usernames) {
            return App.getAccountHelper().trySwitchToUser(usernames[0]);
        }

        @Override
        protected void onPostExecute(RedditClient redditClient) {
            SplashActivity activity = this.activityRef.get();
            if (activity != null) {
                if (redditClient != null) {
                    activity.navigateToMainActivity();
                } else {
                    activity.showLoginButton();
                }
            }
        }
    }
}
