package com.foreverrafs.radiocore.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.android.volley.NetworkError
import com.android.volley.VolleyError
import com.foreverrafs.radiocore.R
import com.foreverrafs.radiocore.activity.NewsDetailActivity
import com.foreverrafs.radiocore.adapter.AnimationAdapter
import com.foreverrafs.radiocore.adapter.NewsAdapter
import com.foreverrafs.radiocore.adapter.NewsAdapter.NewsItemClickListener
import com.foreverrafs.radiocore.data.NewsData
import com.foreverrafs.radiocore.model.News
import com.foreverrafs.radiocore.util.Constants
import kotlinx.android.synthetic.main.content_no_connection.*
import kotlinx.android.synthetic.main.fragment_news.*


// Created by Emperor95 on 1/13/2019.

class NewsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private val TAG = "NewsFragment"
    private var mNewsAdapter: NewsAdapter? = null
    private var mNewsAdapterCached: NewsAdapter? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_news, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.colorAccent, R.color.colorPrimaryDark)
        swipeRefreshLayout.setOnRefreshListener(this)

        mNewsAdapter = NewsAdapter()
        recyclerView.adapter = mNewsAdapter

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

    override fun onRefresh() {
        if (mNewsAdapter == null) {
            swipeRefreshLayout!!.isRefreshing = false
            return
        }

        getNewsData()
    }


    private fun getNewsData() {
        if (activity == null)
            return

        val newsData = NewsData(context!!)

        newsData.setTaskDelegate(object : NewsData.TaskDelegate {
            override fun onAllNewsFetched(newsItems: List<News>?) {
                val fetchedNewsItems: List<News>? = newsItems
                contentNoConnection.visibility = View.INVISIBLE
                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }
                swipeRefreshLayout.visibility = View.VISIBLE
                mNewsAdapter = NewsAdapter(fetchedNewsItems, AnimationAdapter.AnimationType.BOTTOM_UP, 150)
                recyclerView.adapter = mNewsAdapter

                //lets keep a copy of the adapter in case fetching goes awry on next try
                mNewsAdapterCached = mNewsAdapter

                progressBar!!.visibility = View.GONE

                setUpNewsItemClickListener()
            }

            override fun onError(error: VolleyError) {
                Log.e(TAG, error.toString())
                if (error is NetworkError) {
                    val message = "Network Error::Are you online?"
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    Log.i(TAG, message)
                }
                progressBar.visibility = View.INVISIBLE

                if (swipeRefreshLayout.isRefreshing) {
                    swipeRefreshLayout.isRefreshing = false
                }

                contentNoConnection.visibility = View.VISIBLE

                if (mNewsAdapterCached == null)
                    return

                Handler().postDelayed({
                    mNewsAdapter = mNewsAdapterCached
                    setUpNewsItemClickListener()
                    recyclerView.adapter = mNewsAdapter

                }, 3000)
            }
        })

        newsData.fetchNews()
    }

    private fun setUpNewsItemClickListener() {
        if (mNewsAdapter == null) {
            Log.e(TAG, "News adapter is null, unable to set listeners")
            return
        }

        mNewsAdapter!!.setOnNewsItemClickListener(object : NewsItemClickListener {
            override fun onNewsItemClicked(position: Int) {
                val intent = Intent(context, NewsDetailActivity::class.java)
                intent.putExtra(Constants.KEY_SELECTED_NEWS_ITEM_POSITION, position)

                startActivity(intent)
            }
        })
    }

}
