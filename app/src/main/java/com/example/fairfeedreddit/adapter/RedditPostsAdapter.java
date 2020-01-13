package com.example.fairfeedreddit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.ui.reddit_posts.RedditPostsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RedditPostsAdapter extends RecyclerView.Adapter<RedditPostsViewHolder> {

    private List<RedditPostEntity> redditPosts = new ArrayList<>();

    private final OnRedditPostClickListener onRedditPostClickListener;

    public RedditPostsAdapter(OnRedditPostClickListener onRedditPostClickListener) {
        this.onRedditPostClickListener = onRedditPostClickListener;
    }

    @NonNull
    @Override
    public RedditPostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.reddit_post_list_item, viewGroup, false);
        return new RedditPostsViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(@NonNull RedditPostsViewHolder viewHolder, int i) {
        RedditPostEntity redditPost = redditPosts.get(i);
        viewHolder.bind(redditPost);
    }

    @Override
    public int getItemCount() {
        return redditPosts.size();
    }

    public void setSubreddits(List<RedditPostEntity> redditPosts) {
        this.redditPosts = redditPosts;
        notifyDataSetChanged();
    }

    public List<RedditPostEntity> getRedditPosts() {
        return redditPosts;
    }

    public void onRedditPostClick(RedditPostEntity redditPost) {
        onRedditPostClickListener.onRedditPostClick(redditPost);
    }

    public void onRedditPostActionsClick(RedditPostEntity redditPost) {
        onRedditPostClickListener.onRedditPostActionsClick(redditPost);
    }
}
