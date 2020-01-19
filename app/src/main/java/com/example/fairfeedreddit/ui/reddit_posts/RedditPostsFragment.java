package com.example.fairfeedreddit.ui.reddit_posts;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout.OnRefreshListener;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.OnRedditPostClickListener;
import com.example.fairfeedreddit.adapter.OnSubredditSearchResultClickListener;
import com.example.fairfeedreddit.adapter.RedditPostsAdapter;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.ui.PagingScrollListener;
import com.example.fairfeedreddit.ui.reddit_posts.RedditPostsBottomSheetDialog.OnRedditPostActionItemClickListener;
import com.example.fairfeedreddit.utils.AppConstants;
import com.example.fairfeedreddit.utils.AppExecutors;
import com.example.fairfeedreddit.utils.CollectionUtils;
import com.example.fairfeedreddit.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.references.SubredditReference;

import java.util.List;
import java.util.Objects;

import butterknife.BindBool;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static androidx.appcompat.widget.SearchView.OnQueryTextListener;
import static com.example.fairfeedreddit.utils.AppConstants.ADD_TO_BOOKMARKS_MESSAGE;
import static com.example.fairfeedreddit.utils.AppConstants.JOIN_SUBREDDIT_MESSAGE;
import static com.example.fairfeedreddit.utils.AppConstants.LEAVE_SUBREDDIT_MESSAGE;
import static com.example.fairfeedreddit.utils.AppConstants.REMOVE_FROM_BOOKMARKS_MESSAGE;

public class RedditPostsFragment extends Fragment implements OnQueryTextListener,
        OnRedditPostClickListener, OnSubredditSearchResultClickListener,
        OnRefreshListener, OnRedditPostActionItemClickListener {

    private RedditPostsViewModel viewModel;

    @BindView(R.id.reddit_posts_layout)
    View redditPostsLayout;

    @BindView(R.id.reddit_posts_sv)
    SearchView searchView;

    @BindView(R.id.reddit_posts_rv)
    RecyclerView recyclerView;

    @BindView(R.id.error_msg_tv)
    TextView errorTV;

    @BindView(R.id.reddit_posts_pb)
    ProgressBar progressBar;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    private Snackbar snackbar;

    private boolean isLoading = false;
    private RedditPostsAdapter adapter;
    private Unbinder unbinder;
    private RedditPostsBottomSheetDialog bottomSheetDialog;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reddit_posts, container, false);
        unbinder = ButterKnife.bind(this, root);
        viewModel = ViewModelProviders.of(requireActivity()).get(RedditPostsViewModel.class);
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (!NetworkUtils.isOnline(requireContext())) {
            showSnackbar(getString(R.string.check_internet));
        }

        Toolbar toolbar = requireActivity().findViewById(R.id.toolbar);
        toolbar.setOnClickListener((v -> {
            clearSearchViewFocus();
            recyclerView.smoothScrollToPosition(0);
        }));

        searchView.setOnQueryTextListener(this);
        searchView.setOnClickListener(v -> {
            onQueryTextSubmit(searchView.getQuery().toString());
            hideKeyboardAndClearFocus(view);
        });
        searchView.setOnQueryTextFocusChangeListener((v, hasFocus) -> {
            if (recyclerView != null)
                recyclerView.setVisibility(hasFocus ? View.INVISIBLE : View.VISIBLE);
        });

        swipeRefreshLayout.setOnRefreshListener(this);

        adapter = new RedditPostsAdapter(this, this);
        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), getSpanCount());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PagingScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                boolean canLoadRedditPosts = viewModel.moreRedditPostsExist() && !adapter.isSearching();
                boolean canLoadSubreddits = viewModel.moreSubredditsExist() && adapter.isSearching();
                if (!isLoading && (canLoadRedditPosts || canLoadSubreddits)) {
                    if (!NetworkUtils.isOnline(requireContext())) {
                        showSnackbar(getString(R.string.check_internet));
                        return;
                    }
                    if (adapter.isSearching()) {
                        searchForSubreddits(viewModel.getSearchQuery(), viewModel.getSearchPage() + 1);
                    } else {
                        loadRedditPosts(viewModel.getCurrentPage() + 1, false);
                    }
                }
            }

            @Override
            protected boolean isLoading() {
                return isLoading;
            }
        });

        if (!TextUtils.isEmpty(viewModel.getSearchQuery())) {
            searchView.setQuery(viewModel.getSearchQuery(), true);
        } else {
            if (NetworkUtils.isOnline(requireContext()) || viewModel.getRedditPosts().getValue() != null) {
                loadRedditPosts(viewModel.getCurrentPage(), false);
            } else if (CollectionUtils.isEmpty(viewModel.getRedditPosts().getValue())) {
                showErrorMessage();
            }
        }

        clearSearchViewFocus();
    }

    private void clearSearchViewFocus() {
        redditPostsLayout.requestFocus();
        searchView.clearFocus();
        hideKeyboardAndClearFocus(searchView);
    }

    private int getSpanCount() {
        int spanCount = 1;
        if (isLandscape && isTablet) {
            spanCount = 3;
        }
        if ((isTablet && !isLandscape)) {
            spanCount = 2;
        }
        return spanCount;
    }

    private void loadRedditPosts(int page, boolean isRefresh) {
        hideErrorMessage();
        if (!isRefresh) {
            showProgressBar();
        }
        if (!viewModel.getRedditPosts().hasObservers()) {
            viewModel.getRedditPosts(page, isRefresh).observe(this, redditPosts -> {
                hideProgressBar();
                if (CollectionUtils.isNonEmpty(redditPosts)) {
                    updateRecyclerView(redditPosts);
                } else if (viewModel.getCurrentPage() == 1 && CollectionUtils.isEmpty(viewModel.getRedditPosts().getValue())) {
                    showErrorMessage();
                } else if (!NetworkUtils.isOnline(requireContext())) {
                    showSnackbar(getString(R.string.check_internet));
                }
                swipeRefreshLayout.setRefreshing(false);
            });
        } else {
            viewModel.getRedditPosts(page, isRefresh);
        }
    }

    private void updateRecyclerView(List<RedditPostEntity> posts) {
        recyclerView.setVisibility(View.VISIBLE);
        adapter.setRedditPosts(posts);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        viewModel.getRedditPosts().removeObservers(this);
        if (bottomSheetDialog != null)
            bottomSheetDialog.clearListener();
    }

    private void hideKeyboardAndClearFocus(View view) {
        InputMethodManager inputMethodManager = (InputMethodManager) Objects.requireNonNull(getContext()).getSystemService(Activity.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(inputMethodManager).hideSoftInputFromWindow(view.getWindowToken(), 0);
        searchView.clearFocus();
    }

    private void showSnackbar(String message) {
        if (snackbar == null) {
            snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG);
        }
        requireActivity().runOnUiThread(() -> {
            snackbar.dismiss();
            snackbar.setText(message);
            snackbar.show();
        });
    }

    private void hideProgressBar() {
        isLoading = false;
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(R.string.error_message);
    }

    private void hideErrorMessage() {
        errorTV.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchForSubreddits(query, 1);
        return false;
    }

    private void searchForSubreddits(String query, int page) {
        searchView.setEnabled(false);
        hideErrorMessage();
        showProgressBar();
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (query.isEmpty()) {
                requireActivity().runOnUiThread(() -> {
                    hideProgressBar();
                    List<RedditPostEntity> redditPosts = viewModel.getRedditPosts().getValue();
                    if (CollectionUtils.isNonEmpty(redditPosts)) {
                        updateRecyclerView(redditPosts);
                    } else if (viewModel.getCurrentPage() == 1 && CollectionUtils.isEmpty(redditPosts)) {
                        showErrorMessage();
                    } else if (!NetworkUtils.isOnline(requireContext())) {
                        showSnackbar(getString(R.string.check_internet));
                    }
                    searchView.setEnabled(true);
                    swipeRefreshLayout.setRefreshing(false);
                });
            } else {
                List<SubredditEntity> filteredSubreddits = viewModel.searchForSubreddits(query, page);
                requireActivity().runOnUiThread(() -> {
                    hideProgressBar();
                    recyclerView.setVisibility(View.VISIBLE);
                    adapter.setSubreddits(filteredSubreddits);
                    searchView.setEnabled(true);
                    clearSearchViewFocus();
                });
            }

        });
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (TextUtils.isEmpty(newText)) {
            onQueryTextSubmit(newText);
        }
        return false;
    }

    @Override
    public void onRedditPostClick(RedditPostEntity redditPost) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(redditPost.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onRedditPostActionsClick(RedditPostEntity redditPost) {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = RedditPostsBottomSheetDialog.newInstance(this);
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            Bundle args = new Bundle();
            args.putBoolean(AppConstants.SHOW_LESS_OFTEN_POSTS_KEY, shouldShowLessOften(redditPost.getSubreddit()));
            args.putBoolean(AppConstants.IS_POST_BOOKMARKED_KEY, viewModel.isPostBookmarked(redditPost.getId()));
            args.putSerializable(AppConstants.REDDIT_POST_KEY, redditPost);
            bottomSheetDialog.setArguments(args);
            bottomSheetDialog.show(requireFragmentManager(), RedditPostsBottomSheetDialog.class.getName());
        });
    }

    private boolean shouldShowLessOften(String name) {
        return viewModel.shouldShowLessOften(name);
    }

    @Override
    public void onRefresh() {
        if (adapter.isSearching()) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        if (NetworkUtils.isOnline(requireContext()) || viewModel.getRedditPosts().getValue() != null) {
            loadRedditPosts(1, true);
        } else if (CollectionUtils.isEmpty(viewModel.getRedditPosts().getValue())) {
            showErrorMessage();
        }
    }

    @Override
    public void onActionItemClick(int id, RedditPostEntity redditPost) {
        bottomSheetDialog.dismiss();
        switch (id) {
            case R.id.open_reddit_post_action:
                onRedditPostClick(redditPost);
                break;
            case R.id.add_post_to_bookmarks_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    if (redditPost.isBookmarked()) {
                        viewModel.removePostFromBookmarks(redditPost);
                        redditPost.setBookmarked(false);
                    } else {
                        viewModel.addRedditPostToBookmarks(redditPost);
                        redditPost.setBookmarked(true);
                    }
                    showSnackbar(redditPost.isBookmarked() ? ADD_TO_BOOKMARKS_MESSAGE :
                            REMOVE_FROM_BOOKMARKS_MESSAGE);
                });
                break;
            case R.id.go_to_subreddit_action:
                String url = AppConstants.BASE_REDDIT_URL + "/r/" + redditPost.getSubreddit();
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(intent);
                break;
            case R.id.update_show_less_often_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    String subreddit = redditPost.getSubreddit();
                    if (redditPost.shouldShowLessOften()) {
                        viewModel.showLessOftenSubreddit(subreddit);
                    } else {
                        viewModel.showMoreOftenSubreddit(subreddit);
                    }
                });
                break;
            case R.id.leave_subreddit_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    SubredditReference subredditReference = App.getAccountHelper().getReddit().subreddit(redditPost.getSubreddit());
                    subredditReference.unsubscribe();
                    viewModel.showMoreOftenSubreddit(redditPost.getSubreddit());
                    showSnackbar(String.format(LEAVE_SUBREDDIT_MESSAGE, redditPost.getSubreddit()));
                });
                break;
        }
    }

    @Override
    public void updateSubreddit(SubredditEntity subredditEntity) {
        AppExecutors.getInstance().diskIO().execute(() -> {
            if (!NetworkUtils.isOnline(requireContext())) {
                requireActivity().runOnUiThread(() -> showSnackbar(getString(R.string.check_internet)));
                return;
            }

            SubredditReference subredditReference = App.getAccountHelper().getReddit().subreddit(subredditEntity.getName());
            if (subredditEntity.isUserSubscriber()) {
                subredditReference.subscribe();
            } else {
                subredditReference.unsubscribe();
                viewModel.showMoreOftenSubreddit(subredditEntity.getName());
            }
            requireActivity().runOnUiThread(() -> showSnackbar(String.format(subredditEntity.isUserSubscriber() ? JOIN_SUBREDDIT_MESSAGE
                    : LEAVE_SUBREDDIT_MESSAGE, subredditEntity.getName())));
        });
    }
}