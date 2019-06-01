package com.foreverrafs.starfm;

import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.foreverrafs.starfm.model.News;
import com.squareup.picasso.Picasso;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsDetailActivity extends AppCompatActivity {
    @BindView(R.id.content)
    TextView textContent;

    @BindView(R.id.headline)
    TextView textHeadline;

    @BindView(R.id.date)
    TextView textDate;

    @BindView(R.id.image)
    ImageView imageView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    private String content, image, title, date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);


//        String[] transitions;
        News newsItem = null;
        if (getIntent() != null) {
//            content = getIntent().getExtras().getString("content", "");
//            title = getIntent().getExtras().getString("title", "");
//            date = getIntent().getExtras().getString("date", "");
//            image = getIntent().getExtras().getString("image", "");
//            transitions = getIntent().getExtras().getStringArray("transitions");
            newsItem = getIntent().getParcelableExtra("NEWS");

//            ViewCompat.setTransitionName(imageView, transitions[0]);
//            ViewCompat.setTransitionName(textHeadline, transitions[1]);
        }

        textContent.setText(Html.fromHtml(newsItem.getContent()));
        textHeadline.setText(Html.fromHtml(newsItem.getHeadline()));

        DateTimeFormatter fmt = DateTimeFormat.forPattern("MMMM d, yyyy");
        String datePretty = newsItem.getDate().toString(fmt);

        textDate.setText("Published on " + datePretty);

        Picasso.get().load(newsItem.getImage()).into(imageView);
    }
}