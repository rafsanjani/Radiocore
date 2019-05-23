package com.emperor95online.starfm.fragment;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
import com.emperor95online.starfm.NewsDetailActivity;
import com.emperor95online.starfm.R;
import com.emperor95online.starfm.adapter.NewsAdapter;
import com.emperor95online.starfm.data.NewsData;
import com.emperor95online.starfm.model.News;
import com.emperor95online.starfm.util.Constants;

import java.util.ArrayList;
import java.util.List;

import jp.wasabeef.recyclerview.adapters.ScaleInAnimationAdapter;


// Created by Emperor95 on 1/13/2019.

public class HomeFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ImageButton more_main, imBtn;

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

        imBtn = view.findViewById(R.id.imBtn);
        more_main = view.findViewById(R.id.more_main);
        more_main.setOnClickListener(this);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        swipeRefreshLayout = view.findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark);
        swipeRefreshLayout.setOnRefreshListener(this);

        recyclerView = view.findViewById(R.id.recyclerView);

        recyclerView.setLayoutManager(layoutManager);

        progressBar = view.findViewById(R.id.progressBar2);
        newsAdapter = new NewsAdapter(getActivity(), newsList);
        ScaleInAnimationAdapter adapter = new ScaleInAnimationAdapter(newsAdapter);
        adapter.setDuration(500);
        recyclerView.setAdapter(adapter);


        getNewsData();

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

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.more_main:
//                showPopupMenu(imBtn);
                break;
        }
    }

    @Override
    public void onRefresh() {
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
                newsList.addAll(fetchedNewsItems);
                newsAdapter.notifyDataSetChanged();
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

//    /**
//     * Inflate popup menu on the banner at the homescreen
//     *
//     * @param view
//     */
//    private void showPopupMenu(final View view) {
//        PopupMenu popupMenu = new PopupMenu(getActivity(), view);
//    MenuInflater inflater = popupMenu.getMenuInflater();
//        inflater.inflate(R.menu.main_popup, popupMenu.getMenu());
//        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
//        @Override
//        public boolean onMenuItemClick(MenuItem item) {
//
//            switch (item.getItemId()) {
//                case R.id.staff:
//                    if (getActivity() != null) {
//                        getActivity()
//                                .getSupportFragmentManager().beginTransaction()
////                                    .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
//                                .replace(R.id.content, new TeamFragment())
//                                .commit();
//                    }
//                    return true;
//                case R.id.about_station:
//                    return true;
//                case R.id.privacy_policy:
//                    return true;
//            }
//
//            return false;
//        }
//    });
//        popupMenu.show();
//}

}