package com.emperor95online.starfm.adapter;

// Created by Emperor95 on 1/13/2019.

import android.content.Context;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.emperor95online.starfm.R;
import com.emperor95online.starfm.model.News;
import com.emperor95online.starfm.util.ItemAnimation;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    private Context context;
    private List<News> newsList;
    private NewsItemClickListener listener;

    public NewsAdapter(Context context, List<News> list) {
        this.context = context;
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

//    @Override
//    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
//        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
//                on_attach = false;
//                super.onScrollStateChanged(recyclerView, newState);
//            }
//        });
//        super.onAttachedToRecyclerView(recyclerView);
//    }

    private int lastPosition = -1;
    private boolean on_attach = true;

    @Override
    public void onBindViewHolder(@NonNull final NewsHolder newsHolder, final int position) {
        final News newsObject = newsList.get(position);

        String string = newsObject.getDate();
        DateFormat format = new SimpleDateFormat("yyyy-MM-d", Locale.ENGLISH);
        Date date = null;


        try {
            date = format.parse(string);
        } catch (ParseException e) {
        }

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.ENGLISH);
        newsHolder.date.setText(simpleDateFormat.format(date));
        newsHolder.headline.setText(newsObject.getHeadline());

        Picasso.with(context)
                .load(newsObject.getImage())
                .into(newsHolder.imageView);

        //todo: replace with an MD5 hash
        Long tsLong = System.currentTimeMillis() / 1000;
        final String imageTransitionName = tsLong.toString();

        ViewCompat.setTransitionName(newsHolder.imageView, imageTransitionName + "_image");

        tsLong = System.currentTimeMillis() / 1000;
        final String headlineTransitionName = tsLong.toString();

        ViewCompat.setTransitionName(newsHolder.headline, headlineTransitionName + "_headline");

        setAnimation(newsHolder.itemView, position);
    }

    private void setAnimation(View view, int position) {
        if (position > lastPosition) {
            int animation_type = ItemAnimation.FADE_IN;
            ItemAnimation.animate(view, on_attach ? position : -1, animation_type);
            lastPosition = position;
        }
    }

    public void setOnNewsItemClickListener(NewsItemClickListener listener) {
        this.listener = listener;
    }

    ////


    public interface NewsItemClickListener {
        void onNewItemClicked(News newsObject, Pair[] pairs, int position);
    }

    public class NewsHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView date, headline;
        private ImageView imageView;

        NewsHolder(View view) {
            super(view);
            view.setOnClickListener(this);
            headline = itemView.findViewById(R.id.headline);
            date = itemView.findViewById(R.id.date);
            imageView = itemView.findViewById(R.id.image);
        }

        public TextView getHeadlineTextView() {
            return headline;
        }

        public ImageView getImageImageView() {
            return imageView;
        }

        @Override
        public void onClick(View v) {
            int position = getAdapterPosition();
            Pair[] pair = new Pair[]{
                    new Pair(imageView, ViewCompat.getTransitionName(imageView)),
                    new Pair(headline, ViewCompat.getTransitionName(headline))
            };
            listener.onNewItemClicked(newsList.get(position), pair, position);
        }
    }
}
