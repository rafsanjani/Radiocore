package com.foreverrafs.radiocore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.NetworkError;
import com.android.volley.VolleyError;
import com.foreverrafs.radiocore.R;
import com.foreverrafs.radiocore.activity.NewsDetailActivity;
import com.foreverrafs.radiocore.adapter.NewsAdapter;
import com.foreverrafs.radiocore.data.NewsData;
import com.foreverrafs.radiocore.model.News;
import com.foreverrafs.radiocore.util.Constants;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pl.droidsonroids.gif.GifImageView;

import static com.foreverrafs.radiocore.util.Constants.DEBUG_TAG;


// Created by Emperor95 on 1/13/2019.

public class NewsFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    @BindView(R.id.progress_loading)
    GifImageView progressBar;

    @BindView(R.id.recycler_newslist)
    RecyclerView recyclerView;

    @BindView(R.id.swipe_refresh_news)
    SwipeRefreshLayout swipeRefreshLayout;

    @BindView(R.id.content_no_network)
    View contentNoConnection;

    private NewsAdapter mNewsAdapter, mNewsAdapterCached;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_news, container, false);

        ButterKnife.bind(this, view);

        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        mNewsAdapter = new NewsAdapter();
        recyclerView.setAdapter(mNewsAdapter);

        getNewsData();

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @SuppressWarnings("WeakerAccess")
    @OnClick(R.id.button_retry)
    public void onRetryClicked() {
        contentNoConnection.setVisibility(View.INVISIBLE);
        swipeRefreshLayout.setRefreshing(true);
        progressBar.setVisibility(View.VISIBLE);
        getNewsData();
    }

    @Override
    public void onRefresh() {
        if (mNewsAdapter == null) {
            swipeRefreshLayout.setRefreshing(false);
            return;
        }

        getNewsData();
    }


    private void getNewsData() {
        if (getActivity() == null)
            return;
//
//        if (mNewsAdapter != null) {
//            if (mNewsAdapter.getItemCount() != 0)
//                mNewsAdapterCached = mNewsAdapter;
//        }

        NewsData newsData = new NewsData(getContext());

        newsData.setTaskDelegate(new NewsData.TaskDelegate() {
            @Override
            public void onAllNewsFetched(List<News> fetchedNewsItems) {
                mNewsAdapter = new NewsAdapter(fetchedNewsItems, NewsAdapter.AnimationType.BOTTOM_UP, 150);
                recyclerView.setAdapter(mNewsAdapter);

                //lets keep a copy of the adapter in case fetching goes awry on next try
                mNewsAdapterCached = mNewsAdapter;

                progressBar.setVisibility(View.GONE);

                setUpNewsItemClickListener();
            }

            @Override
            public void onNewsItemFetched(News newsItem) {
                contentNoConnection.setVisibility(View.INVISIBLE);
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
                swipeRefreshLayout.setVisibility(View.VISIBLE);

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

                if (mNewsAdapterCached == null)
                    return;

                new Handler().postDelayed(() -> {
                    mNewsAdapter = mNewsAdapterCached;
                    setUpNewsItemClickListener();
                    recyclerView.setAdapter(mNewsAdapter);

                }, 3000);
            }
        });

        newsData.fetchNews();
    }

    private void setUpNewsItemClickListener() {
        if (mNewsAdapter == null) {
            Log.e(DEBUG_TAG, "News adapter is null, unable to set listeners");
            return;
        }

        mNewsAdapter.setOnNewsItemClickListener((position) -> {
            Intent intent = new Intent(getContext(), NewsDetailActivity.class);
            intent.putExtra(Constants.KEY_SELECTED_NEWS_ITEM_POSITION, position);

            startActivity(intent);

        });
    }
}
