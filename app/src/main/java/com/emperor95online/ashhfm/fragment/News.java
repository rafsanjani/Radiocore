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

import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.NewsAdapter;
import com.emperor95online.ashhfm.pojo.NewsObject;

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
        addData();

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

}
