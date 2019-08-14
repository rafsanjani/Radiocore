package com.foreverrafs.radiocore.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.foreverrafs.radiocore.model.News;
import com.foreverrafs.radiocore.model.NewsCache;
import com.foreverrafs.radiocore.util.RadioPreferences;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static com.foreverrafs.radiocore.util.Constants.DEBUG_TAG;

public class NewsData {
    public static List<News> mNewsList;
    private final Context mContext;
    private final String NEWS_URL = "https://newscentral.herokuapp.com/news";
    private TaskDelegate mTaskDelegate;

    private RadioPreferences mRadioPreferences;

    public NewsData(Context mContext) {
        this.mContext = mContext;
        mNewsList = new ArrayList<>();

        mRadioPreferences = new RadioPreferences(mContext);

    }

    public void fetchNews() {
        String cacheFilePath = mRadioPreferences.getCacheFileName();

        if (cacheFilePath == null) {
            Log.i(DEBUG_TAG, "Location of cache is not known. Possible because this is the first run or cache is corrupted. Loading from online");
            loadFromOnline();
            return;
        }

        //try reading from the cache if it is available
        File cacheFile = new File(cacheFilePath);

        if (cacheFile.exists()) {
            Log.i(DEBUG_TAG, "News cache hit. Trying to read from Cache");
            loadFromCache(cacheFilePath);
        } else {
            Log.i(DEBUG_TAG, "Cache doesn't exist in it's previously saved location. Clearing the dir and loading from online");
            loadFromOnline();
            clearCache(cacheFile);
        }
    }

    /**
     * Fetch news Items from an online source
     */
    private void loadFromOnline() {
        if (mTaskDelegate == null) {
            throw new IllegalArgumentException("TaskDelegate must be supplied and cannot be null");
        }

        RequestQueue queue = Volley.newRequestQueue(mContext);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, NEWS_URL,
                response -> {
                    try {
                        JSONArray newsArray = new JSONArray(response);
                        for (int i = 0; i < newsArray.length(); i++) {
                            JSONObject newsObject = newsArray.getJSONObject(i);
                            String title = newsObject.getString("headline");

                            //replace all newline items with a paragraph
                            String content = newsObject.getString("content").replace("\n", "<p>");

                            String image = newsObject.getString("imageUrl");
                            String category = newsObject.getString("category");

                            String dateStr = newsObject.getString("date");

                            DateTime date = null;

                            try {
                                date = DateTime.parse(dateStr);
                            } catch (Exception e) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }
                            final News newsItem = new News(title, date, image, content, category);

                            mTaskDelegate.onNewsItemFetched(newsItem);
                            mNewsList.add(newsItem);
                        }
                        mTaskDelegate.onAllNewsFetched(mNewsList);
                        saveToCache(mNewsList);

                    } catch (JSONException e) {
                        Toast.makeText(mContext, "JSON Exception", Toast.LENGTH_SHORT).show();
                    }
                }, error -> mTaskDelegate.onError(error));

        queue.add(stringRequest);
    }

    /**
     * Fetch news Items from in-memory. Only meant for debugging
     */
    private void fetchNewsFromLocalStore() {
        if (mTaskDelegate == null) {
            throw new IllegalArgumentException("TaskDelegate must be supplied and cannot be null");
        }

        //try reading from the cache if the file has previously been created
        String cacheFilePath = mRadioPreferences.getCacheFileName();
        if (cacheFilePath != null) {
            Log.i(DEBUG_TAG, "News cache hit. Trying to read from Cache");
            loadFromCache(cacheFilePath);
            return;
        }

        Log.i(DEBUG_TAG, "No News Cache found. Loading from online");

        List<News> newsItems = new ArrayList<>();

        try {
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide", "random"));

            mTaskDelegate.onAllNewsFetched(newsItems);
            saveToCache(newsItems);

        } catch (Exception e) {
            Log.i(DEBUG_TAG, e.getMessage());
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void saveToCache(List<News> newsItems) {
        AsyncTask<List<News>, Void, Void> task = new AsyncTask<List<News>, Void, Void>() {
            @SafeVarargs
            @Override
            protected final Void doInBackground(List<News>... lists) {
                List<News> newsItems = lists[0];

                NewsCache newsCache = new NewsCache(DateTime.now(), newsItems);
                final String fileName = "newscache";

                Gson gson = NewsJson.getInstance();

                String newsCacheJson = gson.toJson(newsCache);

                try {
                    File file = File.createTempFile(fileName, ".json", mContext.getCacheDir());

                    FileOutputStream fileOutputStream = new FileOutputStream(file);
                    fileOutputStream.write(newsCacheJson.getBytes());
                    fileOutputStream.close();

                    mRadioPreferences.setCacheFileName(file.getAbsolutePath());

                } catch (IOException exception) {
                    Log.e(DEBUG_TAG, exception.getMessage());
                }
                return null;
            }
        };

        task.execute(newsItems);
    }

    @SuppressLint("StaticFieldLeak")
    private void loadFromCache(String cacheFilePath) {
        AsyncTask<String, Void, List<News>> task = new AsyncTask<String, Void, List<News>>() {
            @Override
            protected List<News> doInBackground(String... strings) {
                String cacheFilePath = strings[0];

                final int defaultCacheExpiryHours = Integer.parseInt(mRadioPreferences.getCacheExpiryHours());
                final File cacheFile = new File(cacheFilePath);


                try {
                    FileInputStream fileInputStream = new FileInputStream(cacheFile);
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(fileInputStream));

                    String line = bufferedReader.readLine();
                    StringBuilder stringBuilder = new StringBuilder();

                    while (line != null) {
                        stringBuilder.append(line).append("\n");
                        line = bufferedReader.readLine();
                    }

                    fileInputStream.close();
                    bufferedReader.close();

                    String fileAsString = stringBuilder.toString();
                    Gson gson = NewsJson.getInstance();

                    NewsCache newsCache = gson.fromJson(fileAsString, NewsCache.class);

                    //lets calculate how many hours have elapsed since the last news items were cached.
                    //a news cache can expire after some specified hours. The default is 5
                    Period interval = new Period(newsCache.getFetchTime(), DateTime.now());
                    int hoursElapsed = interval.getHours();

                    if (hoursElapsed < defaultCacheExpiryHours) {
                        Log.i(DEBUG_TAG, "Reading from cache. Local cache expires in " + (defaultCacheExpiryHours - hoursElapsed) + " hours");
                        return newsCache.getNewsItems();
                    } else {
                        Log.i(DEBUG_TAG, "Cache expired. Deleting it now");
                        clearCache(cacheFile);
                        Log.i(DEBUG_TAG, "Loading news from online");
                        loadFromOnline();
                    }

                } catch (IOException e) {
                    cacheFile.delete();
                    e.printStackTrace();
                    Log.e(DEBUG_TAG, e.getMessage());
                }

                return null;
            }

            @Override
            protected void onPostExecute(List<News> newsItems) {
                NewsData.mNewsList = newsItems;

                if (newsItems == null) {
                    mTaskDelegate.onError(new CacheFetchError("Error reading message from cache"));
                    return;
                }
                for (News newsItem : newsItems) {
                    mTaskDelegate.onNewsItemFetched(newsItem);
                }
                mTaskDelegate.onAllNewsFetched(newsItems);
            }
        };
        task.execute(cacheFilePath);
    }

    private void clearCache(File cacheFile) {
        cacheFile.delete();
        mRadioPreferences.removeCacheFileEntry();
    }

    /**
     * This method must be called in every class which wants to fetch news items else
     * an IllegalArgumentExeption will be thrown at runtime
     *
     * @param mTaskDelegate the listener which will propagate the news fetching events
     */
    public void setTaskDelegate(TaskDelegate mTaskDelegate) {
        this.mTaskDelegate = mTaskDelegate;
    }

    /**
     * Implement to register News Fetch events and propagate them accordingly
     */
    public interface TaskDelegate {
        void onAllNewsFetched(List<News> newsItems);

        void onNewsItemFetched(News newsItem);

        void onError(VolleyError error);
    }
}
