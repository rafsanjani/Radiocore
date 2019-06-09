package com.foreverrafs.starfm.adapter;

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.foreverrafs.starfm.R;
import com.foreverrafs.starfm.model.News;
import com.foreverrafs.starfm.util.ItemAnimation;
import com.squareup.picasso.Picasso;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    private List<News> newsList;
    private NewsItemClickListener listener;
    private int lastPosition = -1;
    private boolean on_attach = true;

    public NewsAdapter(List<News> list) {
        this.newsList = list;
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_news__, viewGroup, false);

        return new NewsHolder(view);
    }

    public void setOnNewsItemClickListener(NewsItemClickListener listener) {
        this.listener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(@NonNull final NewsHolder newsHolder, final int position) {
        final News newsItem = newsList.get(position);

        DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM d, yyyy");
        newsHolder.date.setText(newsItem.getDate().toString(formatter));

        newsHolder.headline.setText(newsItem.getHeadline());

        Picasso.get().load(newsItem.getImage()).into(newsHolder.imageView);

        ViewCompat.setTransitionName(newsHolder.imageView, newsItem.getImage());
        ViewCompat.setTransitionName(newsHolder.headline, newsItem.getHeadline());

        newsHolder.itemView.setOnClickListener(v -> listener.onNewsItemClicked(newsHolder.getAdapterPosition(), newsItem, newsHolder.imageView, newsHolder.headline));

        setAnimation(newsHolder.itemView, position);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                on_attach = false;
                super.onScrollStateChanged(recyclerView, newState);
            }
        });
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Set's an animation to be used with the adapter during view {@link #onBindViewHolder(NewsHolder, int)}.
     *
     * @param view     The itemView of the recyclerView to be animated
     * @param position The position of the itemView
     */
    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            ItemAnimation.animate(view, on_attach ? position : -1, ItemAnimation.LEFT_RIGHT);
            lastPosition = position;
        }
    }

    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    public interface NewsItemClickListener {
        void onNewsItemClicked(int position, News newsItem, ImageView newsImage, TextView headline);
    }

    @SuppressWarnings("WeakerAccess")
    public class NewsHolder extends RecyclerView.ViewHolder {

        private TextView date, headline;
        private ImageView imageView;

        NewsHolder(View view) {
            super(view);

            headline = itemView.findViewById(R.id.headline);
            date = itemView.findViewById(R.id.date);
            imageView = itemView.findViewById(R.id.image);
        }
    }
}
