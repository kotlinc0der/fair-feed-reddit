package com.example.fairfeedreddit.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class AppExecutors {

    private static final Object LOCK = new Object();
    private static AppExecutors INSTANCE;
    private final Executor diskIO;

    private AppExecutors(Executor diskIO) {
        this.diskIO = diskIO;
    }

    public static AppExecutors getInstance() {
        if (INSTANCE == null) {
            synchronized (LOCK) {
                if (INSTANCE == null) {
                    INSTANCE = new AppExecutors(Executors.newSingleThreadExecutor());
                }
            }
        }
        return INSTANCE;
    }

    public Executor diskIO() {
        return diskIO;
    }

}
