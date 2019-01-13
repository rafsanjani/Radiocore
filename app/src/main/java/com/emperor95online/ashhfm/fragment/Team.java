package com.emperor95online.ashhfm.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.NewsAdapter;
import com.emperor95online.ashhfm.adapter.TeamAdapter;
import com.emperor95online.ashhfm.pojo.NewsObject;
import com.emperor95online.ashhfm.pojo.TeamObject;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class Team extends Fragment {

    private RecyclerView recyclerView;

    private ArrayList<TeamObject> members;
    private TeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        members = new ArrayList<>();
        addData();

        adapter = new TeamAdapter(getActivity(), members);
        recyclerView.setAdapter(adapter);

        return view;
    }

    void addData(){
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
        members.add(new TeamObject("John Doe", "Operations Manager", R.drawable.one));
    }

}
