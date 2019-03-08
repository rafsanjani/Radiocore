package com.emperor95online.ashhfm;

import androidx.core.view.ViewCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.widget.ImageView;
import android.widget.TextView;

//import com.bumptech.glide.Glide;

import com.squareup.picasso.Picasso;

public class NewsDetail extends AppCompatActivity {

    private TextView textContent, textDate, textTitle;
    private ImageView imageView;

    private String content = "", image = "", title = "", date = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_news_detail);

        textContent = findViewById(R.id.content);
        textTitle = findViewById(R.id.headline);
        textDate = findViewById(R.id.date);
        imageView = findViewById(R.id.image);

        String[] transitions;

        if (getIntent() != null) {
            content = getIntent().getExtras().getString("content", "");
            title = getIntent().getExtras().getString("title", "");
            date = getIntent().getExtras().getString("date", "");
            image = getIntent().getExtras().getString("image", "");
            transitions = getIntent().getExtras().getStringArray("transitions");

            ViewCompat.setTransitionName(imageView, transitions[0]);
            ViewCompat.setTransitionName(textTitle, transitions[1]);
        }

        textContent.setText(Html.fromHtml(content));
        textTitle.setText(Html.fromHtml(title));
        textDate.setText("Published on " + date);

        Picasso.with(getApplicationContext())
                .load(image)
                .into(imageView);

    }
}
