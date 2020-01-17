package com.example.fairfeedreddit.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.database.AppDatabase;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.utils.AppConstants;
import com.example.fairfeedreddit.utils.CollectionUtils;

import java.util.List;

import static com.example.fairfeedreddit.ui.bookmarks.BookmarksViewHolder.parseCreatedAt;
import static com.example.fairfeedreddit.utils.AppConstants.N_A;

public class WidgetRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {

    private final LiveData<List<RedditPostEntity>> listLiveData;
    private Context context;
    private int appWidgetId;
    private List<RedditPostEntity> bookmarks;
    private final Observer<List<RedditPostEntity>> listObserver;
    private final AppWidgetManager appWidgetManager;

    WidgetRemoteViewsFactory(Context context, Intent intent) {
        this.context = context;
        appWidgetManager = AppWidgetManager.getInstance(context);
        listLiveData = AppDatabase.getInstance(context).redditPostDao().loadBookmarkedRedditPosts();
        listObserver = redditPostEntities -> {
            Bundle appWidgetOptions = appWidgetManager.getAppWidgetOptions(appWidgetId);
            appWidgetOptions.putBoolean(AppConstants.BOOKMARKS_EXIST_KEY, CollectionUtils.isNonEmpty(redditPostEntities));
            appWidgetManager.updateAppWidgetOptions(appWidgetId, appWidgetOptions);
            bookmarks = redditPostEntities;
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_listView);
        };
        listLiveData.observeForever(listObserver);
        appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
    }

    @Override
    public void onCreate() {
    }

    @Override
    public void onDataSetChanged() {
    }

    @Override
    public void onDestroy() {
        listLiveData.removeObserver(listObserver);
    }

    @Override
    public int getCount() {
        return bookmarks != null ? bookmarks.size() : 0;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        RedditPostEntity bookmark = bookmarks.get(position);
        String text = bookmark.getTitle();
        final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_list_item);
        remoteViews.setTextViewText(R.id.reddit_post_text_tv, TextUtils.isEmpty(text) ? N_A : text);
        remoteViews.setTextViewText(R.id.reddit_post_title_tv, String.format("r/%s  *  %s  *  %s", bookmark.getSubreddit(), bookmark.getAuthor(), parseCreatedAt(bookmark.getCreatedAt())));

        Bundle extras = new Bundle();
        extras.putString(AppConstants.BOOKMARK_URL_KEY, bookmark.getUrl());
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        remoteViews.setOnClickFillInIntent(R.id.bookmarks_item_layout, fillInIntent);

        return remoteViews;
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
