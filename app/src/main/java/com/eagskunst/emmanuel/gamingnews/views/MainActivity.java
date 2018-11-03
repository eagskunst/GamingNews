package com.eagskunst.emmanuel.gamingnews.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.eagskunst.emmanuel.gamingnews.Fragments.NewsListFragment;
import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.Objects.LoadUrls;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.BaseActivity;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.messaging.FirebaseMessaging;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends BaseActivity implements NewsListFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final String[] News_TAG = {"NewsListFragment_All","NewsListFragment_PS4","NewsListFragment_XboxO",
                                                "NewsListFragment_Switch","NewsListFragment_PC","NewsListFragment_Saved"
                                                };
    private static final int[] tab_id ={R.id.all_news,R.id.ps4_news,R.id.xboxo_news,R.id.switch_news,R.id.PC_news,R.id.saved_news};


    private String currentFrag;
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private LoadUrls loadUrls;
    private NavigationView navigationView;
    private List<String> navigationHistory = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesLoader.setCanLoadImages(getUserSharedPreferences());

        setContentView(R.layout.activity_main);
        boolean isNightActive = getUserSharedPreferences().getBoolean("night_mode",false);
        if(isNightActive){
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorBackgroundNightMode));
        }

        MobileAds.initialize(this,"ca-app-pub-7679100799273392~6141549329");

        AdView adView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .build();
        adView.loadAd(adRequest);

        setFirebaseToken();

        //In first launch, create saved list
        if(getUserSharedPreferences().getBoolean("first_launch",true)){
            callLog(TAG,"First launch of this app in this device.");
            if(Locale.getDefault().getLanguage().equals("es")){
                FirebaseMessaging.getInstance().subscribeToTopic(Locale.getDefault().getLanguage());
            }
            else{
                FirebaseMessaging.getInstance().subscribeToTopic("en");
            }
            SharedPreferences.Editor spEditor = getUserSharedPreferences().edit();
            List<NewsModel> savedNewsList = new ArrayList<>();
            List<String> topicList = new ArrayList<>();
            SharedPreferencesLoader.saveList(spEditor,savedNewsList);
            SharedPreferencesLoader.saveTopics(spEditor,topicList);
            spEditor.putBoolean("first_launch",false).apply();
            spEditor.putBoolean("night_mode",false).apply();
            spEditor.putBoolean("load_images",true).apply();
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        progressBar = findViewById(R.id.toolbarProgressBar);
        showToolbar(toolbar, R.string.app_name, false,progressBar);
        callLog(TAG, "Title: " + getSupportActionBar().getTitle().toString());
        startDrawerLayout(toolbar);

        InputStream is;
        loadUrls = null;
        try {
            is = getAssets().open("Urls.json");
            loadUrls = new LoadUrls(Locale.getDefault().getLanguage(), is);
            loadUrls.setUrls();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startNavigationView();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, NewsListFragment.newInstance(loadUrls.getAllUrls()), News_TAG[0])
                .addToBackStack(News_TAG[0])
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
        currentFrag = News_TAG[0];
        navigationHistory.add(News_TAG[0]);
        navigationView.setCheckedItem(tab_id[0]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        navigationHistory.clear();
        SharedPreferencesLoader.saveCurrentTime(getUserSharedPreferences().edit());
    }

    @Override
    public void onBackPressed() {
        NewsListFragment fragment = (NewsListFragment) getSupportFragmentManager().findFragmentByTag(currentFrag);
        int size = navigationHistory.size() - 2;
        if(drawerLayout.isDrawerOpen(GravityCompat.START)){
            drawerLayout.closeDrawers();
        }
        else if(fragment.getTag().equals(News_TAG[0])){
            if(fragment.getParserMaker().isRunning()){
                moveTaskToBack(true);
            }
            else{
                getSupportFragmentManager().popBackStackImmediate(null,FragmentManager.POP_BACK_STACK_INCLUSIVE);
                finish();
            }
        }
        else{
            if(getSupportFragmentManager().findFragmentByTag(navigationHistory.get(size)).getTag().equals(currentFrag)){
                currentFrag = getSupportFragmentManager().findFragmentByTag(navigationHistory.get(size-1)).getTag();
            }
            else{
                currentFrag = getSupportFragmentManager().findFragmentByTag(navigationHistory.get(size)).getTag();
            }
            int i = 0;
            while(!currentFrag.equals(News_TAG[i])){
                i++;
            }
            navigationView.setCheckedItem(tab_id[i]);
            hideAndShow(fragment,(NewsListFragment)getSupportFragmentManager().findFragmentByTag(currentFrag));
            navigationHistory.remove(fragment.getTag());
        }
    }

    private void startDrawerLayout(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
    }

    private void startNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.all_news:
                        makeFragmentTransaction(loadUrls.getAllUrls(),tab_id[0],News_TAG[0]);
                        break;
                    case R.id.ps4_news:
                        makeFragmentTransaction(loadUrls.getPs4Urls(), tab_id[1],News_TAG[1]);
                        break;
                    case R.id.xboxo_news:
                        makeFragmentTransaction(loadUrls.getXboxOUrls(), tab_id[2],News_TAG[2]);
                        break;
                    case R.id.switch_news:
                        makeFragmentTransaction(loadUrls.getSwitchUrls(), tab_id[3],News_TAG[3]);
                        break;
                    case R.id.PC_news:
                        makeFragmentTransaction(loadUrls.getPcUrls(), tab_id[4],News_TAG[4]);
                        break;
                    case R.id.saved_news:
                        makeFragmentTransaction(new String[]{"SAVEDLIST"}, tab_id[5],News_TAG[5]);
                        break;
                    case R.id.settings:
                        startActivity(new Intent(MainActivity.this,SettingsActivity.class));
                        drawerLayout.closeDrawers();
                        break;

                }
                return true;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void makeFragmentTransaction(String[] urls, int item, String _TAG) {
        NewsListFragment newsFragment = (NewsListFragment) getSupportFragmentManager().findFragmentByTag(_TAG);
        if(newsFragment == null){
            newsFragment = NewsListFragment.newInstance(urls);
            getSupportFragmentManager().beginTransaction()
                    .hide(getSupportFragmentManager().findFragmentByTag(currentFrag))
                    .add(R.id.container,newsFragment,_TAG)
                    .addToBackStack(_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
            currentFrag = _TAG;
            navigationHistory.add(_TAG);
        }
        else if(!currentFrag.equals(_TAG)){
            hideAndShow((NewsListFragment)getSupportFragmentManager().findFragmentByTag(currentFrag),newsFragment);
            if(!navigationHistory.contains(_TAG)){
                navigationHistory.add(_TAG);
            }
            else{
                int i = navigationHistory.size() - 1;
                while(!navigationHistory.get(i).equals(_TAG)){
                    navigationHistory.remove(i);
                    i--;
                }
            }
            currentFrag = _TAG;
        }
        callLog(TAG,"Size: "+navigationHistory.size());

        navigationView.setCheckedItem(item);
        drawerLayout.closeDrawers();
    }

    private void hideAndShow(NewsListFragment toHide,NewsListFragment toShow) {
        getSupportFragmentManager().beginTransaction()
                .hide(toHide)
                .show(toShow)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}