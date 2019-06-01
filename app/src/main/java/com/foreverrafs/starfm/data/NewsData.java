package com.foreverrafs.starfm.data;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.foreverrafs.starfm.model.News;
import com.foreverrafs.starfm.util.Preference;
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

import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;

public class NewsData {
    private final Context context;
    private final String url = "https://www.newsghana.com.gh/wp-json/wp/v2/posts?_embed&categories=35";
    private List<News> newsList;
    private NewsFetchEventListener newsFetchEventListener;

    private Preference preference;

    public NewsData(Context context) {
        this.context = context;
        newsList = new ArrayList<>();

        preference = new Preference(context);
    }

    /**
     * Fetch news Items from an online source
     * todo: cache news items into an offline json file for 24hours
     */
    public void fetchNewsFromOnlineAsync() {
        if (newsFetchEventListener == null) {
            throw new IllegalArgumentException("NewsFetchEventListener must be supplied and cannot be null");
        }

        //try reading from the cache if the file has previously been created
        String cacheFilePath = preference.getCacheFileName();
        if (cacheFilePath != null) {
            Log.i(DEBUG_TAG, "News cache hit. Trying to read from Cache");
            readFromCache(cacheFilePath);
            return;
        }

        Log.i(DEBUG_TAG, "No News Cache found. Loading from online");


        //only proceed with an online request when there is nothing in the cache or the cache items exceed 24 hours, in that case
        //it will be cleared for news to be fetched from online
        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                response -> {
                    try {
                        JSONArray newsArray = new JSONArray(response);
                        for (int i = 0; i < newsArray.length(); i++) {
                            JSONObject newsObject = newsArray.getJSONObject(i);
                            String title = newsObject.getJSONObject("title").getString("rendered");
                            String content = newsObject.getJSONObject("content").getString("rendered");

                            String image = "http://www.51allout.co.uk/wp-content/uploads/2012/02/Image-not-found.gif";

                            if (!newsObject.getJSONObject("_embedded").isNull("wp:featuredmedia")) {
                                image = newsObject.getJSONObject("_embedded")
                                        .getJSONArray("wp:featuredmedia").getJSONObject(0)
                                        .getString("source_url");
                            }
                            String dateStr = newsObject.getString("date");
                            dateStr = dateStr.substring(0, dateStr.indexOf("T"));


//                            DateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
                            DateTime date = null;

                            try {
                                date = DateTime.parse(dateStr);
                            } catch (Exception e) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }

                            newsList.add(new News(title, date, image/*images.get(i)*/, content));
                        }
                        //todo: persist news objects in json file for caching
                        saveToCache(newsList);
                        newsFetchEventListener.onNewsFetched(newsList);

                    } catch (JSONException e) {
                        Toast.makeText(context, "JSON Exception", Toast.LENGTH_SHORT).show();
                    }
                }, error -> newsFetchEventListener.onError(error));

        queue.add(stringRequest);
    }

    /**
     * Fetch news Items from in-memory. Only meant for debugging
     */
    public void fetchNewsFromLocalStore() {
        if (newsFetchEventListener == null) {
            throw new IllegalArgumentException("NewsFetchEventListener must be supplied and cannot be null");
        }

        //try reading from the cache if the file has previously been created
        String cacheFilePath = preference.getCacheFileName();
        if (cacheFilePath != null) {
            Log.i(DEBUG_TAG, "News cache hit. Trying to read from Cache");
            readFromCache(cacheFilePath);
            return;
        }

        Log.i(DEBUG_TAG, "No News Cache found. Loading from online");

        List<News> newsItems = new ArrayList<>();

        try {
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide"));
            newsItems.add(new News("The man is dead", DateTime.parse("2019-12-31"), "https://image.shutterstock.com/image-vector/breaking-news-vector-illustration-background-450w-725898868.jpg", "The man has committed suicide"));

            newsFetchEventListener.onNewsFetched(newsItems);
            saveToCache(newsItems);

        } catch (Exception e) {
            Log.i(DEBUG_TAG, e.getMessage());
        }

    }

    private void saveToCache(List<News> newsItems) {
        NewsCache newsCache = new NewsCache(DateTime.now(), newsItems);
        final String fileName = "newscache";

        Gson gson = NewsJson.getInstance();

        String newsCacheJson = gson.toJson(newsCache);

        try {
            File file = File.createTempFile(fileName, ".json", context.getCacheDir());

            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(newsCacheJson.getBytes());
            fileOutputStream.close();

            Log.i(DEBUG_TAG, "Cache Saved at " + file.getAbsolutePath());
            preference.setCacheFileName(file.getAbsolutePath());

        } catch (IOException exception) {
            Log.e(DEBUG_TAG, exception.getMessage());
        }
    }

    private void readFromCache(String cacheFilePath) {
        final int defaultCacheExpiryHours = 5;
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

            String fileAsString = stringBuilder.toString();
            Gson gson = NewsJson.getInstance();

            NewsCache newsCache = gson.fromJson(fileAsString, NewsCache.class);

            //lets calculate how many hours have elapsed since the last news items were cached.
            //a news cache can expire after some specified hours. The default is 5
            Period interval = new Period(newsCache.fetchTime, DateTime.now());
            int hoursElapsed = interval.getHours();

            if (hoursElapsed < defaultCacheExpiryHours) {
                Log.i(DEBUG_TAG, "Reading from cache. Local cache expires in " + (defaultCacheExpiryHours - hoursElapsed) + " hours");
                newsFetchEventListener.onNewsFetched(newsCache.getNewsItems());
            } else {
                Log.i(DEBUG_TAG, "Cache expired. Deleting it now");
                if (cacheFile.delete()) {
                    Log.i(DEBUG_TAG, "Successfully deleted cache file" + cacheFile.getName());
                    Log.i(DEBUG_TAG, "Loading news from online");
                    preference.removeCacheFileEntry();
                    fetchNewsFromOnlineAsync();
                }
            }


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * This method must be called in every class which wants to fetch news items else
     * an IllegalArgumentExeption will be thrown at runtime
     *
     * @param newsFetchEventListener
     */
    public void setNewsFetchEventListener(NewsFetchEventListener newsFetchEventListener) {
        this.newsFetchEventListener = newsFetchEventListener;
    }

    /**
     * Implement to register News Fetch events and propagate them accordingly
     */
    public interface NewsFetchEventListener {
        void onNewsFetched(List<News> newsList);

        void onError(VolleyError error);
    }

    private class NewsCache {
        private DateTime fetchTime;
        private List<News> newsItems;

        public NewsCache(DateTime fetchTime, List<News> newsItems) {
            this.fetchTime = fetchTime;
            this.newsItems = newsItems;
        }

        public DateTime getFetchTime() {
            return fetchTime;
        }

        public void setFetchTime(DateTime fetchTime) {
            this.fetchTime = fetchTime;
        }

        public List<News> getNewsItems() {
            return newsItems;
        }

        public void setNewsItems(List<News> newsItems) {
            this.newsItems = newsItems;
        }
    }
}
