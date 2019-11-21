package com.foreverrafs.radiocore.fragment

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
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.activity.NewsDetailActivity
import com.foreverrafs.radiocore.adapter.AnimationAdapter
import com.foreverrafs.radiocore.adapter.NewsAdapter
import com.foreverrafs.radiocore.adapter.NewsAdapter.NewsItemClickListener
import com.foreverrafs.radiocore.data.NewsRepository
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.viewmodels.NewsViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.content_no_connection.*
import kotlinx.android.synthetic.main.fragment_news.*


// Created by Emperor95 on 1/13/2019.
class NewsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()


    private val viewModel: NewsViewModel by lazy {
        ViewModelProviders.of(this)[NewsViewModel::class.java]
    }


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        swipeRefreshLayout.setOnRefreshListener(this)

//        mNewsAdapter = NewsAdapter()
//        recyclerView.adapter = mNewsAdapter

        getNewsData()

        buttonRetry.setOnClickListener {
            run {
                contentNoConnection.visibility = View.INVISIBLE
                swipeRefreshLayout.isRefreshing = true
                progressBar.visibility = View.VISIBLE
                getNewsData()
            }
        }
    }

//    override fun onRefresh() {
//        if (mNewsAdapter == null) {
//            swipeRefreshLayout!!.isRefreshing = false
//            return
//        }
//
//        getNewsData()
//    }

    private fun getNewsData() {
        viewModel.getAllNews().observe(this,
                Observer { newsList ->
                    //keep this inside our repository
                    NewsRepository.getInstance().radioCoreNews = newsList


                    if (!newsList.isNullOrEmpty()) {
                        swipeRefreshLayout.visibility = View.VISIBLE
                        contentNoConnection.visibility = View.INVISIBLE
                        progressBar.visibility = View.INVISIBLE

                        val adapter = NewsAdapter(newsList, AnimationAdapter.AnimationType.BOTTOM_UP, 150)
                        recyclerView.adapter = adapter
                        setUpNewsItemClickListener(adapter)

                    } else {
                        progressBar!!.visibility = View.GONE
                    }

                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }
                })
    }

//    private fun getNewsData() {
//        if (activity == null)
//            return
//
//        val newsRepository = NewsService(context!!)
//
//        newsRepository.fetchNews()
//                .subscribe(object : SimpleObserver<List<News>>() {
//                    override fun onSubscribe(d: Disposable) {
//                        mCompositeDisposable.add(d)
//                    }
//
//                    override fun onNext(newsItems: List<News>) {
//                        //keep a copy in our news repository for use by viewpaging fragments
//                        newsRepository.saveNewsItems(newsItems)
//
//                        Log.i(TAG, "${newsItems.size} news items fetched")
//                        contentNoConnection.visibility = View.INVISIBLE
//                        if (swipeRefreshLayout.isRefreshing) {
//                            swipeRefreshLayout.isRefreshing = false
//                        }
//                        swipeRefreshLayout.visibility = View.VISIBLE
//                        mNewsAdapter = NewsAdapter(newsItems, AnimationAdapter.AnimationType.BOTTOM_UP, 150)
//                        recyclerView.adapter = mNewsAdapter
//
//                        //lets keep a copy of the adapter in case fetching goes awry on next try
//                        mNewsAdapterCached = mNewsAdapter
//
//                        progressBar!!.visibility = View.GONE
//
//                        setUpNewsItemClickListener()
//                    }
//
//                    override fun onError(e: Throwable) {
//                        progressBar.visibility = View.INVISIBLE
//
//                        if (swipeRefreshLayout.isRefreshing) {
//                            swipeRefreshLayout.isRefreshing = false
//                        }
//
//                        contentNoConnection.visibility = View.VISIBLE
//
//                        if (mNewsAdapterCached == null)
//                            return
//
//                        Handler().postDelayed({
//                            mNewsAdapter = mNewsAdapterCached
//                            setUpNewsItemClickListener()
//                            recyclerView.adapter = mNewsAdapter
//
//                        }, 3000)
//                    }
//                })
//    }

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
