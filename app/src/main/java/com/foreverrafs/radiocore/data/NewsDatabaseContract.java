package com.foreverrafs.radiocore.data;

import android.provider.BaseColumns;

public final class NewsDatabaseContract {
    private NewsDatabaseContract() {
    }


    public static final class NewsInfo implements BaseColumns {
        public static final String TABLE_NAME = "news_entry";

        public static final String COLUMN_NEWS_HEADLINE = "news_headline";
        public static final String COLUMN_NEWS_CONTENT = "news_content";
        public static final String COLUMN_NEWS_DATE = "news_date";
        public static final String COLUMN_NEWS_IMAGEURL = "news_imageurl";
        public static final String COLUMN_NEWS_CATEGORY = "news_category";

        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE " + TABLE_NAME + "(" +
                        _ID + " INTEGER PRIMARY KEY, " +
                        COLUMN_NEWS_HEADLINE + " TEXT NOT NULL, " +
                        COLUMN_NEWS_CONTENT + " TEXT NOT NULL, " +
                        COLUMN_NEWS_CATEGORY + " TEXT NOT NULL, " +
                        COLUMN_NEWS_DATE + " TEXT NOT NULL, " +
                        COLUMN_NEWS_IMAGEURL + " TEXT NOT NULL " +
                        ")";
    }
}
