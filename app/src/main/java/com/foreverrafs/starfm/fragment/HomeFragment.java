package com.foreverrafs.starfm.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.foreverrafs.starfm.NewsDetailActivity;
import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.adapter.NewsAdapter;
import com.foreverrafs.starfm.data.NewsData;
import com.foreverrafs.starfm.model.News;
import com.foreverrafs.starfm.util.Constants;

import java.util.ArrayList;
import java.util.List;


// Created by Emperor95 on 1/13/2019.

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    // private ImageButton more_main, imBtn;

    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;

    private ArrayList<News> newsList;
    private ArrayList<String> images;
    private NewsAdapter newsAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsList = new ArrayList<>();
        images = new ArrayList<>();

        swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        progressBar = view.findViewById(R.id.progressBar2);
        //newsAdapter = new NewsAdapter(getActivity(), newsList);

        //recyclerView.setAdapter(newsAdapter);

        getNewsData();

        return view;
    }

    @Override
    public void onRefresh() {
        if (newsAdapter == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        // Clear the news items before adding new set of data
        newsList.clear();
        newsAdapter.notifyDataSetChanged();

        getNewsData();
    }

    //todo: Rename this method and push it into a new file
    private void getNewsData() {
        //final List<NewsFragment> newsList = new ArrayList<>();
        if (getActivity() == null) {
            return;
        }

        NewsData newsData = new NewsData(getContext());

        newsData.setNewsFetchEventListener(new NewsData.NewsFetchEventListener() {
            @Override
            public void onNewsFetched(List<News> fetchedNewsItems) {
                newsAdapter = new NewsAdapter(getContext(), fetchedNewsItems);

                //todo: make activity the activity implement this interface to keep oncreate clean enough
                newsAdapter.setOnNewsItemClickListener(new NewsAdapter.NewsItemClickListener() {
                    @Override
                    public void onNewItemClicked(News newsObject, Pair[] pairs, int position) {
                        NewsAdapter.NewsHolder newsHolder = (NewsAdapter.NewsHolder) recyclerView.findViewHolderForAdapterPosition(position);

                        String[] transitionNames = new String[]{
                                ViewCompat.getTransitionName(newsHolder.getImageImageView()),// newsHolder.getHeadlineTextView().getTransitionName(),
                                ViewCompat.getTransitionName(newsHolder.getHeadlineTextView())
                        };

                        Intent intent = new Intent(getContext(), NewsDetailActivity.class);
                        intent.putExtra("title", newsObject.getHeadline());
                        intent.putExtra("content", newsObject.getContent());
                        intent.putExtra("image", newsObject.getImage());
                        intent.putExtra("date", newsObject.getDate());

                        //also pass this for shared element transition
                        intent.putExtra("transitions", transitionNames);

                        ActivityOptions activityOptions = null;

                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            activityOptions = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                                    pairs);
                        }

                        startActivity(intent, activityOptions.toBundle());
                    }
                });

                recyclerView.setAdapter(newsAdapter);

                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            }

            @Override
            public void onError(VolleyError error) {
                if (error instanceof NetworkError) {
                    final String message = "Network Error::Are you online?";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    Log.i(Constants.DEBUG_TAG, message);
                    progressBar.setVisibility(View.GONE);
                }
            }
        });

        newsData.fetchNewsFromOnlineAsync();
    }

}