package com.example.fairfeedreddit.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.fairfeedreddit.model.RedditPostEntity;

import java.util.List;

@Dao
public interface RedditPostDao {

    @Query("SELECT id FROM reddit_post ORDER BY bookmarkDate DESC")
    LiveData<List<String>> loadBookmarkIds();

    @Query("SELECT * FROM reddit_post ORDER BY bookmarkDate DESC")
    LiveData<List<RedditPostEntity>> loadBookmarkedRedditPosts();

    @Insert
    void insertRedditPost(RedditPostEntity redditPostEntity);

    @Delete
    void deleteRedditPost(RedditPostEntity redditPostEntity);

    @Query("DELETE from reddit_post")
    void clearBookmarkedRedditPosts();

    @Query("select case when exists(select * from reddit_post where id = :id) then 1 else 0 end")
    boolean isBookmarkedRedditPost(String id);
}
