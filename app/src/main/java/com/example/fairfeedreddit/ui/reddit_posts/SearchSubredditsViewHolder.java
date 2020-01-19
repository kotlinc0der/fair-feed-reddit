package com.example.fairfeedreddit.ui.reddit_posts;

import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.RedditPostsAdapter;
import com.example.fairfeedreddit.model.SubredditEntity;

import butterknife.BindColor;
import butterknife.BindString;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SearchSubredditsViewHolder extends RecyclerView.ViewHolder {
    private RedditPostsAdapter adapter;

    @BindView(R.id.subreddit_title_tv)
    TextView subredditTV;

    @BindView(R.id.join_leave_subreddit_tv)
    TextView updateSubredditTV;

    @BindString(R.string.join_subreddit)
    String joinSubreddit;

    @BindString(R.string.leave_subreddit)
    String leaveSubreddit;

    @BindColor(R.color.colorPrimary)
    int primaryColor;

    @BindColor(android.R.color.white)
    int whiteColor;

    public SearchSubredditsViewHolder(RedditPostsAdapter adapter, @NonNull View itemView) {
        super(itemView);
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    public void bind(SubredditEntity subredditEntity) {
        subredditTV.setText(String.format("r/%s", subredditEntity.getName()));
        customizeUpdateSubredditTV(subredditEntity.isUserSubscriber());
    }

    @OnClick(R.id.join_leave_subreddit_tv)
    void onItemClick(View view) {
        SubredditEntity subredditEntity = adapter.getSubredditSearchResults().get(getAdapterPosition());
        subredditEntity.setIsUserSubscriber(!subredditEntity.isUserSubscriber());
        adapter.updateSubreddit(subredditEntity);
        customizeUpdateSubredditTV(subredditEntity.isUserSubscriber());
    }

    private void customizeUpdateSubredditTV(boolean userSubscriber) {
        updateSubredditTV.setText(userSubscriber ? leaveSubreddit : joinSubreddit);
        updateSubredditTV.setTextColor(userSubscriber ? primaryColor : whiteColor);
        updateSubredditTV.setBackgroundResource(userSubscriber ?
                R.drawable.join_subreddit_background :
                R.drawable.leave_subreddit_background);
    }
}
