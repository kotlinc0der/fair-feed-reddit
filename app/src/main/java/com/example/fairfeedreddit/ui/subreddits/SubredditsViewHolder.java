package com.example.fairfeedreddit.ui.subreddits;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.SubredditsAdapter;
import com.example.fairfeedreddit.model.SubredditEntity;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.example.fairfeedreddit.utils.AppConstants.N_A;

public class SubredditsViewHolder extends RecyclerView.ViewHolder {
    private SubredditsAdapter adapter;

    @BindView(R.id.subreddit_actions_ib)
    ImageButton subredditActionsIB;

    @BindView(R.id.subreddit_title_tv)
    TextView titleTV;

    @BindView(R.id.subreddit_description_tv)
    TextView descriptionTV;

    public SubredditsViewHolder(SubredditsAdapter adapter, @NonNull View itemView) {
        super(itemView);
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    public void bind(SubredditEntity subreddit) {
        titleTV.setText(String.format("r/%s", subreddit.getName()));
        String description = subreddit.getDescription();
        descriptionTV.setText(TextUtils.isEmpty(description) ? N_A : description);
    }

    @OnClick(R.id.subreddit_actions_ib)
    void onItemClick(View view) {
        adapter.onSubredditActionsClick(adapter.getSubreddits().get(getAdapterPosition()));
    }

    @OnLongClick
    void onLongClick(View view) {
        onItemClick(view);
    }

    @OnClick
    void onClick(View view) {
        adapter.onSubredditClick(adapter.getSubreddits().get(getAdapterPosition()));
    }
}
