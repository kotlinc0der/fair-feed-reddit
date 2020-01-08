package com.example.fairfeedreddit.adapter;

import com.example.fairfeedreddit.model.SubredditEntity;

public interface OnSubredditClickListener {
    void onSubredditClick(SubredditEntity subreddit);
    void onSubredditActionsClick(SubredditEntity subreddit);
}
