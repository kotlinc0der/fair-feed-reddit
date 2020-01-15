package com.example.fairfeedreddit.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.database.AppDatabase;
import com.example.fairfeedreddit.utils.AppExecutors;
import com.example.fairfeedreddit.utils.SharedPreferenceUtils;
import com.google.android.material.snackbar.Snackbar;

import java.util.Objects;

import static com.example.fairfeedreddit.utils.AppConstants.DEFAULT_SHOW_LESS_OFTEN_POSTS;
import static com.example.fairfeedreddit.utils.AppConstants.SHOW_LESS_OFTEN_POSTS_KEY;

public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private AlertDialog.Builder clearSubredditsDialog;
    private AlertDialog.Builder clearBookmarksDialog;
    private AlertDialog.Builder logoutDialog;

    private Snackbar snackbar;

    public SettingsFragment() {}


    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.settings_preferences, rootKey);

        Preference clearShowLessOftenPreference = findPreference(getString(R.string.clear_show_less_often_key));
        if (clearShowLessOftenPreference != null) {
            clearShowLessOftenPreference.setOnPreferenceClickListener(preference -> {
                showClearSubredditsDialog();
                return false;
            });
        }

        Preference clearBookmarksPreference = findPreference(getString(R.string.clear_bookmarked_posts_key));
        if (clearBookmarksPreference != null) {
            clearBookmarksPreference.setOnPreferenceClickListener(preference -> {
                showClearBookmarksDialog();
                return false;
            });
        }

        Preference logoutAccountPreference = findPreference(getString(R.string.logout_account_key));
        if (logoutAccountPreference != null) {
            logoutAccountPreference.setOnPreferenceClickListener(preference -> {
                showLogoutDialog();
                return false;
            });
        }
    }

    private void showClearSubredditsDialog() {
        if (clearSubredditsDialog == null) {
            clearSubredditsDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setMessage(getString(R.string.clear_show_less_often_message))
                    .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> AppExecutors.getInstance().diskIO().execute(() -> {
                        AppDatabase.getInstance(getContext()).subredditDao().clearSubreddits();
                        showSnackbar(R.string.show_less_often_subreddits_cleared);
                    }))
                    .setNegativeButton(android.R.string.no, null)
                    .setCancelable(true);
        }

        clearSubredditsDialog.show();
    }

    private void showClearBookmarksDialog() {
        if (clearBookmarksDialog == null) {
            clearBookmarksDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setMessage(getString(R.string.clear_bookmarks_message))
                    .setPositiveButton(android.R.string.yes, (dialogInterface, i) ->
                        AppExecutors.getInstance().diskIO().execute(() -> {
                            AppDatabase.getInstance(getContext()).redditPostDao().clearBookmarkedRedditPosts();
                            showSnackbar(R.string.bookmarks_cleared); }
                        ))
                    .setNegativeButton(android.R.string.no, null)
                    .setCancelable(true);
        }

        clearBookmarksDialog.show();
    }

    private void showLogoutDialog() {
        if (logoutDialog == null) {
            logoutDialog = new AlertDialog.Builder(Objects.requireNonNull(getContext()))
                    .setMessage(getString(R.string.logout_message))
                    .setPositiveButton(android.R.string.yes, (dialogInterface, i) -> {
                        App.getAccountHelper().logout();
                        App.getTokenStore().clear();
                        App.getTokenStore().persist();
                        SharedPreferenceUtils.setIsUserLogged(false);
                        Objects.requireNonNull(getActivity()).finish();
                    })
                    .setNegativeButton(android.R.string.no, null)
                    .setCancelable(true);
        }

        logoutDialog.show();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if (SHOW_LESS_OFTEN_POSTS_KEY.equals(key)) {
            SharedPreferenceUtils.setShowLessOftenPosts(Integer.valueOf(sharedPreferences.getString(key, String.valueOf(DEFAULT_SHOW_LESS_OFTEN_POSTS))));
        }
    }

    private void showSnackbar(@StringRes int string) {
        if (snackbar == null) {
            snackbar = Snackbar.make(requireView(), string, Snackbar.LENGTH_LONG);
        }
        snackbar.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceManager().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        getPreferenceManager().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
    }
}