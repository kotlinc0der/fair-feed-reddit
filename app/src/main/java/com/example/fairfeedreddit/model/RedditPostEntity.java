package com.example.fairfeedreddit.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.example.fairfeedreddit.utils.AppConstants;

import net.dean.jraw.models.Submission;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.Date;

@Entity(tableName = "reddit_post")
public class RedditPostEntity implements Serializable {

    @NonNull
    @PrimaryKey
    private String id;
    private String url;
    private String text;
    private String title;
    private String subreddit;
    private String author;
    private String thumbnail;
    private Date createdAt;
    private Date bookmarkDate;
    private String commentCount;
    private String score;

    @Ignore
    private boolean isBookmarked;
    @Ignore
    private boolean shouldShowLessOftenSubreddit;

    public RedditPostEntity() {

    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id) {
        this.id = id;
    }

    public void setBookmarkDate(Date bookmarkDate) {
        this.bookmarkDate = bookmarkDate;
    }

    public static RedditPostEntity fromSubmission(Submission submission) {
        RedditPostEntity entity = new RedditPostEntity();
        entity.id = submission.getId();
        entity.author = submission.getAuthor();
        entity.commentCount = round(submission.getCommentCount());
        entity.createdAt = submission.getCreated();
        entity.subreddit = submission.getSubreddit();
        entity.text = submission.getSelfText();
        entity.title = submission.getTitle();
        entity.url = AppConstants.BASE_REDDIT_URL + submission.getPermalink();
        entity.bookmarkDate = new Date();
        entity.thumbnail = submission.getThumbnail();
        entity.score = round(submission.getScore());
        return entity;
    }

    // method implementation based on: https://stackoverflow.com/a/52773332/5826864
    private static String round(double quantity) {
        final String[] units = {"", "k", "m", "b"};
        int i = 0;
        while ((quantity / 1000) >= 1) {
            quantity = quantity / 1000;
            i++;
        }
        DecimalFormat decimalFormat = new DecimalFormat("#.##");
        decimalFormat.setRoundingMode(RoundingMode.FLOOR);
        return String.format("%s%s", decimalFormat.format(quantity), units[i]);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public Date getBookmarkDate() {
        return bookmarkDate;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public boolean isBookmarked() {
        return isBookmarked;
    }

    public void setBookmarked(boolean bookmarked) {
        isBookmarked = bookmarked;
    }

    public boolean shouldShowLessOften() {
        return shouldShowLessOftenSubreddit;
    }

    public void setShouldShowLessOften(boolean shouldShowLessOftenSubreddit) {
        this.shouldShowLessOftenSubreddit = shouldShowLessOftenSubreddit;
    }
}
