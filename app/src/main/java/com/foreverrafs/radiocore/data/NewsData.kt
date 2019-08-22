package com.foreverrafs.radiocore.data

import android.annotation.SuppressLint
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import com.android.volley.Request
import com.android.volley.VolleyError
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.model.NewsCache
import com.foreverrafs.radiocore.util.RadioPreferences
import org.joda.time.DateTime
import org.joda.time.Period
import org.json.JSONArray
import org.json.JSONException
import java.io.*
import java.util.*

class NewsData(private val mContext: Context) {
    private val NEWS_URL = "https://newscentral.herokuapp.com/news"
    private var mTaskDelegate: TaskDelegate? = null

    private val mRadioPreferences: RadioPreferences

    init {
        mNewsList = ArrayList()

        mRadioPreferences = RadioPreferences(mContext)

    }

    fun fetchNews() {
        val cacheFilePath = mRadioPreferences.cacheFileName

        if (cacheFilePath == null) {
            Log.i(TAG, "Location of cache is not known. Possible because this is the first run or cache is corrupted. Loading from online")
            loadFromOnline()
            return
        }

        //try reading from the cache if it is available
        val cacheFile = File(cacheFilePath)

        if (cacheFile.exists()) {
            Log.i(TAG, "News cache hit. Trying to read from Cache")
            loadFromCache(cacheFilePath)
        } else {
            Log.i(TAG, "Cache doesn't exist in it's previously saved location. Clearing the dir and loading from online")
            loadFromOnline()
            clearCache(cacheFile)
        }
    }

    /**
     * Fetch news Items from an online source
     */
    private fun loadFromOnline() {
        if (mTaskDelegate == null) {
            throw IllegalArgumentException("TaskDelegate must be supplied and cannot be null")
        }

        val queue = Volley.newRequestQueue(mContext)

        val stringRequest = StringRequest(Request.Method.GET, NEWS_URL,
                { response ->
                    try {
                        val newsArray = JSONArray(response)
                        for (i in 0 until newsArray.length()) {
                            val newsObject = newsArray.getJSONObject(i)
                            val title = newsObject.getString("headline")

                            //replace all newline items with a paragraph
                            val content = newsObject.getString("content").replace("\n", "<p>")

                            val image = newsObject.getString("imageUrl")
                            val category = newsObject.getString("category")

                            val dateStr = newsObject.getString("date")

                            var date: DateTime? = null

                            try {
                                date = DateTime.parse(dateStr)
                            } catch (e: Exception) {
                                Log.e(TAG, e.message!!)
                            }

                            val newsItem = News(title, date, image, content, category)

                            mNewsList?.add(newsItem)
                        }
                        mTaskDelegate?.onAllNewsFetched(mNewsList)
                        saveToCache(mNewsList)

                    } catch (e: JSONException) {
                        Toast.makeText(mContext, "JSON Exception", Toast.LENGTH_SHORT).show()
                    }
                }, { error -> mTaskDelegate!!.onError(error) })

        queue.add(stringRequest)
    }

    /**
     * Fetch news Items from in-memory. Only meant for debugging
     */
    private fun fetchNewsFromLocalStore() {
        if (mTaskDelegate == null) {
            throw IllegalArgumentException("TaskDelegate must be supplied and cannot be null")
        }

        //try reading from the cache if the file has previously been created
        val cacheFilePath = mRadioPreferences.cacheFileName
        if (cacheFilePath != null) {
            Log.i(TAG, "News cache hit. Trying to read from Cache")
            loadFromCache(cacheFilePath)
            return
        }

        Log.i(TAG, "No News Cache found. Loading from online")

        val newsItems = ArrayList<News>()

        try {
            newsItems.add(News("The man is dead", DateTime.parse("2019-12-31"), "https://imageUrl.shutterstock.com/imageUrl-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"))
            newsItems.add(News("The man is dead", DateTime.parse("2019-12-31"), "https://imageUrl.shutterstock.com/imageUrl-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"))
            newsItems.add(News("The man is dead", DateTime.parse("2019-12-31"), "https://imageUrl.shutterstock.com/imageUrl-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"))
            newsItems.add(News("The man is dead", DateTime.parse("2019-12-31"), "https://imageUrl.shutterstock.com/imageUrl-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"))
            newsItems.add(News("The man is dead", DateTime.parse("2019-12-31"), "https://imageUrl.shutterstock.com/imageUrl-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"))

            mTaskDelegate!!.onAllNewsFetched(newsItems)
            saveToCache(newsItems)

        } catch (e: Exception) {
            Log.i(TAG, e.message!!)
        }

    }


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

            override fun onPostExecute(newsItems: MutableList<News>?) {
                mNewsList = newsItems

                if (newsItems == null) {
                    mTaskDelegate!!.onError(CacheFetchError("Error reading message from cache"))
                    return
                }
                mTaskDelegate!!.onAllNewsFetched(newsItems)
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
    fun setTaskDelegate(mTaskDelegate: TaskDelegate) {
        this.mTaskDelegate = mTaskDelegate
    }

    /**
     * Implement to register News Fetch events and propagate them accordingly
     */
    interface TaskDelegate {
        fun onAllNewsFetched(newsItems: List<News>?)

        fun onError(error: VolleyError)
    }

    companion object {
        private val TAG = "NewsData"
        var mNewsList: MutableList<News>? = null
    }
}
