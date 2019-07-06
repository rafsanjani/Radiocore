package com.foreverrafs.starfm.activity;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.fragment.NewsFragment;
import com.foreverrafs.starfm.model.News;
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
    Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Star FM News");
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
}