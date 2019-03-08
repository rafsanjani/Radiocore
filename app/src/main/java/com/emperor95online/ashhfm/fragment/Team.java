package com.emperor95online.ashhfm.fragment;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.emperor95online.ashhfm.R;
import com.emperor95online.ashhfm.adapter.TeamAdapter;
import com.emperor95online.ashhfm.pojo.TeamObject;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class Team extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private ArrayList<TeamObject> members;
    private TeamAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_team, container, false);

        LinearLayoutManager layoutManager = new GridLayoutManager(getActivity(), 2);
        layoutManager.setOrientation(RecyclerView.VERTICAL);
//
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        fab = view.findViewById(R.id.fab);

        members = new ArrayList<>();
        addData();

        adapter = new TeamAdapter(getActivity(), members);
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getActivity() != null) {
                    getActivity()
                            .getSupportFragmentManager().beginTransaction()
                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.content, new Home())
                            .commit();
                }
            }
        });
    }

    void addData(){
        members.add(new TeamObject("Afia Tiwaa", "News Caster", R.drawable.afia_tiwaa_news_caster));
        members.add(new TeamObject("Akakpo Agogi", "Sports Producer", R.drawable.akakpo_agogi_sports_producer));
        members.add(new TeamObject("Alex Owusu", "Producer", R.drawable.alex));
        members.add(new TeamObject("Armstrong Esaah", "General Manager", R.drawable.armstrong));
        members.add(new TeamObject("Charles Welbeck", "Accountant", R.drawable.charlse_welbeck_accountant));
        members.add(new TeamObject("Daakye Hene Adusi Poku", "News Caster", R.drawable.daakye_hene));
        members.add(new TeamObject("Daasebre Adjei Dwamena", "News Editor", R.drawable.daasebre));
        members.add(new TeamObject("Dannis Osei Hayes", "Marketing Manager", R.drawable.dannis));
        members.add(new TeamObject("David Aban", "DJ BAM", R.drawable.david_aban));
        members.add(new TeamObject("Dwomoh Thomas", "Head of Technical", R.drawable.thomas));
        members.add(new TeamObject("Edgar Appiah Davis", "Foreign Sports", R.drawable.edgar));
        members.add(new TeamObject("Emelia Nana Abakomah Mensah", "Marketing Executive", R.drawable.emelia));
        members.add(new TeamObject("Elder Kingsley Owusu", "Elder Kingsley", R.drawable.kingsley));
        members.add(new TeamObject("Eric Agyemang Dua", "Head of Sports", R.drawable.eric_agyemang));
        members.add(new TeamObject("Frank Owusu Addo", "Sports Presenter", R.drawable.frank));
        members.add(new TeamObject("George Mensah Frans", "Sports Presenter", R.drawable.george));
        members.add(new TeamObject("Hawa Alhassan", "Assist Secretary", R.drawable.hawa));
        members.add(new TeamObject("Hayford Sarpong", "Producer", R.drawable.hayford));
        members.add(new TeamObject("Joe Osei Bonsu", "Foreign Sports Presenter", R.drawable.joe));
        members.add(new TeamObject("John Quaye", "Engineer and Chief Security", R.drawable.john_quaye));
        members.add(new TeamObject("Kamil Umar", "Hr-Head of Production", R.drawable.kamil));
        members.add(new TeamObject("Kofi Addae Munumkum", "News Caster", R.drawable.kofi_addae));
        members.add(new TeamObject("Kofi Agyei", "Events Manager", R.drawable.kofi_agyei));
        members.add(new TeamObject("Lord Inusah", "Programmes Manager", R.drawable.inusah));
        members.add(new TeamObject("Mama Thess", "Aware mu Nsem", R.drawable.mama));
        members.add(new TeamObject("Martha Darko", "Secretary", R.drawable.martha));
        members.add(new TeamObject("Maxwell Gyimah", "DPT Marketing Manager", R.drawable.maxwell));
        members.add(new TeamObject("Nana Amoako", "De-Black Child Presenter", R.drawable.amoako));
        members.add(new TeamObject("Nhyiraba Akwasi Kay", "Morning Show Host", R.drawable.nhyiraba));
        members.add(new TeamObject("Nii Okorley Nunoo", "Driver - Electrician", R.drawable.nii));
        members.add(new TeamObject("Philip Appiah", "Sports Presenter", R.drawable.philip));
        members.add(new TeamObject("Reinolds Appiah", "DJ Boray - Presenter", R.drawable.reinolds));
        members.add(new TeamObject("Richard Antwi Boasiako", "Presenter - Marketer", R.drawable.richard));
        members.add(new TeamObject("Solomon Ofosu Ware", "Head of Operations", R.drawable.solomon));
        members.add(new TeamObject("Sylvester Anane", "Sly-Sports Presenter", R.drawable.sylvester));
        members.add(new TeamObject("Yaa Asantewaa", "News Caster", R.drawable.yaa));
    }

}
