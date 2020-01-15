package com.example.fairfeedreddit.ui.bookmarks;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.database.AppDatabase;
import com.example.fairfeedreddit.database.RedditPostDao;
import com.example.fairfeedreddit.database.SubredditDao;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.model.SubredditEntity;

import net.dean.jraw.models.Subreddit;

import java.util.List;

public class BookmarksViewModel extends AndroidViewModel {

    private String query;
    private SubredditDao subredditDao;
    private RedditPostDao redditPostsDao;
    private List<RedditPostEntity> filteredBookmarks;
    private LiveData<List<RedditPostEntity>> bookmarksLiveData;

    public BookmarksViewModel(@NonNull Application application) {
        super(application);
        subredditDao = AppDatabase.getInstance(application.getApplicationContext()).subredditDao();
        redditPostsDao = AppDatabase.getInstance(application.getApplicationContext()).redditPostDao();
        bookmarksLiveData = redditPostsDao.loadBookmarkedRedditPosts();
    }


    List<RedditPostEntity> filterBookmarks(String query) {
        this.query = query;
        if (query.isEmpty()) {
            filteredBookmarks = bookmarksLiveData.getValue();
            return filteredBookmarks;
        }
        filteredBookmarks = redditPostsDao.loadBookmarksByName(query);
        return filteredBookmarks;
    }

    void showLessOftenSubreddit(String subredditName) {
        Subreddit subreddit = App.getAccountHelper().getReddit().subreddit(subredditName).about();
        subredditDao.insertSubreddit(SubredditEntity.fromSubreddit(subreddit));
    }

    void showMoreOftenSubreddit(String subredditName) {
        SubredditEntity subreddit = subredditDao.findSubredditByName(subredditName);
        if (subreddit != null) {
            subredditDao.deleteSubreddit(subreddit);
        }
    }

    LiveData<List<RedditPostEntity>> getBookmarks() {
        return bookmarksLiveData;
    }

    void removePostFromBookmarks(RedditPostEntity redditPost) {
        redditPostsDao.deleteRedditPost(redditPost);
    }

    boolean shouldShowLessOften(String name) {
        return !subredditDao.isShowLessOftenSubredditByName(name);
    }

    boolean isPostBookmarked(String id) {
        return redditPostsDao.isBookmarkedRedditPost(id);
    }

    public List<RedditPostEntity> getFilteredBookmarks() {
        return filteredBookmarks;
    }

    String getQuery() {
        return query;
    }
}