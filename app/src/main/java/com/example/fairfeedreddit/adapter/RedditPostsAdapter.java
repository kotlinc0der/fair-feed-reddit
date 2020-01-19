package com.example.fairfeedreddit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.ui.reddit_posts.RedditPostsViewHolder;
import com.example.fairfeedreddit.ui.reddit_posts.SearchSubredditsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class RedditPostsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private boolean isSearching;

    private List<RedditPostEntity> redditPosts = new ArrayList<>();
    private List<SubredditEntity> subreddits = new ArrayList<>();
    private final OnRedditPostClickListener onRedditPostClickListener;
    private OnSubredditSearchResultClickListener onSubredditSearchListener;

    public RedditPostsAdapter(OnRedditPostClickListener onRedditPostClickListener, OnSubredditSearchResultClickListener onSubredditSearchListener) {
        this.onRedditPostClickListener = onRedditPostClickListener;
        this.onSubredditSearchListener = onSubredditSearchListener;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(isSearching ? R.layout.search_subreddits_list_item
                : R.layout.reddit_post_list_item, viewGroup, false);
        return isSearching ? new SearchSubredditsViewHolder(this, view)
                : new RedditPostsViewHolder(this, view);
    }

    @Override
    public int getItemViewType(int position) {
        return isSearching ? 1 : 0;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (isSearching) {
            SubredditEntity subredditEntity = subreddits.get(i);
            ((SearchSubredditsViewHolder) viewHolder).bind(subredditEntity);
        } else {
            RedditPostEntity redditPost = redditPosts.get(i);
            ((RedditPostsViewHolder) viewHolder).bind(redditPost);
        }
    }

    @Override
    public int getItemCount() {
        return isSearching ? subreddits.size() : redditPosts.size();
    }

    public void setRedditPosts(List<RedditPostEntity> redditPosts) {
        this.redditPosts = redditPosts;
        this.isSearching = false;
        notifyDataSetChanged();
    }

    public void setSubreddits(List<SubredditEntity> subreddits) {
        this.subreddits = subreddits;
        this.isSearching = true;
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

    public List<SubredditEntity> getSubredditSearchResults() {
        return subreddits;
    }

    public boolean isSearching() {
        return isSearching;
    }

    public void updateSubreddit(SubredditEntity subredditEntity) {
        this.onSubredditSearchListener.updateSubreddit(subredditEntity);
    }
}
