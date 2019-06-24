package com.foreverrafs.starfm.model;


import org.joda.time.DateTime;

import java.util.List;

/**
 * Structure which represents cached news Items
 */
public class NewsCache {
    /**
     * The last time a news item was fetched. It will be serialized as json together with the news entries
     */
    private DateTime fetchTime;

    /**
     * The lis of News items which will be cached. These will be serialized into json as an array of news objects
     */
    private List<News> newsItems;

    public NewsCache(DateTime fetchTime, List<News> newsItems) {
        this.fetchTime = fetchTime;
        this.newsItems = newsItems;
    }

    public DateTime getFetchTime() {
        return fetchTime;
    }

    public List<News> getNewsItems() {
        return newsItems;
    }
}