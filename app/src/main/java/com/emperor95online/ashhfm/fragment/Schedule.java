package com.emperor95online.ashhfm.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.ScheduleAdapter;
import com.emperor95online.ashhfm.pojo.ScheduleObject;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class Schedule extends Fragment {

    private ArrayList<ScheduleObject> schedules;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule, container, false);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        schedules = new ArrayList<>();
        addData();

        ScheduleAdapter scheduleAdapter = new ScheduleAdapter(getActivity(), schedules);
        recyclerView.setAdapter(scheduleAdapter);

        return view;
    }

    void addData() {
        schedules.add(new ScheduleObject("We shall Have peace", "13th January, 2019", "4hrs 3mins"));
        schedules.add(new ScheduleObject("We shal make the world a better place", "13th January, 2019", "3hrs 2mins"));
    }
}
