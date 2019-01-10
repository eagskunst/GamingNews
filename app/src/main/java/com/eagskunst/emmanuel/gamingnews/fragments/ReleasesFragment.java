package com.eagskunst.emmanuel.gamingnews.fragments;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.adapter.ReleasesAdapter;
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel;
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity;

import java.util.ArrayList;
import java.util.List;

public class ReleasesFragment extends Fragment {

    private RecyclerView recyclerView;
    private FloatingActionButton fab;

    public ReleasesFragment() {
        // Required empty public constructor
    }

    public static ReleasesFragment newInstance() {
        ReleasesFragment fragment = new ReleasesFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_releases, container, false);
        recyclerView = v.findViewById(R.id.rv_releases);
        fab = v.findViewById(R.id.releases_fab);
        List<ReleasesModel> releasesModelList = new ArrayList<>();
        releasesModelList.add(new ReleasesModel("https://i.blogs.es/aea019/red-dead-redemption-2/450_1000.jpg", "Red Dead Redemption 2", "26 de octubre de 2018", "Playstation 4, Xbox One"));
        ReleasesAdapter adapter = new ReleasesAdapter(releasesModelList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        recyclerView.setHasFixedSize(true);
        return v;
    }

}
