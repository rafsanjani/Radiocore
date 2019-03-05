package com.emperor95online.ashhfm.adapter;

// Created by Emperor95 on 1/13/2019.

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.emperor95online.ashhfm.GlideApp;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.pojo.NewsObject;
import com.emperor95online.ashhfm.pojo.TeamObject;

import java.util.List;

public class TeamAdapter extends RecyclerView.Adapter<TeamAdapter.TeamHolder> {

    private Context context;
    private List<TeamObject> list;

    public TeamAdapter(Context context, List<TeamObject> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public TeamHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_team, viewGroup, false);

        return new TeamHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TeamHolder newsHolder, int i) {
        final TeamObject teamObject = list.get(i);

        newsHolder.name.setText(teamObject.getName());
        newsHolder.portfolio.setText(teamObject.getPorfolio());
//        newsHolder.imageView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
                GlideApp.with(context)
                        .load(teamObject.getImage())
                        .placeholder(R.drawable.one)
                        .into(newsHolder.imageView);
//            }
//        }, 2000);


    }

    ////
    class TeamHolder extends RecyclerView.ViewHolder{

        private TextView name, portfolio;
        private ImageView imageView;

        TeamHolder(View view){
            super(view);

            name = itemView.findViewById(R.id.name);
            portfolio = itemView.findViewById(R.id.portfolio);
            imageView = itemView.findViewById(R.id.image);
        }

    }

}
