package com.foreverrafs.starfm.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
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

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.foreverrafs.starfm.util.Constants.DEBUG_TAG;


// Created by Emperor95 on 1/13/2019.
public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final String NEWS_ITEM_EXTRA = "com.foreverrafs.starfm.news_extra";
    public static final String IMAGE_TRANSITION_NAME_EXTRA = "com.foreverrafs.starfm.newsfragment.image_transition_name_extra";

    @BindView(R.id.swipe_refresh_news)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;

    @BindView(R.id.progress_news_loading)
    ProgressBar progressBar;
    @BindView(R.id.content_no_connection)
    ViewGroup contentNoConnection;
    private ArrayList<News> newsList;
    private NewsAdapter newsAdapter, cachedAdapter;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        ButterKnife.bind(this, view);
        newsList = new ArrayList<>();

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        getNewsData();

        return view;
    }


    @OnClick(R.id.button_retry)
    public void onRetryClicked() {
        contentNoConnection.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        getNewsData();
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

    /**
     * Fetch news Items from either a remote store or a local store
     */
    private void getNewsData() {
        if (getActivity() == null) {
            return;
        }

        if (newsAdapter != null && newsAdapter.getItemCount() != 0)
            cachedAdapter = newsAdapter;

        NewsData newsData = new NewsData(getContext());

        newsData.setNewsFetchEventListener(new NewsData.NewsFetchEventListener() {
            @Override
            public void onNewsFetched(List<News> fetchedNewsItems) {
                newsAdapter = new NewsAdapter(getContext(), fetchedNewsItems);

                setUpNewsItemClickListener();

                recyclerView.setAdapter(newsAdapter);

                progressBar.setVisibility(View.GONE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                contentNoConnection.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onError(VolleyError error) {
                Log.e(DEBUG_TAG, error.toString());
                if (error instanceof NetworkError) {
                    final String message = "Network Error::Are you online?";
                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    Log.i(DEBUG_TAG, message);
                }
                progressBar.setVisibility(View.INVISIBLE);

                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }

                contentNoConnection.setVisibility(View.VISIBLE);

                if (cachedAdapter == null)
                    return;

                new Handler().postDelayed(() -> {
                    newsAdapter = cachedAdapter;
                    setUpNewsItemClickListener();
                    recyclerView.setAdapter(newsAdapter);

                }, 3000);
            }
        });

        newsData.fetchNewsFromOnlineAsync();
//        newsData.fetchNewsFromLocalStore();
    }

    private void setUpNewsItemClickListener() {
        if (newsAdapter == null) {
            Log.e(DEBUG_TAG, "News adapter is null, unable to set listeners");
            return;
        }

        newsAdapter.setOnNewsItemClickListener((position, newsItem, newsImageView, headline) -> {
            Intent intent = new Intent(getContext(), NewsDetailActivity.class);
            intent.putExtra(NEWS_ITEM_EXTRA, newsItem);
            intent.putExtra(IMAGE_TRANSITION_NAME_EXTRA, ViewCompat.getTransitionName(newsImageView));

            // ActivityOptions activityOptions;

            //view transition pairs
            Pair<View, String> imageViewPair = Pair.create(newsImageView, newsImageView.getTransitionName());


            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(getActivity(),
                        imageViewPair);
                startActivity(intent/*, options.toBundle()*/);

            } else {
                startActivity(intent);
            }
        });
    }
}