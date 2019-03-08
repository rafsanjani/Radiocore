package com.emperor95online.ashhfm.adapter;

// Created by Emperor95 on 1/13/2019.

import android.content.Context;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.emperor95online.ashhfm.NewsDetail;
import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.pojo.ScheduleObject;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ScheduleViewHolder> {

    private Context context;
    private List<ScheduleObject> list;

    public ScheduleAdapter(Context context, List<ScheduleObject> list){
        this.context = context;
        this.list = list;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @NonNull
    @Override
    public ScheduleViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_schedule__, viewGroup, false);

        return new ScheduleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ScheduleViewHolder scheduleHolder, int i) {
        ScheduleObject scheduleObject = list.get(i);

        scheduleHolder.headline.setText(scheduleObject.getHeadline());
        scheduleHolder.date.setText(scheduleObject.getDate());
        scheduleHolder.remainingTime.setText(scheduleObject.getRemainingTime());
//        newsHolder.imageView.postDelayed(new Runnable() {
//            @Override
//            public void run() {
  //              Glide.with(context)
    //                    .load(R.drawable.asht)
      //                  .into(ScheduleViewHolder.imageView);
//            }
//        }, 2000);

        scheduleHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.startActivity(new Intent(context, NewsDetail.class));
            }
        });
    }

    ////
    class ScheduleViewHolder extends RecyclerView.ViewHolder{

        private TextView headline, date, remainingTime;


        ScheduleViewHolder(View view){
            super(view);

            headline = itemView.findViewById(R.id.headline);
            date = itemView.findViewById(R.id.date);
            remainingTime = itemView.findViewById(R.id.remaining_time);
        }

    }

}
