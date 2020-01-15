package com.example.fairfeedreddit.ui.bookmarks;

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
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.BookmarksAdapter;
import com.example.fairfeedreddit.adapter.OnRedditPostClickListener;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.ui.reddit_posts.RedditPostsBottomSheetDialog;
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

import static com.example.fairfeedreddit.utils.AppConstants.ADD_TO_BOOKMARKS_MESSAGE;
import static com.example.fairfeedreddit.utils.AppConstants.LEAVE_SUBREDDIT_MESSAGE;
import static com.example.fairfeedreddit.utils.AppConstants.REMOVE_FROM_BOOKMARKS_MESSAGE;

public class BookmarksFragment extends Fragment implements SearchView.OnQueryTextListener,
        OnRedditPostClickListener, RedditPostsBottomSheetDialog.OnRedditPostActionItemClickListener {


    @BindView(R.id.bookmarks_layout)
    View redditPostsLayout;

    @BindView(R.id.bookmarks_sv)
    SearchView searchView;

    @BindView(R.id.bookmarks_rv)
    RecyclerView recyclerView;

    @BindView(R.id.error_msg_tv)
    TextView errorTV;

    @BindView(R.id.bookmarks_pb)
    ProgressBar progressBar;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    private Snackbar snackbar;

    private Observer<List<RedditPostEntity>> bookmarksObserver;

    private Unbinder unbinder;
    private BookmarksAdapter adapter;
    private BookmarksViewModel viewModel;
    private RedditPostsBottomSheetDialog bottomSheetDialog;


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_bookmarks, container, false);
        unbinder = ButterKnife.bind(this, root);
        viewModel = ViewModelProviders.of(requireActivity()).get(BookmarksViewModel.class);
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

        adapter = new BookmarksAdapter(this);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), getSpanCount());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);

        if (TextUtils.isEmpty(viewModel.getQuery())) {
            queryBookmarksDatabase();
        } else {
            searchView.setQuery(viewModel.getQuery(), true);
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

    private void updateRecyclerView(List<RedditPostEntity> posts) {
        recyclerView.setVisibility(View.VISIBLE);
        adapter.setSubreddits(posts);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        viewModel.getBookmarks().removeObservers(this);
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

    private void queryBookmarksDatabase() {
        hideErrorMessage();
        showProgressBar();
        if (bookmarksObserver == null || !viewModel.getBookmarks().hasObservers()) {
            bookmarksObserver = posts -> {
                hideProgressBar();
                if (CollectionUtils.isNonEmpty(posts)) {
                    updateRecyclerView(posts);
                } else {
                    showErrorMessage();
                }
            };
            viewModel.getBookmarks().observe(this, bookmarksObserver);
        } else {
            bookmarksObserver.onChanged(viewModel.getBookmarks().getValue());
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        recyclerView.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(R.string.no_bookmarks);
    }

    private void hideErrorMessage() {
        errorTV.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        searchView.setEnabled(false);
        hideErrorMessage();
        showProgressBar();
        AppExecutors.getInstance().diskIO().execute(() -> {
            List<RedditPostEntity> filteredBookmarks = viewModel.filterBookmarks(query);
            requireActivity().runOnUiThread(() -> {
                hideProgressBar();
                updateRecyclerView(filteredBookmarks);
                searchView.setEnabled(true);
            });
        });
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
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
    public void onActionItemClick(int id, RedditPostEntity redditPost) {
        bottomSheetDialog.dismiss();
        switch (id) {
            case R.id.open_reddit_post_action:
                onRedditPostClick(redditPost);
                break;
            case R.id.add_post_to_bookmarks_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    viewModel.removePostFromBookmarks(redditPost);
                    redditPost.setBookmarked(false);
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
}