package com.emperor95online.ashhfm.data;

import android.content.Context;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.model.News;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

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
     */
    public void fetchNewsFromOnlineAsync() {
        if (newsFetchEventListener == null) {
            throw new IllegalArgumentException("NewsFetchEventListener must be supplied and cannot be null");
        }

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONArray newsArray = new JSONArray(response);
                            for (int i = 0; i < newsArray.length(); i++) {
                                JSONObject newsObject = newsArray.getJSONObject(i);
                                String title = newsObject.getJSONObject("title").getString("rendered");
                                String content = newsObject.getJSONObject("content").getString("rendered");

//                                String image = "https://pbs.twimg.com/profile_images/425274582581264384/X3QXBN8C.jpeg";
                                String image = "http://www.51allout.co.uk/wp-content/uploads/2012/02/Image-not-found.gif";

                                if (!newsObject.getJSONObject("_embedded").isNull("wp:featuredmedia")) {
                                    image = newsObject.getJSONObject("_embedded")
                                            .getJSONArray("wp:featuredmedia").getJSONObject(0)
                                            .getString("source_url");
                                }
                                String date = newsObject.getString("date");

                                newsList.add(new News(title, date.substring(0, date.indexOf("T")), image/*images.get(i)*/, content));
                            }
                            newsFetchEventListener.onNewsFetched(newsList);

                        } catch (JSONException e) {
                            Toast.makeText(context, "JSON Exception", Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                newsFetchEventListener.onError(error);
            }
        });

        queue.add(stringRequest);
    }

    /**
     * Fetch news Items from in-memory. Only meant for debugging
     */
    public void fetchNewsFromLocalStore() {
        if (newsFetchEventListener == null) {
            throw new IllegalArgumentException("NewsFetchEventListener must be supplied and cannot be null");
        }
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));
        newsList.add(new News(context.getString(R.string.tt), "13th January, 2019", "image_url"));

        //Lets assume that no error can occur at this point
        newsFetchEventListener.onNewsFetched(newsList);
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
