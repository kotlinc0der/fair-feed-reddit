package com.example.fairfeedreddit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import net.dean.jraw.models.Subreddit;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Date;

@Entity(tableName = "subreddit")
public class SubredditEntity implements Serializable {

    @NonNull
    @PrimaryKey
    private String id;
    private String name;
    private String description;
    private String url;
    private Integer subscriberCount;
    private Date showLessOftenDate;

    public SubredditEntity(@NotNull String id, String name, String description, String url, Integer subscriberCount, Date showLessOftenDate) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.url = url;
        this.subscriberCount = subscriberCount;
        this.showLessOftenDate = showLessOftenDate;
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getSubscriberCount() {
        return subscriberCount;
    }

    public void setSubscriberCount(Integer subscriberCount) {
        this.subscriberCount = subscriberCount;
    }

    public Date getShowLessOftenDate() {
        return showLessOftenDate;
    }

    public void setShowLessOftenDate(Date showLessOftenDate) {
        this.showLessOftenDate = showLessOftenDate;
    }

    public static SubredditEntity fromSubreddit(Subreddit subreddit) {
        return new SubredditEntity(subreddit.getId(), subreddit.getName(), subreddit.getPublicDescription(),
                subreddit.getUrl(), subreddit.getSubscribers(), new Date());
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
