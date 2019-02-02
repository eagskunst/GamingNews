package com.eagskunst.emmanuel.gamingnews.fragments.releases;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.design.widget.CoordinatorLayout;
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
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity;
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader;
import com.eagskunst.emmanuel.gamingnews.utility.di.DaggerIgdbComponent;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.inject.Inject;
public class ReleasesFragment extends Fragment implements ReleaseView.View, ReleasesAdapter.OnReleaseClickListener {

    private CoordinatorLayout rootLayout;
    private LinearLayout recyclerViewWrapper;
    private RecyclerView recyclerView;
    private LinearLayout progressWrapper;
    private ProgressBar progressBar;
    private TextView progressText;
    private FloatingActionButton fab;

    private List<ReleasesModel> releasesModelList;
    private ReleasesAdapter releasesAdapter;

    private boolean tryAgain = false;

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
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =inflater.inflate(R.layout.fragment_releases, container, false);

        rootLayout = v.findViewById(R.id.root_releases);
        recyclerViewWrapper = v.findViewById(R.id.recyclerview_wrapper);
        recyclerView = v.findViewById(R.id.rv_releases);
        progressWrapper = v.findViewById(R.id.progress_wrapper);
        progressBar = v.findViewById(R.id.progressBar);
        progressText = v.findViewById(R.id.progressText);
        fab = v.findViewById(R.id.releases_fab);

        releasesAdapter = new ReleasesAdapter(releasesModelList, this);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(releasesAdapter);
        recyclerView.setHasFixedSize(true);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy > 0 && fab.getVisibility() == View.VISIBLE){
                    fab.hide();
                }
                else if(dy < 0 && fab.getVisibility() != View.VISIBLE){
                    fab.show();
                }
                super.onScrolled(recyclerView, dx, dy);
            }
        });

        fab.setOnClickListener(view -> recyclerView.smoothScrollToPosition(0));

        fillList();
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
        if(tryAgain){
            tryAgain = false;
            presenter.getReleasesList().clear();
            getNewReleases();
        }
        else{
            releasesModelList.addAll(releasesList);
            releasesAdapter.notifyDataSetChanged();
            progressWrapper.setVisibility(View.GONE);
            recyclerViewWrapper.setVisibility(View.VISIBLE);
            Snackbar.make(rootLayout,R.string.success_releases, Snackbar.LENGTH_SHORT).show();
            saveReleasesInPreferences();
        }
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
    public void changeTextMessage(int message){
        progressText.setText(message);
    }

    @Override
    public void setTryAgain(){
        tryAgain = true;
    }

    private void fillList(){
        List<ReleasesModel> list = getReleasesFromPreferences();
        int savedMonth = SharedPreferencesLoader.getSavedMonth(((BaseActivity)getActivity()).getUserSharedPreferences());
        int actualMonth = Calendar.getInstance().get(Calendar.MONTH);
        if(list == null || savedMonth == -1 || actualMonth>savedMonth || (actualMonth == 0 && savedMonth == 11) || list.isEmpty()){
            getNewReleases();
        }
        else {
            Calendar calendar = Calendar.getInstance();
            int day = calendar.get(Calendar.DAY_OF_MONTH);
            presenter.erasePassedDate(list, day);
            releasesModelList.addAll(list);
            releasesAdapter.notifyDataSetChanged();
            saveReleasesInPreferences();
        }
    }

    private void saveReleasesInPreferences() {
        SharedPreferences.Editor editor = ((BaseActivity)getActivity()).getUserSharedPreferences().edit();
        SharedPreferencesLoader.saveReleasesList(editor, releasesModelList);
        Calendar calendar = Calendar.getInstance();
        int month = calendar.get(Calendar.MONTH);
        SharedPreferencesLoader.saveCurrentMonth(editor, month);
    }

    private List<ReleasesModel> getReleasesFromPreferences(){
        SharedPreferences preferences = ((BaseActivity)getActivity()).getUserSharedPreferences();
        return SharedPreferencesLoader.retrieveReleasesList(preferences);
    }

    @Override
    public void OnItemClick(ReleasesModel release) {
        if(release.getGameUrl() == null){
            showToastError(R.string.no_game_url);
        }
        else{
            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("UserPreferences",Context.MODE_PRIVATE);

            CustomTabsIntent.Builder builder = new CustomTabsIntent.Builder()
                    .addDefaultShareMenuItem();
            if(SharedPreferencesLoader.currentTheme(sharedPreferences) == R.style.AppTheme)
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryDark));
            else{
                builder.setToolbarColor(getResources().getColor(R.color.colorPrimaryText));
            }
            builder.setStartAnimations(getActivity(),R.anim.slide_in_right,R.anim.slide_out_left);
            builder.setExitAnimations(getActivity(),android.R.anim.slide_in_left,android.R.anim.slide_out_right);
            CustomTabsIntent customTab = builder.build();
            customTab.launchUrl(getActivity(),Uri.parse(release.getGameUrl()));
        }
    }
}
