package com.example.fairfeedreddit.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.utils.AppConstants;


public class BookmarksAppWidget extends AppWidgetProvider {

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.bookmarks_app_widget);
        views.setViewVisibility(R.id.widget_error_text, View.INVISIBLE);
        Intent intent = new Intent(context, WidgetRemoteViewsService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        views.setRemoteAdapter(R.id.widget_listView, intent);

        Intent bookmarksIntent = new Intent(context, BookmarksAppWidget.class);
        bookmarksIntent.setAction(AppConstants.OPEN_BOOKMARK_ACTION);
        bookmarksIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, bookmarksIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setPendingIntentTemplate(R.id.widget_listView, toastPendingIntent);

        // Instruct the widget manager to update the widget
        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId, Bundle newOptions) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
        boolean bookmarksExist = newOptions.getBoolean(AppConstants.BOOKMARKS_EXIST_KEY);
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.bookmarks_app_widget);
        rv.setViewVisibility(R.id.widget_error_text, bookmarksExist ? View.INVISIBLE : View.VISIBLE);
        appWidgetManager.partiallyUpdateAppWidget(appWidgetId, rv);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (AppConstants.OPEN_BOOKMARK_ACTION.equals(intent.getAction())) {
            String bookmarkURL = intent.getStringExtra(AppConstants.BOOKMARK_URL_KEY);
            Intent openBookmark = new Intent(Intent.ACTION_VIEW, Uri.parse(bookmarkURL));
            openBookmark.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(openBookmark);
        }
        super.onReceive(context, intent);
    }
}

