package com.example.fairfeedreddit.adapter;

import com.example.fairfeedreddit.model.RedditPostEntity;

public interface OnRedditPostClickListener {
    void onRedditPostClick(RedditPostEntity redditPost);
    void onRedditPostActionsClick(RedditPostEntity redditPost);
}
