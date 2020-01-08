package com.example.fairfeedreddit.ui;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// This ScrollListener implementation was inspired by PaginationScrollListener.java from http://velmm.com/pagination-with-recyclerview-in-android/
public abstract class PagingScrollListener extends RecyclerView.OnScrollListener {
    private final LinearLayoutManager layoutManager;

    protected PagingScrollListener(LinearLayoutManager layoutManager) {
        this.layoutManager = layoutManager;
    }

    @Override
    public void onScrolled(@NonNull RecyclerView recyclerView, int x, int y) {
        super.onScrolled(recyclerView, x, y);

        int itemCount = layoutManager.getItemCount();
        int lastVisible = layoutManager.findLastVisibleItemPosition();
        boolean endHasBeenReached = lastVisible + 1 >= itemCount;
        if (!isLoading() && (itemCount > 0 && endHasBeenReached)) {
            loadMoreItems();
        }
    }

    protected abstract void loadMoreItems();

    protected abstract boolean isLoading();
}

