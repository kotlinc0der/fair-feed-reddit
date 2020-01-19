package com.example.fairfeedreddit.ui.bookmarks;

import android.os.AsyncTask;

import com.example.fairfeedreddit.model.RedditPostEntity;

import java.lang.ref.WeakReference;
import java.util.List;

class SearchBookmarksTask extends AsyncTask<String, Void, List<RedditPostEntity>> {

    private final WeakReference<BookmarksFragment> weakReference;


    SearchBookmarksTask(BookmarksFragment fragment) {
        weakReference = new WeakReference<>(fragment);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        BookmarksFragment bookmarksFragment = weakReference.get();
        bookmarksFragment.searchView.setEnabled(false);
        bookmarksFragment.hideErrorMessage();
        bookmarksFragment.showProgressBar();
    }

    @Override
    protected List<RedditPostEntity> doInBackground(String... args) {
        String query = args[0];
        return weakReference.get().filterBookmarks(query);
    }

    @Override
    protected void onPostExecute(List<RedditPostEntity> filteredBookmarks) {
        super.onPostExecute(filteredBookmarks);
        BookmarksFragment bookmarksFragment = weakReference.get();
        bookmarksFragment.hideProgressBar();
        bookmarksFragment.updateRecyclerView(filteredBookmarks);
        bookmarksFragment.searchView.setEnabled(true);
    }
}
