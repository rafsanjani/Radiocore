package com.foreverrafs.radiocore.model;


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
     * The list of News items which will be cached. These will be serialized into json as an array of news objects
     */
    private List<News> newsItems;

    /**
     * Parameterized constructor for a news Cache
     *
     * @param fetchTime The time this particular cache was created
     * @param newsItems The list of news items to be cached in a particular point in time
     */
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