package com.example.fairfeedreddit.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.ui.subreddits.SubredditsViewHolder;

import java.util.ArrayList;
import java.util.List;

public class SubredditsAdapter extends RecyclerView.Adapter<SubredditsViewHolder> {

    private List<SubredditEntity> subreddits = new ArrayList<>();

    private final OnSubredditClickListener onSubredditClickListener;

    public SubredditsAdapter(OnSubredditClickListener onSubredditClickListener) {
        this.onSubredditClickListener = onSubredditClickListener;
    }

    @NonNull
    @Override
    public SubredditsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View view = inflater.inflate(R.layout.subreddits_list_item, viewGroup, false);
        return new SubredditsViewHolder(this, view);
    }

    @Override
    public void onBindViewHolder(@NonNull SubredditsViewHolder viewHolder, int i) {
        SubredditEntity subreddit = subreddits.get(i);
        viewHolder.bind(subreddit);
    }

    @Override
    public int getItemCount() {
        return subreddits.size();
    }

    public void setSubreddits(List<SubredditEntity> subreddits) {
        this.subreddits = subreddits;
        notifyDataSetChanged();
    }

    public List<SubredditEntity> getSubreddits() {
        return subreddits;
    }

    public void onSubredditClick(SubredditEntity subreddit) {
        onSubredditClickListener.onSubredditClick(subreddit);
    }

    public void onSubredditActionsClick(SubredditEntity subreddit) {
        onSubredditClickListener.onSubredditActionsClick(subreddit);
    }
}
