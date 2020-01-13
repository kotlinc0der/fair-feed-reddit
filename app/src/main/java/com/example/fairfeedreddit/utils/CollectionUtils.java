package com.example.fairfeedreddit.utils;

import java.util.List;

public final class CollectionUtils {

    public static boolean isEmpty(List<?> collection) {
        return collection == null || collection.isEmpty();
    }

    public static boolean isNonEmpty(List<?> collection) {
        return collection != null && !collection.isEmpty();
    }
}
