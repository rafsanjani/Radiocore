package com.foreverrafs.radiocore.activity;

import android.os.Bundle;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.foreverrafs.radiocore.R;
import com.foreverrafs.radiocore.fragment.NewsFragment;
import com.foreverrafs.radiocore.model.News;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.squareup.picasso.Picasso;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDetailActivity extends AppCompatActivity {
    @BindView(R.id.text_content)
    TextView textContent;

    @BindView(R.id.text_headline)
    TextView textHeadline;

    @BindView(R.id.text_date)
    TextView textDate;

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout mCollapsingToolbarLayout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);

        setSupportActionBar(mToolbar);
        mCollapsingToolbarLayout.setTitleEnabled(false);
        mToolbar.setTitle("RadioCore News");

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        if (getIntent() != null) {
            News newsItem = getIntent().getParcelableExtra(NewsFragment.NEWS_ITEM_EXTRA);

            DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM d, yyyy");
            String datePretty = newsItem.getDate().toString(fmt);

            textHeadline.setText(Html.fromHtml(newsItem.getHeadline()));
            textDate.setText(datePretty);
            textContent.setText(Html.fromHtml(newsItem.getContent()));
            Picasso.get().load(newsItem.getImage()).into(imageView);
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