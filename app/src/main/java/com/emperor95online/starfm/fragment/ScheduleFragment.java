package com.emperor95online.starfm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emperor95online.starfm.R;
import com.emperor95online.starfm.adapter.ScheduleAdapter;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class ScheduleFragment extends Fragment {

    private ArrayList<com.emperor95online.starfm.model.Schedule> schedules;

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
        schedules.add(new com.emperor95online.starfm.model.Schedule("We shall Have peace", "13th January, 2019", "4hrs 3mins"));
        schedules.add(new com.emperor95online.starfm.model.Schedule("We shal make the world a better place", "13th January, 2019", "3hrs 2mins"));
    }
}
