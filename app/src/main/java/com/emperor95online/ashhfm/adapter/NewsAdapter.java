package com.emperor95online.ashhfm.adapter;

// Created by Emperor95 on 1/13/2019.

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emperor95online.ashhfm.NewsDetail;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.pojo.NewsObject;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsHolder> {

    private Context context;
    private List<NewsObject> list;

    public NewsAdapter(Context context, List<NewsObject> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public NewsHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_news__, viewGroup, false);

        return new NewsHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final NewsHolder newsHolder, int i) {
        NewsObject newsObject = list.get(i);

        newsHolder.headline.setText(newsObject.getHeadline());
        newsHolder.date.setText(newsObject.getDate());
//        newsHolder.imageView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                Glide.with(context)
                        .load(R.drawable.asht)
                        .into(newsHolder.imageView);
//            }
//        }, 2000);

        newsHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, NewsDetail.class));
            }
        });
    }

    ////
    public class NewsHolder extends RecyclerView.ViewHolder{

        private TextView date, headline;
        private ImageView imageView;

        public NewsHolder(View view){
            super(view);

            headline = itemView.findViewById(R.id.headline);
            date = itemView.findViewById(R.id.date);
            imageView = itemView.findViewById(R.id.image);
        }

    }

}
