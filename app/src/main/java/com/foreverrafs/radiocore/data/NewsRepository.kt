package com.foreverrafs.radiocore.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.model.NewsCache
import com.foreverrafs.radiocore.util.RadioPreferences
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import org.joda.time.DateTime
import org.joda.time.Period
import java.io.*

class NewsRepository(private val mContext: Context) {
    companion object {
        private val TAG = "NewsRepository"
        private lateinit var mNewsList: List<News>

        fun getNewsItems(): List<News>? {
            return mNewsList.toList()
        }

        fun saveNewsItems(newsItems: List<News>) {
            mNewsList = newsItems
        }

        fun loadFromOnline(): Observable<List<News>> {
            Log.i(TAG, "running on : ${Thread.currentThread().name}")
            val newsService = ServiceGenerator.createService(NewsService::class.java)
            return newsService.allNews.subscribeOn(Schedulers.newThread()).observeOn(AndroidSchedulers.mainThread())
        }

    }

    private val mRadioPreferences: RadioPreferences = RadioPreferences(mContext)

    fun fetchNews() {
        val cacheFilePath = mRadioPreferences.cacheFileName

        if (cacheFilePath == null) {
            Log.i(TAG, "Location of cache is not known. Possible because this is the first run or cache is corrupted. Loading from online")
            // loadFromOnlineAsync()
            return
        }

        //try reading from the cache if it is available
        val cacheFile = File(cacheFilePath)

        if (cacheFile.exists()) {
            Log.i(TAG, "News cache hit. Trying to read from Cache")
            loadFromCache(cacheFilePath)
        } else {
            Log.i(TAG, "Cache doesn't exist in it's previously saved location. Clearing the dir and loading from online")
            //loadFromOnlineAsync()
            clearCache(cacheFile)
        }
    }

    /**
     * Fetch news Items from an online source
     */


    private fun saveToCache(newsItems: List<News>?) {
        val task = @SuppressLint("StaticFieldLeak")
        object : AsyncTask<List<News>, Void, Void>() {
            @SafeVarargs
            override fun doInBackground(vararg lists: List<News>): Void? {
                val newsItems = mutableListOf<News>()
                newsItems.addAll(lists[0])

                val newsCache = NewsCache(DateTime.now(), newsItems)
                val fileName = "newscache"

                val gson = NewsJson.getInstance()

                val newsCacheJson = gson.toJson(newsCache)

                try {
                    val file = File.createTempFile(fileName, ".json", mContext.cacheDir)

                    val fileOutputStream = FileOutputStream(file)
                    fileOutputStream.write(newsCacheJson.toByteArray())
                    fileOutputStream.close()

                    mRadioPreferences.cacheFileName = file.absolutePath

                } catch (exception: IOException) {
                    Log.e(TAG, exception.message.toString())
                }

                return null
            }
        }

        task.execute(newsItems)
    }

    @SuppressLint("StaticFieldLeak")
    private fun loadFromCache(cacheFilePath: String) {
        val task = object : AsyncTask<String, Void, MutableList<News>>() {
            override fun doInBackground(vararg strings: String): MutableList<News>? {
                val cacheFilePath = strings[0]

                val defaultCacheExpiryHours = Integer.parseInt(mRadioPreferences.cacheExpiryHours!!)
                val cacheFile = File(cacheFilePath)


                try {
                    val fileInputStream = FileInputStream(cacheFile)
                    val bufferedReader = BufferedReader(InputStreamReader(fileInputStream))

                    var line: String? = bufferedReader.readLine()
                    val stringBuilder = StringBuilder()

                    while (line != null) {
                        stringBuilder.append(line).append("\n")
                        line = bufferedReader.readLine()
                    }

                    fileInputStream.close()
                    bufferedReader.close()

                    val fileAsString = stringBuilder.toString()
                    val gson = NewsJson.getInstance()

                    val newsCache = gson.fromJson(fileAsString, NewsCache::class.java)

                    //lets calculate how many hours have elapsed since the last news items were cached.
                    //a news cache can expire after some specified hours. The default is 5
                    val interval = Period(newsCache.fetchTime, DateTime.now())
                    val hoursElapsed = interval.hours

                    if (hoursElapsed < defaultCacheExpiryHours) {
                        Log.i(TAG, "Reading from cache. Local cache expires in " + (defaultCacheExpiryHours - hoursElapsed) + " hours")
                        return newsCache.newsItems
                    } else {
                        Log.i(TAG, "Cache expired. Deleting it now")
                        clearCache(cacheFile)
                        Log.i(TAG, "Loading news from online")
                        loadFromOnline()
                    }

                } catch (e: IOException) {
                    cacheFile.delete()
                    e.printStackTrace()
                    Log.e(TAG, e.message!!)
                }
                return null

            }

            override fun onPostExecute(newsItems: MutableList<News>) {
                mNewsList = newsItems


            }
        }
        task.execute(cacheFilePath)
    }

    private fun clearCache(cacheFile: File) {
        cacheFile.delete()
        mRadioPreferences.removeCacheFileEntry()
    }

    /**
     * This method must be called in every class which wants to fetch news items else
     * an IllegalArgumentExeption will be thrown at runtime
     *
     * @param mTaskDelegate the listener which will propagate the news fetching events
     */
}

