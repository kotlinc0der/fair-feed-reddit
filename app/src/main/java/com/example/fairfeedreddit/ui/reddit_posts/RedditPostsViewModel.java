package com.example.fairfeedreddit.ui.reddit_posts;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.example.fairfeedreddit.App;
import com.example.fairfeedreddit.database.AppDatabase;
import com.example.fairfeedreddit.database.RedditPostDao;
import com.example.fairfeedreddit.database.SubredditDao;
import com.example.fairfeedreddit.model.RedditPostEntity;
import com.example.fairfeedreddit.model.SubredditEntity;
import com.example.fairfeedreddit.utils.AppExecutors;
import com.example.fairfeedreddit.utils.SharedPreferenceUtils;

import net.dean.jraw.models.Listing;
import net.dean.jraw.models.Submission;
import net.dean.jraw.models.Subreddit;
import net.dean.jraw.models.SubredditSearchSort;
import net.dean.jraw.models.SubredditSort;
import net.dean.jraw.models.TimePeriod;
import net.dean.jraw.oauth.AccountHelper;
import net.dean.jraw.pagination.DefaultPaginator;
import net.dean.jraw.pagination.SubredditSearchPaginator;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class RedditPostsViewModel extends AndroidViewModel {

    private String searchQuery;
    private int sloLimit;
    private Integer currentPage = 1;
    private RedditPostDao redditPostsDao;
    private SubredditDao subredditDao;
    private List<Submission> submissions;
    private Iterator<Listing<Submission>> iterator;
    private MutableLiveData<List<RedditPostEntity>> redditPostsLiveData = new MutableLiveData<>();
    private Map<String, List<Map.Entry<Integer, Integer>>> sloMap;
    private List<RedditPostEntity> redditPostEntities;

    private int searchPage = 1;
    private List<Subreddit> searchResults;
    private Iterator<Listing<Subreddit>> searchResultsIterator;

    public RedditPostsViewModel(@NonNull Application application) {
        super(application);
        subredditDao = AppDatabase.getInstance(application.getApplicationContext()).subredditDao();
        redditPostsDao = AppDatabase.getInstance(application.getApplicationContext()).redditPostDao();
        sloLimit = SharedPreferenceUtils.getShowLessOftenPosts();
    }

    List<SubredditEntity> searchForSubreddits(String query, int page) {
        List<SubredditEntity> subreddits = new ArrayList<>();

        AccountHelper accountHelper = App.getAccountHelper();
        checkIfAuthenticated(accountHelper);

        if (page == 1) {
            searchResults = new ArrayList<>();
            SubredditSearchPaginator searchPaginator = accountHelper.getReddit().searchSubreddits().query(query).sorting(SubredditSearchSort.RELEVANCE).build();
            searchResultsIterator = searchPaginator.iterator();
        }

        searchResults.addAll(searchResultsIterator.next());
        for (Subreddit subreddit : searchResults) {
            if (!subreddit.isNsfw()) {
                subreddits.add(SubredditEntity.fromSubreddit(subreddit));
            }
        }
        searchPage = page;
        searchQuery = query;

        return subreddits;
    }

    MutableLiveData<List<RedditPostEntity>> getRedditPosts(int page, boolean isRefresh) {
        if (redditPostsLiveData.getValue() == null || currentPage != page || isRefresh) {
            AppExecutors.getInstance().diskIO().execute(() -> {

                AccountHelper accountHelper = App.getAccountHelper();
                checkIfAuthenticated(accountHelper);

                if (page == 1) {
                    initSLOMap();
                    DefaultPaginator<Submission> paginator = accountHelper.getReddit().frontPage().sorting(SubredditSort.NEW).timePeriod(TimePeriod.ALL).build();
                    iterator = paginator.iterator();
                    submissions = new ArrayList<>();
                    redditPostEntities = new ArrayList<>();
                }

                try {
                    submissions = iterator.next();
                    for (Submission submission : submissions) {
                        if (submission.isNsfw())
                            continue;

                        Date current = new Date();
                        long diff = current.getTime() - submission.getCreated().getTime();
                        int days = Long.valueOf(diff / (24 * 60 * 60 * 1000)).intValue();

                        String subreddit = submission.getSubreddit();
                        if (sloMap.containsKey(subreddit)) {
                            List<Map.Entry<Integer, Integer>> sloPairs = sloMap.get(subreddit);
                            int index = -1;
                            for (int i = 0; i < sloPairs.size(); i++) {
                                Map.Entry<Integer, Integer> sloPair = sloPairs.get(i);
                                if (sloPair != null && sloPair.getKey().equals(days)) {
                                    index = i;
                                }
                            }
                            Map.Entry<Integer, Integer> sloPair = index != -1 ? sloPairs.get(index) : new AbstractMap.SimpleEntry<>(days, sloLimit);
                            if (index == -1) {
                                sloPairs.add(sloPair);
                            }

                            if (sloPair.getValue() == 0) {
                                continue;
                            }
                            sloPair.setValue(sloPair.getValue() - 1);
                        }
                        redditPostEntities.add(RedditPostEntity.fromSubmission(submission));
                    }
                    currentPage = page;
                    redditPostsLiveData.postValue(redditPostEntities);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    redditPostsLiveData.postValue(null);
                }
            });
        }
        return redditPostsLiveData;
    }

    private void checkIfAuthenticated(AccountHelper accountHelper) {
        List<String> usernames = App.getTokenStore().getUsernames();
        if (!accountHelper.isAuthenticated() && !usernames.isEmpty()) {
            accountHelper.trySwitchToUser(usernames.get(0));
        }
    }

    private void initSLOMap() {
        sloMap = new HashMap<>();
        List<String> sloSubredditNames = subredditDao.loadShowLessOftenSubredditNames();
        for (String subreddit : sloSubredditNames) {
            sloMap.put(subreddit, new ArrayList<>());
        }
    }

    void showLessOftenSubreddit(String subredditName) {
        Subreddit subreddit = App.getAccountHelper().getReddit().subreddit(subredditName).about();
        subredditDao.insertSubreddit(SubredditEntity.fromSubreddit(subreddit));
    }

    void showMoreOftenSubreddit(String subredditName) {
        SubredditEntity subreddit = subredditDao.findSubredditByName(subredditName);
        if (subreddit != null) {
            subredditDao.deleteSubreddit(subreddit);
        }
    }

    Integer getCurrentPage() {
        return currentPage;
    }

    MutableLiveData<List<RedditPostEntity>> getRedditPosts() {
        return redditPostsLiveData;
    }

    boolean moreRedditPostsExist() {
        return iterator.hasNext();
    }

    void addRedditPostToBookmarks(RedditPostEntity redditPost) {
        redditPost.setBookmarkDate(new Date());
        redditPostsDao.insertRedditPost(redditPost);
    }

    void removePostFromBookmarks(RedditPostEntity redditPost) {
        redditPostsDao.deleteRedditPost(redditPost);
    }

    boolean shouldShowLessOften(String name) {
        return subredditDao.shouldShowLessOften(name);
    }

    boolean isPostBookmarked(String id) {
        return redditPostsDao.isBookmarkedRedditPost(id);
    }

    String getSearchQuery() {
        return searchQuery;
    }

    boolean moreSubredditsExist() {
        return searchResultsIterator.hasNext();
    }

    int getSearchPage() {
        return searchPage;
    }
}