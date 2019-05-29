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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;

public class NewsData {
    private final Context context;
    private final String url = "https://www.newsghana.com.gh/wp-json/wp/v2/posts?_embed&categories=35";
    private List<News> newsList;
    private NewsFetchEventListener newsFetchEventListener;

    public NewsData(Context context) {
        this.context = context;
        newsList = new ArrayList<>();
    }

    /**
     * Fetch news Items from an online source
     * todo: cache news items into an offline json file for 24hours
     */
    public void fetchNewsFromOnlineAsync() {
        if (newsFetchEventListener == null) {
            throw new IllegalArgumentException("NewsFetchEventListener must be supplied and cannot be null");
        }

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


                            DateFormat format = new SimpleDateFormat("yyyy-MM-d", Locale.ENGLISH);
                            Date date = null;

                            try {
                                date = format.parse(dateStr);
                            } catch (ParseException e) {
                                Log.e(DEBUG_TAG, e.getMessage());
                            }

                            newsList.add(new News(title, date, image/*images.get(i)*/, content));
                        }
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
}
