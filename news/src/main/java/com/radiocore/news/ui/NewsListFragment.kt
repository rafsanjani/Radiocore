package com.radiocore.news.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radiocore.core.util.KEY_SELECTED_NEWS_ITEM_POSITION
import com.radiocore.news.NewsDetailActivity
import com.radiocore.news.R
import com.radiocore.news.adapter.NewsAdapter
import com.radiocore.news.adapter.NewsAdapter.NewsItemClickListener
import com.radiocore.news.model.News
import com.radiocore.news.util.NewsState
import dagger.hilt.android.AndroidEntryPoint
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.content_no_connection.*
import kotlinx.android.synthetic.main.fragment_news_list.*
import timber.log.Timber

// Created by Emperor95 on 1/13/2019.
class NewsListFragment : Fragment(R.layout.fragment_news_list), SwipeRefreshLayout.OnRefreshListener, NewsItemClickListener {
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()

    private val viewModel: NewsViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        swipeRefreshLayout.setOnRefreshListener(this)
        getNewsData()

        btnRetry.setOnClickListener {
            contentNoConnection.visibility = View.INVISIBLE
            loadingBar.visibility = View.VISIBLE
            getNewsData()
        }

        initializeObservers()
    }

    private fun initializeObservers() {
        viewModel.newsState.observe(viewLifecycleOwner, Observer { state ->
            when (state) {
                NewsState.LoadingState -> showLoadingScreen()
                is NewsState.ErrorState -> showError(state.error)
                is NewsState.LoadedState -> showNewsItems(state.news)
            }
        })
    }

    private fun showLoadingScreen() {
        loadingBar.visibility = View.VISIBLE
    }

    private fun showNewsItems(newsList: List<News>) {
        swipeRefreshLayout.visibility = View.VISIBLE
        contentNoConnection.visibility = View.INVISIBLE

        val adapter = NewsAdapter(newsList, this)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView?.adapter = adapter

        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }

        loadingBar.visibility = View.INVISIBLE
    }

    private fun showError(error: Throwable) {
        contentNoConnection.visibility = View.VISIBLE
        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }

        loadingBar.visibility = View.INVISIBLE
        Timber.e(error)
    }

    private fun getNewsData() {
        val observer: Observer<List<News>> = Observer { list ->
            if (!list.isNullOrEmpty())
                viewModel.setNewsState(NewsState.LoadedState(list))
            else
                viewModel.setNewsState(NewsState.ErrorState(IllegalArgumentException("News List is null or empty")))
        }

        viewModel.setNewsState(NewsState.LoadingState)

        try {
            viewModel.getAllNews().observe(viewLifecycleOwner, observer)
        } catch (e: Exception) {
            viewModel.setNewsState(NewsState.ErrorState(e))
        }
    }

    override fun onDestroy() {
        mCompositeDisposable.clear()
        super.onDestroy()
    }

    override fun onRefresh() {
        getNewsData()
    }

    override fun onNewsItemClicked(position: Int, image: ImageView) {
        val intent = Intent(context, NewsDetailActivity::class.java)
        intent.putExtra(KEY_SELECTED_NEWS_ITEM_POSITION, position)
//        val options = ActivityOptions.makeSceneTransitionAnimation(activity, image, image.transitionName)

        startActivity(intent/*, options.toBundle()*/)
    }
}
