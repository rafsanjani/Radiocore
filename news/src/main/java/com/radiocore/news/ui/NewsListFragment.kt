package com.radiocore.news.ui

import KEY_SELECTED_NEWS_ITEM_POSITION
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radiocore.news.NewsDetailActivity
import com.radiocore.news.R
import com.radiocore.news.adapter.NewsAdapter
import com.radiocore.news.data.NewsObjects
import com.radiocore.news.databinding.FragmentNewsListBinding
import com.radiocore.news.model.News
import com.radiocore.news.state.NewsState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import timber.log.Timber

// Created by Emperor95 on 1/13/2019.
class NewsListFragment : Fragment(R.layout.fragment_news_list), SwipeRefreshLayout.OnRefreshListener {
    private val viewModel: NewsViewModel by activityViewModels()
    private lateinit var binding: FragmentNewsListBinding


    private val adapter = NewsAdapter { news, adapterPosition ->
        onNewsClicked(news, adapterPosition)
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentNewsListBinding.inflate(inflater, container, false)
        return binding.root
    }


    @ExperimentalCoroutinesApi
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        with(binding) {
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
            recyclerView.adapter = adapter

            swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
            swipeRefreshLayout.setOnRefreshListener(this@NewsListFragment)
            getAllNews()

            contentNoConnection.btnRetry.setOnClickListener {
                contentNoConnection.root.visibility = View.INVISIBLE
                loadingBar.visibility = View.VISIBLE
                getAllNews()
            }

            initializeObservers()
        }
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
        binding.loadingBar.visibility = View.VISIBLE
    }

    private fun showNewsItems(newsItems: List<News>) = with(binding) {
        swipeRefreshLayout.visibility = View.VISIBLE
        contentNoConnection.root.visibility = View.INVISIBLE

        adapter.submitList(newsItems)

        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }

        loadingBar.visibility = View.INVISIBLE

        //todo: remove this and pass as bundle
        NewsObjects.newsItems = newsItems
    }

    private fun showError(error: Throwable) = with(binding) {
        contentNoConnection.root.visibility = View.VISIBLE

        if (swipeRefreshLayout.isRefreshing) {
            swipeRefreshLayout.isRefreshing = false
        }

        loadingBar.visibility = View.INVISIBLE
        Timber.e(error)
    }

    @ExperimentalCoroutinesApi
    private fun getAllNews() {
        lifecycleScope.launch {
            try {
                viewModel.getAllNews()
                        .distinctUntilChanged()
                        .collectLatest { items ->
                            if (!items.isNullOrEmpty())
                                viewModel.setNewsState(NewsState.LoadedState(items))
                            else
                                viewModel.setNewsState(NewsState.ErrorState(IllegalArgumentException("News List is null or empty")))
                        }
            } catch (e: Exception) {
                viewModel.setNewsState(NewsState.ErrorState(e))
            }
        }
    }

    @ExperimentalCoroutinesApi
    override fun onRefresh() {
        getAllNews()
    }

    private fun onNewsClicked(newsItem: News, position: Int) {
        val intent = Intent(context, NewsDetailActivity::class.java)

        intent.putExtra(KEY_SELECTED_NEWS_ITEM_POSITION, position)

        startActivity(intent)
    }
}
