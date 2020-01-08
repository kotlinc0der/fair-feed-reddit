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

    @Query("SELECT * FROM subreddit ORDER BY showLessOftenDate DESC")
    LiveData<List<SubredditEntity>> loadShowLessOftenSubreddits();

    @Insert
    void insertSubreddit(SubredditEntity subredditEntity);

    @Delete
    void deleteSubreddit(SubredditEntity subredditEntity);

    @Query("DELETE from subreddit")
    void clearSubreddits();

    @Query("select case when exists(select * from subreddit where id = :id) then 1 else 0 end")
    boolean isShowLessOftenSubreddit(String id);
}
