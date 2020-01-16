package com.example.fairfeedreddit.ui.subreddits;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.R;
import com.example.fairfeedreddit.database.AppDatabase;
import com.example.fairfeedreddit.database.SubredditDao;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.utils.AppExecutors;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.pagination.BarebonesPaginator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SubredditsViewModel extends AndroidViewModel {

    private String query;
    private List<SubredditEntity> filteredSubreddits;
    private Integer currentPage = 1;
    private int selectedMenuItemId = R.id.sort_all_subreddits;
    private SubredditDao subredditDao;
    private MutableLiveData<List<SubredditEntity>> subredditsLiveData = new MutableLiveData<>();
    private LiveData<List<SubredditEntity>> showLessOftenSubreddits;
    private List<Subreddit> subreddits;
    private Iterator<Listing<Subreddit>> iterator;


    public SubredditsViewModel(@NonNull Application application) {
        super(application);
        subredditDao = AppDatabase.getInstance(application.getApplicationContext()).subredditDao();
        showLessOftenSubreddits = subredditDao.loadShowLessOftenSubreddits();
    }

    MutableLiveData<List<SubredditEntity>> getSubreddits(final int searchType, int page, boolean isRefresh) {
        if (subredditsLiveData.getValue() == null || selectedMenuItemId != searchType || currentPage != page || isRefresh) {

            AccountHelper accountHelper = App.getAccountHelper();
            List<String> usernames = App.getTokenStore().getUsernames();
            if (!accountHelper.isAuthenticated() && !usernames.isEmpty()) {
                accountHelper.trySwitchToUser(usernames.get(0));
            }

            if (page == 1) {
                subreddits = new ArrayList<>();
                BarebonesPaginator.Builder<Subreddit> builder = accountHelper.getReddit().me().subreddits("subscriber");
                BarebonesPaginator<Subreddit> paginator = builder.build();
                iterator = paginator.iterator();
            }

            AppExecutors.getInstance().diskIO().execute(() -> {
                try {
                    subreddits.addAll(iterator.next());
                    List<SubredditEntity> sfwSubreddits = new ArrayList<>();
                    for (Subreddit subreddit : subreddits) {
                        if (!subreddit.isNsfw()) {
                            sfwSubreddits.add(SubredditEntity.fromSubreddit(subreddit));
                        }
                    }
                    currentPage = page;
                    selectedMenuItemId = searchType;
                    subredditsLiveData.postValue(sfwSubreddits);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    subredditsLiveData.postValue(null);
                }
            });
        }
        return subredditsLiveData;
    }

    List<SubredditEntity> filterSubreddits(String query) {
        this.query = query;
        filteredSubreddits = new ArrayList<>();
        boolean sortAllSubreddits = selectedMenuItemId == R.id.sort_all_subreddits;

        if (subredditsLiveData.getValue() == null) {
            return filteredSubreddits;
        }

        if (query.isEmpty()) {
            filteredSubreddits = sortAllSubreddits ?
                    subredditsLiveData.getValue() : showLessOftenSubreddits.getValue();
            return filteredSubreddits;
        }

        if (!sortAllSubreddits) {
            filteredSubreddits = subredditDao.loadSubredditsByName(query);
            return filteredSubreddits;
        }

        for (SubredditEntity subreddit : subredditsLiveData.getValue()) {
            boolean matchesName = subreddit.getName().toLowerCase().contains(query.toLowerCase());
            boolean matchesDescription = subreddit.getDescription().toLowerCase().contains(query.toLowerCase());
            if (matchesName || matchesDescription) {
                filteredSubreddits.add(subreddit);
            }
        }

        return filteredSubreddits;
    }

    boolean moreSubredditsExist() {
        return iterator.hasNext();
    }

    void showLessOftenSubreddit(SubredditEntity subredditEntity) {
        subredditDao.insertSubreddit(subredditEntity);
    }

    void showMoreOftenSubreddit(SubredditEntity subredditEntity) {
        subredditDao.deleteSubreddit(subredditEntity);
    }

    boolean shouldShowLessOften(String id) {
        return !subredditDao.isShowLessOftenSubreddit(id);
    }

    int getSelectedMenuItemId() {
        return selectedMenuItemId;
    }

    Integer getCurrentPage() {
        return currentPage;
    }

    MutableLiveData<List<SubredditEntity>> getSubreddits() {
        return subredditsLiveData;
    }

    LiveData<List<SubredditEntity>> getShowLessOftenSubreddits() {
        return showLessOftenSubreddits;
    }

    void setSelectedMenuItemId(int searchType) {
        this.selectedMenuItemId = searchType;
    }

    void clearQuery() {
        this.query = "";
    }

    CharSequence getQuery() {
        return query;
    }
}