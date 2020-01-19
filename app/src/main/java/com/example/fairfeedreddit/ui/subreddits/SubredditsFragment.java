package com.example.fairfeedreddit.ui.subreddits;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.adapter.OnSubredditClickListener;
import com.example.fairfeedreddit.adapter.SubredditsAdapter;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.ui.PagingScrollListener;
import com.example.fairfeedreddit.ui.subreddits.SubredditsBottomSheetDialog.OnActionItemClickListener;
import com.example.fairfeedreddit.utils.AppConstants;
import com.example.fairfeedreddit.utils.AppExecutors;
import com.example.fairfeedreddit.utils.CollectionUtils;
import com.example.fairfeedreddit.utils.NetworkUtils;
import com.google.android.material.snackbar.Snackbar;

import net.dean.jraw.references.SubredditReference;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import butterknife.BindBool;
import butterknife.BindDrawable;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.example.fairfeedreddit.utils.AppConstants.LEAVE_SUBREDDIT_MESSAGE;

public class SubredditsFragment extends Fragment implements SearchView.OnQueryTextListener,
        OnSubredditClickListener, SwipeRefreshLayout.OnRefreshListener, OnActionItemClickListener {

    private SubredditsViewModel viewModel;

    @BindView(R.id.subreddits_layout)
    View subredditsLayout;

    @BindView(R.id.subreddits_sv)
    SearchView searchView;

    @BindView(R.id.subreddits_rv)
    RecyclerView recyclerView;

    @BindView(R.id.error_msg_tv)
    TextView errorTV;

    @BindView(R.id.subreddits_pb)
    ProgressBar progressBar;

    @BindView(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindDrawable(R.drawable.divider)
    Drawable divider;

    @BindDrawable(R.drawable.ic_sort_33dp)
    Drawable overflowIcon;

    @BindBool(R.bool.isTablet)
    boolean isTablet;

    @BindBool(R.bool.isLandscape)
    boolean isLandscape;

    private Snackbar snackbar;

    private boolean isLoading = false;
    private SubredditsAdapter adapter;
    private Unbinder unbinder;
    private SubredditsBottomSheetDialog bottomSheetDialog;
    private Observer<List<SubredditEntity>> showLessOftenSubsObserver;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_subreddits, container, false);
        unbinder = ButterKnife.bind(this, root);

        setHasOptionsMenu(true);

        viewModel = ViewModelProviders.of(requireActivity()).get(SubredditsViewModel.class);

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
        toolbar.setOverflowIcon(overflowIcon);

        searchView.setOnClickListener(v -> {
            onQueryTextSubmit(searchView.getQuery().toString());
            hideKeyboardAndClearFocus(view);
        });
        searchView.setOnQueryTextListener(this);

        swipeRefreshLayout.setOnRefreshListener(this);

        adapter = new SubredditsAdapter(this);

        GridLayoutManager layoutManager = new GridLayoutManager(requireContext(), getSpanCount());
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.setAdapter(adapter);
        recyclerView.addOnScrollListener(new PagingScrollListener(layoutManager) {
            @Override
            protected void loadMoreItems() {
                if (!isLoading && !isShowLessOftenSubredditsView() && viewModel.moreSubredditsExist() && TextUtils.isEmpty(searchView.getQuery())) {
                    if (!NetworkUtils.isOnline(requireContext())) {
                        showSnackbar(getString(R.string.check_internet));
                        return;
                    }
                    loadSubreddits(viewModel.getSelectedMenuItemId(), viewModel.getCurrentPage() + 1, false);
                }
            }

            @Override
            protected boolean isLoading() {
                return isLoading;
            }
        });

        if (!TextUtils.isEmpty(viewModel.getQuery())) {
            searchView.setQuery(viewModel.getQuery(), true);
        } else {
            if (isShowLessOftenSubredditsView()) {
                querySubredditsDatabase();
            } else {
                if (NetworkUtils.isOnline(requireContext()) || viewModel.getSubreddits().getValue() != null) {
                    loadSubreddits(viewModel.getSelectedMenuItemId(), viewModel.getCurrentPage(), false);
                } else if (CollectionUtils.isEmpty(viewModel.getSubreddits().getValue())){
                    showErrorMessage(R.string.error_message);
                }
            }
        }

        clearSearchViewFocus();
    }

    private void clearSearchViewFocus() {
        subredditsLayout.requestFocus();
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

    private void loadSubreddits(int searchType, int page, boolean isRefresh) {
        hideErrorMessage();
        if (!isRefresh) {
            showProgressBar();
        }
        if (!viewModel.getSubreddits().hasObservers()) {
            viewModel.getSubreddits(searchType, page, isRefresh).observe(this, subreddits -> {
                hideProgressBar();
                if (CollectionUtils.isNonEmpty(subreddits)) {
                    updateRecyclerView(subreddits);
                } else if (viewModel.getCurrentPage() == 1) {
                    showErrorMessage(R.string.error_message);
                } else if (!NetworkUtils.isOnline(requireContext())) {
                    showSnackbar(getString(R.string.check_internet));
                }
                swipeRefreshLayout.setRefreshing(false);
            });
        } else {
            viewModel.getSubreddits(searchType, page, isRefresh);
        }
    }

    private void querySubredditsDatabase() {
        hideErrorMessage();
        showProgressBar();
        if (showLessOftenSubsObserver == null || !viewModel.getShowLessOftenSubreddits().hasObservers()) {
            showLessOftenSubsObserver = subs -> {
                if (isShowLessOftenSubredditsView()) {
                    hideProgressBar();
                    if (CollectionUtils.isNonEmpty(subs)) {
                        updateRecyclerView(subs);
                    } else {
                        showErrorMessage(R.string.no_show_less_often_subreddits);
                    }
                }
            };
            viewModel.getShowLessOftenSubreddits().observe(this, showLessOftenSubsObserver);
        } else {
            showLessOftenSubsObserver.onChanged(viewModel.getShowLessOftenSubreddits().getValue());
        }
    }

    private void updateRecyclerView(List<SubredditEntity> subs) {
        recyclerView.setVisibility(View.VISIBLE);
        if (viewModel.getCurrentPage() == 1 || isShowLessOftenSubredditsView()) {
            recyclerView.scrollToPosition(0);
        }
        adapter.setSubreddits(subs);
    }

    @Override
    public void onCreateOptionsMenu(@NotNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.subreddits_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
        menu.findItem(viewModel.getSelectedMenuItemId()).setChecked(true);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();

        clearSearchView();

        if (itemId == R.id.sort_all_subreddits) {
            item.setChecked(true);
            loadSubreddits(itemId, 1, false);
            return true;
        }

        if (itemId == R.id.sort_show_less_often_subreddits) {
            item.setChecked(true);
            viewModel.setSelectedMenuItemId(R.id.sort_show_less_often_subreddits);
            querySubredditsDatabase();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void clearSearchView() {
        viewModel.clearQuery();
        searchView.setQuery("", false);
        clearSearchViewFocus();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
        viewModel.getSubreddits().removeObservers(this);
        viewModel.getShowLessOftenSubreddits().removeObservers(this);
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
        snackbar.dismiss();
        snackbar.setText(message);
        snackbar.show();
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.INVISIBLE);
        isLoading = false;
    }

    private void showProgressBar() {
        isLoading = true;
        progressBar.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage(int errorMessage) {
        recyclerView.setVisibility(View.INVISIBLE);
        searchView.setVisibility(View.INVISIBLE);
        errorTV.setVisibility(View.VISIBLE);
        errorTV.setText(errorMessage);
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
            List<SubredditEntity> filteredSubreddits = viewModel.filterSubreddits(query);
            requireActivity().runOnUiThread(() -> {
                hideProgressBar();
                updateRecyclerView(filteredSubreddits);
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
    public void onSubredditClick(SubredditEntity subreddit) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.BASE_REDDIT_URL + subreddit.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onSubredditActionsClick(SubredditEntity subreddit) {
        if (bottomSheetDialog == null) {
            bottomSheetDialog = SubredditsBottomSheetDialog.newInstance(this);
        }

        AppExecutors.getInstance().diskIO().execute(() -> {
            Bundle args = new Bundle();
            args.putSerializable(AppConstants.SUBREDDIT_KEY, subreddit);
            args.putBoolean(AppConstants.SHOW_LESS_OFTEN_POSTS_KEY, shouldShowLessOften(subreddit.getId()));
            bottomSheetDialog.setArguments(args);
            bottomSheetDialog.show(requireFragmentManager(), SubredditsBottomSheetDialog.class.getName());
        });
    }

    private boolean shouldShowLessOften(String uniqueId) {
        return !isShowLessOftenSubredditsView() && viewModel.shouldShowLessOften(uniqueId);
    }

    @Override
    public void onRefresh() {
        if (isShowLessOftenSubredditsView()) {
            swipeRefreshLayout.setRefreshing(false);
        } else {
            if (NetworkUtils.isOnline(requireContext()) || viewModel.getSubreddits().getValue() != null) {
                loadSubreddits(viewModel.getSelectedMenuItemId(), 1, true);
            } else if (CollectionUtils.isEmpty(viewModel.getSubreddits().getValue())){
                showErrorMessage(R.string.error_message);
            }
        }
    }

    private boolean isShowLessOftenSubredditsView() {
        return viewModel.getSelectedMenuItemId() == R.id.sort_show_less_often_subreddits;
    }

    @Override
    public void onActionItemClick(int id, SubredditEntity subreddit) {
        bottomSheetDialog.dismiss();
        switch (id) {
            case R.id.go_to_subreddit_action:
                onSubredditClick(subreddit);
                break;
            case R.id.update_show_less_often_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    if (subreddit.shouldShowLessOften()) {
                        viewModel.showLessOftenSubreddit(subreddit);
                        subreddit.setShouldShowLessOften(false);
                    } else {
                        viewModel.showMoreOftenSubreddit(subreddit);
                        subreddit.setShouldShowLessOften(true);
                    }
                });
                break;
            case R.id.leave_subreddit_action:
                AppExecutors.getInstance().diskIO().execute(() -> {
                    SubredditReference subredditReference = App.getAccountHelper().getReddit().subreddit(subreddit.getName());
                    subredditReference.unsubscribe();
                    viewModel.showMoreOftenSubreddit(subreddit);
                    showSnackbar(String.format(LEAVE_SUBREDDIT_MESSAGE, subreddit.getName()));
                });
                break;
        }
    }
}