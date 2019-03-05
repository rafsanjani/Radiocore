package com.emperor95online.ashhfm.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.NewsAdapter;
import com.emperor95online.ashhfm.pojo.NewsObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class News extends Fragment {

    private RecyclerView recyclerView;

    private ArrayList<NewsObject> news;
    private NewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        news = new ArrayList<>();
//        addData();
        getData();

        newsAdapter = new NewsAdapter(getActivity(), news);
        recyclerView.setAdapter(newsAdapter);

        return view;
    }

    void addData(){
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
        news.add(new NewsObject(getActivity().getString(R.string.tt), "13th January, 2019", "image_url"));
    }

    private void getData(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(getActivity());
//        final String url = "https://newsapi.org/v2/top-headlines?" +
//                "country=us&" +
//                "apiKey=3146247b8179456995f1499b15587f69";

        final String url = "https://newsapi.org/v2/everything?" +
                "q=Ghana&" +
                "from=2019-03-04&" +
                "sortBy=popularity&" +
                "apiKey=3146247b8179456995f1499b15587f69";

// Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject jo = new JSONObject(response);
                            JSONArray ja = jo.getJSONArray("articles");
                            for (int i = 0; i < ja.length(); i++){
                                JSONObject jsonObject = ja.getJSONObject(i);
                                String title = jsonObject.getString("title");
                                String image = jsonObject.getString("urlToImage");
                                String date = jsonObject.getString("publishedAt");

                                news.add(new NewsObject(title, date, image));
                                newsAdapter.notifyDataSetChanged();
                            }
                        }catch (JSONException e){
                            Toast.makeText(getActivity(), e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(getActivity(), "Network error ...", Toast.LENGTH_SHORT).show();
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);

    }

}
