package com.eagskunst.emmanuel.gamingnews.fragments.releases;


import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.adapter.ReleasesAdapter;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.di.DaggerReleasesComponent;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.di.ReleasesModule;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleasePresenter;
import com.eagskunst.emmanuel.gamingnews.fragments.releases.mvp.ReleaseView;
import com.eagskunst.emmanuel.gamingnews.models.ReleasesModel;
import com.eagskunst.emmanuel.gamingnews.utility.di.DaggerIgdbComponent;
import com.eagskunst.emmanuel.gamingnews.utility.di.IgdbModule;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class ReleasesFragment extends Fragment implements ReleaseView.View {

    private LinearLayout recyclerViewWrapper;
    private RecyclerView recyclerView;
    private LinearLayout progressWrapper;
    private ProgressBar progressBar;
    private TextView progressText;
    private FloatingActionButton fab;

    private List<ReleasesModel> releasesModelList;
    private ReleasesAdapter releasesAdapter;

    @Inject
    ReleasePresenter presenter;

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
        injectComponent();
        presenter.createView(this);
        releasesModelList = new ArrayList<>();
    }

    private void injectComponent() {
        DaggerReleasesComponent.builder()
                .igdbComponent(DaggerIgdbComponent.create())
                .releasesModule(new ReleasesModule(this))
                .build().inject(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        releasesModelList = null;
        presenter.destroyView();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_releases, container, false);

        recyclerViewWrapper = v.findViewById(R.id.recyclerview_wrapper);
        recyclerView = v.findViewById(R.id.rv_releases);
        progressWrapper = v.findViewById(R.id.progress_wrapper);
        progressBar = v.findViewById(R.id.progressBar);
        progressText = v.findViewById(R.id.progressText);
        fab = v.findViewById(R.id.releases_fab);

        releasesAdapter = new ReleasesAdapter(releasesModelList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(releasesAdapter);
        recyclerView.setHasFixedSize(true);

        getNewReleases();
        return v;
    }

    @Override
    public void showToastError(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void showToastError(int message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateList(List<ReleasesModel> releasesList) {
        releasesModelList.addAll(releasesList);
        releasesAdapter.notifyDataSetChanged();
        progressWrapper.setVisibility(View.GONE);
        recyclerViewWrapper.setVisibility(View.VISIBLE);
        Snackbar.make(getView(),"Success! Check out this hot releases", Snackbar.LENGTH_SHORT);
    }

    @Override
    public void getNewReleases() {
        recyclerViewWrapper.setVisibility(View.GONE);
        progressWrapper.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        presenter.getReleasesByPlatform();
    }

    @Override
    public void changeTextMessage(String message){
        progressText.setText(message);
    }

    @Override
    public void saveReleasesInPreferences() {

    }
}
