package com.foreverrafs.radiocore.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.util.Log
import com.foreverrafs.radiocore.api.NewsService
import com.foreverrafs.radiocore.api.ServiceGenerator
import com.foreverrafs.radiocore.data.NewsDatabaseContract.NewsInfo
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.RadioPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Hours

class NewsService(mContext: Context) {
    private val mRadioPreferences: RadioPreferences = RadioPreferences(mContext)
    private val mNewsOpenHelper: NewsOpenHelper = NewsOpenHelper(mContext)

    companion object {
        private val TAG = "NewsService"
    }

    fun saveNewsItems(newsItems: List<News>) {
        NewsDataManager.RadioCoreNews = newsItems
    }

    private fun loadFromOnline(): Observable<List<News>> {
        Log.i(TAG, "loadFromOnline: Loading news items from Online")
        val newsService = ServiceGenerator.createService(NewsService::class.java)
        return newsService.allNews
                .subscribeOn(Schedulers.newThread())
                .map { items ->
                    mRadioPreferences.cacheStorageTime = DateTime.now()
                    saveToDatabase(items)
                    items
                }
                .observeOn(AndroidSchedulers.mainThread())
    }


    fun fetchNews(): Observable<List<News>> {
        val expiryHours = Integer.parseInt(mRadioPreferences.cacheExpiryHours!!)
        val fetchedDate = mRadioPreferences.cacheStorageTime
        val elapsedHours = Hours.hoursBetween(fetchedDate, DateTime.now())

        if (elapsedHours.hours < expiryHours) {
            val newsItems = loadFromDatabase()

            return if (newsItems != null && newsItems.isNotEmpty()) {
                Observable
                        .just(newsItems)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
            } else {
                Log.i(TAG, "fetchNews: Error loading from local storage. Trying online...")
                loadFromOnline()
            }
        }
        Log.i(TAG, "fetchNews: Cache Expired. Trying online...")
        return loadFromOnline()

    }

    /***
     * Loads news items from a local database if the expiry time has not elapsed
     */
    private fun loadFromDatabase(): List<News>? {
        Log.i(TAG, "loadFromDatabase: Loading from local storage")


        val newsList = ArrayList<News>()
        var newsCursor: Cursor? = null

        try {
            val db = mNewsOpenHelper.readableDatabase

            val columns = arrayOfNulls<String>(5)
            columns[0] = NewsInfo.COLUMN_NEWS_HEADLINE
            columns[1] = NewsInfo.COLUMN_NEWS_DATE
            columns[2] = NewsInfo.COLUMN_NEWS_CONTENT
            columns[3] = NewsInfo.COLUMN_NEWS_IMAGEURL
            columns[4] = NewsInfo.COLUMN_NEWS_CATEGORY

            newsCursor = db.query(NewsInfo.TABLE_NAME, columns,
                    null, null, null, null, "${NewsInfo.COLUMN_NEWS_DATE} DESC")

            if (newsCursor.count == 0) {
                newsCursor?.close()
                mNewsOpenHelper.close()
                return null
            }

            val newsHeadlinePos = newsCursor.getColumnIndex(NewsInfo.COLUMN_NEWS_HEADLINE)
            val newsDatePos = newsCursor.getColumnIndex(NewsInfo.COLUMN_NEWS_DATE)
            val newsContentPos = newsCursor.getColumnIndex(NewsInfo.COLUMN_NEWS_CONTENT)
            val newsImagePos = newsCursor.getColumnIndex(NewsInfo.COLUMN_NEWS_IMAGEURL)
            val newsCategoryPos = newsCursor.getColumnIndex(NewsInfo.COLUMN_NEWS_CATEGORY)

            while (newsCursor.moveToNext()) {
                val headline = newsCursor.getString(newsHeadlinePos)
                val dateStr = newsCursor.getString(newsDatePos)
                val content = newsCursor.getString(newsContentPos)
                val imageUrl = newsCursor.getString(newsImagePos)
                val category = newsCursor.getString(newsCategoryPos)

                val date = DateTime.parse(dateStr)
                newsList.add(News(headline, date, imageUrl, content, category))
            }
        } catch (ex: Exception) {

        } finally {
            newsCursor?.close()
            mNewsOpenHelper.close()
        }
        return newsList
    }

    private fun saveToDatabase(newsItems: List<News>) {
        val values = ContentValues()
        val db = mNewsOpenHelper.writableDatabase
        db.execSQL("DELETE FROM ${NewsInfo.TABLE_NAME}")

        for (newsItem in newsItems) {
            values.put(NewsInfo.COLUMN_NEWS_CATEGORY, newsItem.category)
            values.put(NewsInfo.COLUMN_NEWS_HEADLINE, newsItem.headline)
            values.put(NewsInfo.COLUMN_NEWS_CONTENT, newsItem.content)
            values.put(NewsInfo.COLUMN_NEWS_DATE, newsItem.date!!.toString())
            values.put(NewsInfo.COLUMN_NEWS_IMAGEURL, newsItem.imageUrl)
            db.insert(NewsInfo.TABLE_NAME, null, values)
        }
        db.close()
    }
}

