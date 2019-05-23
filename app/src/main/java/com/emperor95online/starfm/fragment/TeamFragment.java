package com.emperor95online.starfm.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.emperor95online.starfm.R;
import com.emperor95online.starfm.adapter.TeamAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;


// Created by Emperor95 on 1/13/2019.

public class TeamFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    private ArrayList<com.emperor95online.starfm.model.Team> members;
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
//        addData();

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
//                            .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                            .replace(R.id.content, new HomeFragment())
                            .commit();
                }
            }
        });
    }

//    void addData(){
//        members.add(new com.emperor95online.ashhfm.model.Team("Afia Tiwaa", "NewsFragment Caster", R.drawable.afia_tiwaa_news_caster));
//        members.add(new com.emperor95online.ashhfm.model.Team("Akakpo Agogi", "Sports Producer", R.drawable.akakpo_agogi_sports_producer));
//        members.add(new com.emperor95online.ashhfm.model.Team("Alex Owusu", "Producer", R.drawable.alex));
//        members.add(new com.emperor95online.ashhfm.model.Team("Armstrong Esaah", "General Manager", R.drawable.armstrong));
//        members.add(new com.emperor95online.ashhfm.model.Team("Charles Welbeck", "Accountant", R.drawable.charlse_welbeck_accountant));
//        members.add(new com.emperor95online.ashhfm.model.Team("Daakye Hene Adusi Poku", "NewsFragment Caster", R.drawable.daakye_hene));
//        members.add(new com.emperor95online.ashhfm.model.Team("Daasebre Adjei Dwamena", "NewsFragment Editor", R.drawable.daasebre));
//        members.add(new com.emperor95online.ashhfm.model.Team("Dannis Osei Hayes", "Marketing Manager", R.drawable.dannis));
//        members.add(new com.emperor95online.ashhfm.model.Team("David Aban", "DJ BAM", R.drawable.david_aban));
//        members.add(new com.emperor95online.ashhfm.model.Team("Dwomoh Thomas", "Head of Technical", R.drawable.thomas));
//        members.add(new com.emperor95online.ashhfm.model.Team("Edgar Appiah Davis", "Foreign Sports", R.drawable.edgar));
//        members.add(new com.emperor95online.ashhfm.model.Team("Emelia Nana Abakomah Mensah", "Marketing Executive", R.drawable.emelia));
//        members.add(new com.emperor95online.ashhfm.model.Team("Elder Kingsley Owusu", "Elder Kingsley", R.drawable.kingsley));
//        members.add(new com.emperor95online.ashhfm.model.Team("Eric Agyemang Dua", "Head of Sports", R.drawable.eric_agyemang));
//        members.add(new com.emperor95online.ashhfm.model.Team("Frank Owusu Addo", "Sports Presenter", R.drawable.frank));
//        members.add(new com.emperor95online.ashhfm.model.Team("George Mensah Frans", "Sports Presenter", R.drawable.george));
//        members.add(new com.emperor95online.ashhfm.model.Team("Hawa Alhassan", "Assist Secretary", R.drawable.hawa));
//        members.add(new com.emperor95online.ashhfm.model.Team("Hayford Sarpong", "Producer", R.drawable.hayford));
//        members.add(new com.emperor95online.ashhfm.model.Team("Joe Osei Bonsu", "Foreign Sports Presenter", R.drawable.joe));
//        members.add(new com.emperor95online.ashhfm.model.Team("John Quaye", "Engineer and Chief Security", R.drawable.john_quaye));
//        members.add(new com.emperor95online.ashhfm.model.Team("Kamil Umar", "Hr-Head of Production", R.drawable.kamil));
//        members.add(new com.emperor95online.ashhfm.model.Team("Kofi Addae Munumkum", "NewsFragment Caster", R.drawable.kofi_addae));
//        members.add(new com.emperor95online.ashhfm.model.Team("Kofi Agyei", "Events Manager", R.drawable.kofi_agyei));
//        members.add(new com.emperor95online.ashhfm.model.Team("Lord Inusah", "Programmes Manager", R.drawable.inusah));
//        members.add(new com.emperor95online.ashhfm.model.Team("Mama Thess", "Aware mu Nsem", R.drawable.mama));
//        members.add(new com.emperor95online.ashhfm.model.Team("Martha Darko", "Secretary", R.drawable.martha));
//        members.add(new com.emperor95online.ashhfm.model.Team("Maxwell Gyimah", "DPT Marketing Manager", R.drawable.maxwell));
//        members.add(new com.emperor95online.ashhfm.model.Team("Nana Amoako", "De-Black Child Presenter", R.drawable.amoako));
//        members.add(new com.emperor95online.ashhfm.model.Team("Nhyiraba Akwasi Kay", "Morning Show Host", R.drawable.nhyiraba));
//        members.add(new com.emperor95online.ashhfm.model.Team("Nii Okorley Nunoo", "Driver - Electrician", R.drawable.nii));
//        members.add(new com.emperor95online.ashhfm.model.Team("Philip Appiah", "Sports Presenter", R.drawable.philip));
//        members.add(new com.emperor95online.ashhfm.model.Team("Reinolds Appiah", "DJ Boray - Presenter", R.drawable.reinolds));
//        members.add(new com.emperor95online.ashhfm.model.Team("Richard Antwi Boasiako", "Presenter - Marketer", R.drawable.richard));
//        members.add(new com.emperor95online.ashhfm.model.Team("Solomon Ofosu Ware", "Head of Operations", R.drawable.solomon));
//        members.add(new com.emperor95online.ashhfm.model.Team("Sylvester Anane", "Sly-Sports Presenter", R.drawable.sylvester));
//        members.add(new com.emperor95online.ashhfm.model.Team("Yaa Asantewaa", "NewsFragment Caster", R.drawable.yaa));
//    }

}
