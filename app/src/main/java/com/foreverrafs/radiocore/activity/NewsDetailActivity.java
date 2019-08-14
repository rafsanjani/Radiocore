package com.foreverrafs.radiocore.activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import com.foreverrafs.radiocore.R;
import com.foreverrafs.radiocore.adapter.NewsPagerAdapter;
import com.foreverrafs.radiocore.util.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDetailActivity extends AppCompatActivity {
    @BindView(R.id.view_pager)
    ViewPager mNewsViewPager;

    private int mNewsPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_item_detail_pager);

        ButterKnife.bind(this);

        getIncomingIntent();

        NewsPagerAdapter newsPagerAdapter = new NewsPagerAdapter(getSupportFragmentManager());
        mNewsViewPager.setOffscreenPageLimit(1);
        mNewsViewPager.setAdapter(newsPagerAdapter);
        mNewsViewPager.setCurrentItem(mNewsPosition);
    }


    private void getIncomingIntent() {
        if (getIntent() != null) {
            mNewsPosition = getIntent().getIntExtra(Constants.KEY_SELECTED_NEWS_ITEM_POSITION, 0);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return true;
    }

}