package com.example.fairfeedreddit.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.fairfeedreddit.model.SubredditEntity;

import java.util.List;

@Dao
public interface SubredditDao {

    @Query("SELECT name FROM subreddit ORDER BY showLessOftenDate DESC")
    List<String> loadShowLessOftenSubredditNames();

    @Query("SELECT * FROM subreddit ORDER BY showLessOftenDate DESC")
    LiveData<List<SubredditEntity>> loadShowLessOftenSubreddits();

    @Insert
    void insertSubreddit(SubredditEntity subredditEntity);

    @Delete
    void deleteSubreddit(SubredditEntity subredditEntity);

    @Query("DELETE from subreddit where name = :name")
    void deleteSubredditByName(String name);

    @Query("DELETE from subreddit")
    void clearSubreddits();

    @Query("select case when exists(select * from subreddit where id = :id) then 1 else 0 end")
    boolean isShowLessOftenSubreddit(String id);

    @Query("select case when exists(select * from subreddit where name = :name) then 1 else 0 end")
    boolean isShowLessOftenSubredditByName(String name);

    @Query("SELECT * FROM subreddit where name = :subreddit")
    SubredditEntity findSubredditByName(String subreddit);
}
