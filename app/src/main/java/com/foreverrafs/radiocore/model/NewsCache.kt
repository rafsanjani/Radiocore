package com.foreverrafs.radiocore.model


import org.joda.time.DateTime

/**
 * Structure which represents cached news Items
 */
class NewsCache
/**
 * Parameterized constructor for a news Cache
 *
 * @param fetchTime The time this particular cache was created
 * @param newsItems The list of news items to be cached in a particular point in time
 */
(
        /**
         * The last time a news item was fetched. It will be serialized as json together with the news entries
         */
        //TODO: use SQLite for caching
        val fetchTime: DateTime,
        /**
         * The list of News items which will be cached. These will be serialized into json as an array of news objects
         */
        val newsItems: MutableList<News>?
)