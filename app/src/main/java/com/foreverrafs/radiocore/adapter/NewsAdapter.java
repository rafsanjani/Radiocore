package com.foreverrafs.radiocore.adapter;

// Created by Emperor95 on 1/13/2019.

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.foreverrafs.radiocore.R;
import com.foreverrafs.radiocore.model.News;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NewsAdapter extends AnimationAdapter {

    private List<News> newsList;
    private NewsItemClickListener listener;

    public NewsAdapter(List<News> list, AnimationType type, int duration) {
        super(type, duration);
        this.newsList = list;
    }

    public NewsAdapter() {
        super(AnimationType.LEFT_RIGHT, 200);
        this.newsList = new ArrayList<>();
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
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder newsViewHolder, final int position) {
        final News newsItem = newsList.get(position);
        ((NewsHolder) newsViewHolder).bind(newsItem);
    }


    /**
     * Propagate click events to the RecyclervView to which this adapter is attached.
     */
    public interface NewsItemClickListener {
        void onNewsItemClicked(int position);
    }


    class NewsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.text_date)
        TextView tvDate;

        @BindView(R.id.text_category)
        TextView tvCategory;

        @BindView(R.id.text_headline)
        TextView tvHeadline;

        @BindView(R.id.image)
        ImageView image;

        NewsHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

        public void bind(News newsItem) {
            DateTimeFormatter formatter = DateTimeFormat.forPattern("MMMM d, yyyy");

            tvDate.setText(newsItem.getDate().toString(formatter));
            tvHeadline.setText(newsItem.getHeadline());
            tvCategory.setText(newsItem.getCategory());

            Glide.with(itemView)
                    .load(newsItem.getImage())
                    .error(R.drawable.newsimage)
                    .placeholder(R.drawable.newsimage)
                    .centerCrop()
                    .into(image);

            image.setTransitionName(newsItem.getImage());

            itemView.setOnClickListener(v -> listener.onNewsItemClicked(getAdapterPosition()));

            setAnimation(itemView, getAdapterPosition());
        }
    }
}
