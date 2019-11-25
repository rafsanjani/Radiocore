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
import com.foreverrafs.radiocore.util.Constants
import com.foreverrafs.radiocore.viewmodels.NewsViewModel
import io.reactivex.disposables.CompositeDisposable
import kotlinx.android.synthetic.main.content_no_connection.*
import kotlinx.android.synthetic.main.fragment_news.*


// Created by Emperor95 on 1/13/2019.
class NewsListFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
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

    private fun getNewsData() {
        viewModel.getAllNews().observe(this,
                Observer { newsList ->
                    if (!newsList.isNullOrEmpty()) {

                        swipeRefreshLayout.visibility = View.VISIBLE
                        contentNoConnection.visibility = View.INVISIBLE

                        val adapter = NewsAdapter(newsList, AnimationAdapter.AnimationType.BOTTOM_UP, 150)
                        recyclerView.adapter = adapter
                        setUpNewsItemClickListener(adapter)

                    } else {
                        contentNoConnection.visibility = View.VISIBLE
                    }

                    //these views will be hidden either ways
                    if (swipeRefreshLayout.isRefreshing) {
                        swipeRefreshLayout.isRefreshing = false
                    }

                    progressBar.visibility = View.INVISIBLE
                })
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
