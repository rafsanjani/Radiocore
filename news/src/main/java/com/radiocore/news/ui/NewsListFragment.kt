package com.radiocore.news.ui

import android.app.ActivityOptions
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.radiocore.core.util.Constants
import com.radiocore.news.NewsDetailActivity
import com.radiocore.news.R
import com.radiocore.news.adapter.AnimationAdapter
import com.radiocore.news.adapter.NewsAdapter
import com.radiocore.news.adapter.NewsAdapter.NewsItemClickListener
import com.radiocore.stickyheaders.StickyHeadersLinearLayoutManager
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.content_no_connection.*
import kotlinx.android.synthetic.main.fragment_news_list.*


// Created by Emperor95 on 1/13/2019.
class NewsListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()


    private val viewModel: NewsViewModel by lazy {
        ViewModelProviders.of(this)[NewsViewModel::class.java]
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        swipeRefreshLayout.setOnRefreshListener(this)
        getNewsData()

        btnRetry.setOnClickListener {
            run {
                contentNoConnection.visibility = View.INVISIBLE
                loadingBar.visibility = View.VISIBLE
                getNewsData()
            }
        }

        textView.setOnClickListener {
            println("hi")
        }
    }

    private fun getNewsData() {
        val observer: Observer<List<com.radiocore.news.model.News>> = Observer { list ->
            if (!list.isNullOrEmpty()) {
                swipeRefreshLayout.visibility = View.VISIBLE
                contentNoConnection.visibility = View.INVISIBLE

                val x = com.radiocore.news.model.SectionedNews(list)

                val adapter = NewsAdapter(x, AnimationAdapter.AnimationType.BOTTOM_UP, 150)
                recyclerView.layoutManager = StickyHeadersLinearLayoutManager<NewsAdapter>(requireContext())
                recyclerView?.adapter = adapter

                setUpNewsItemClickListener(adapter)

            } else {
                contentNoConnection.visibility = View.VISIBLE
            }

            if (swipeRefreshLayout.isRefreshing) {
                swipeRefreshLayout.isRefreshing = false
            }

            loadingBar.visibility = View.INVISIBLE
        }

        viewModel.getAllNews().observe(this, observer)
    }


    private fun setUpNewsItemClickListener(adapter: NewsAdapter) {
        adapter.setOnNewsItemClickListener(object : NewsItemClickListener {
            override fun onNewsItemClicked(position: Int, image: ImageView) {
                val intent = Intent(context, NewsDetailActivity::class.java)
                intent.putExtra(Constants.KEY_SELECTED_NEWS_ITEM_POSITION, position)
                val options = ActivityOptions.makeSceneTransitionAnimation(activity, image, image.transitionName)

                startActivity(intent/*, options.toBundle()*/)
            }
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        mCompositeDisposable.clear()
    }

    override fun onRefresh() {
        getNewsData()
    }

}
