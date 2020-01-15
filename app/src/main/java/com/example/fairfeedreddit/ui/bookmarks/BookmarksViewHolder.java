package com.example.fairfeedreddit.ui.bookmarks;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.BookmarksAdapter;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.squareup.picasso.Picasso;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnLongClick;

import static com.example.fairfeedreddit.utils.AppConstants.N_A;

public class BookmarksViewHolder extends RecyclerView.ViewHolder {

    private BookmarksAdapter adapter;

    @BindView(R.id.reddit_post_title_tv)
    TextView titleTV;

    @BindView(R.id.reddit_post_text_tv)
    TextView descriptionTV;

    @BindView(R.id.reddit_post_thumbnail_iv)
    ImageView thumbnailIV;


    public BookmarksViewHolder(BookmarksAdapter adapter, @NonNull View itemView) {
        super(itemView);
        this.adapter = adapter;
        ButterKnife.bind(this, itemView);
    }

    public void bind(RedditPostEntity redditPost) {
        String description = redditPost.getTitle();
        descriptionTV.setText(TextUtils.isEmpty(description) ? N_A : description);
        titleTV.setText(String.format("r/%s  *  %s  *  %s", redditPost.getSubreddit(), redditPost.getAuthor(), parseCreatedAt(redditPost.getCreatedAt())));

        Picasso.get()
                .load(redditPost.getThumbnail())
                .into(thumbnailIV);
    }

    @OnClick(R.id.reddit_post_actions_ib)
    void onItemClick(View view) {
        adapter.onRedditPostActionsClick(adapter.getRedditPosts().get(getAdapterPosition()));
    }

    @OnLongClick
    void onLongClick(View view) {
        onItemClick(view);
    }

    @OnClick
    void onClick(View view) {
        adapter.onRedditPostClick(adapter.getRedditPosts().get(getAdapterPosition()));
    }

    private static String parseCreatedAt(Date createdAt) {
        Date current = new Date();
        long diff = current.getTime() - createdAt.getTime();

        long days = diff / (24 * 60 * 60 * 1000);
        if (days > 0) {
            return String.format("%sd", days);
        }

        long hours = diff / (60 * 60 * 1000);
        if (hours > 0) {
            return String.format("%sh", hours);
        }

        long minutes = diff / (60 * 1000);
        if (minutes > 0) {
            return String.format("%sm", minutes);
        }

        long seconds = diff / 1000;
        return String.format("%ss", seconds);
    }
}
