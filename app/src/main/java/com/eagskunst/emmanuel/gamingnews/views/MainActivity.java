package com.eagskunst.emmanuel.gamingnews.views;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.eagskunst.emmanuel.gamingnews.credentials.Credentials;
import com.eagskunst.emmanuel.gamingnews.fragments.NewsListFragment;
import com.eagskunst.emmanuel.gamingnews.models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.objects.LoadUrls;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity;
import com.eagskunst.emmanuel.gamingnews.utility.SharedPreferencesLoader;

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
    private NewsListFragment[] newsFragments;


    private String currentFrag;
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private LoadUrls loadUrls;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SharedPreferencesLoader.setCanLoadImages(getUserSharedPreferences());

        setContentView(R.layout.activity_main);
        boolean isNightActive = getUserSharedPreferences().getBoolean("night_mode",false);
        if(isNightActive){
            getWindow().getDecorView().setBackgroundColor(getResources().getColor(R.color.colorBackgroundNightMode));
        }

        MobileAds.initialize(this,Credentials.adMobCredential);

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
        newsFragments = new NewsListFragment[News_TAG.length];
        startNavigationView();
        initAllFragments();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newsFragments[0], News_TAG[0])
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        currentFrag = News_TAG[0];
        navigationView.setCheckedItem(R.id.all_news);
        if(newsFragments[0].getParserMaker() != null)
            newsFragments[0].getArticles();
        setOnBackChangeListener();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesLoader.saveCurrentTime(getUserSharedPreferences().edit());
    }


    private void startDrawerLayout(Toolbar toolbar) {
        drawerLayout = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout,
                toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.addDrawerListener(drawerToggle);
        drawerToggle.syncState();
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawerlogo1);
    }

    private void startNavigationView() {
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                switch (id) {
                    case R.id.all_news:
                        makeFragmentTransaction(newsFragments[0],id,News_TAG[0]);
                        break;
                    case R.id.ps4_news:
                        makeFragmentTransaction(newsFragments[1],id,News_TAG[1]);
                        break;
                    case R.id.xboxo_news:
                        makeFragmentTransaction(newsFragments[2],id,News_TAG[2]);
                        break;
                    case R.id.switch_news:
                        makeFragmentTransaction(newsFragments[3],id,News_TAG[3]);
                        break;
                    case R.id.PC_news:
                        makeFragmentTransaction(newsFragments[4],id,News_TAG[4]);
                        break;
                    case R.id.saved_news:
                        makeFragmentTransaction(newsFragments[5],id,News_TAG[5]);
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

    private void initAllFragments(){
        for(int i = 0;i<newsFragments.length;i++){
                newsFragments[i] = NewsListFragment.newInstance(getUrls(i));
        }
    }
    private void setOnBackChangeListener() {
        getSupportFragmentManager().addOnBackStackChangedListener(new FragmentManager.OnBackStackChangedListener() {
            @Override
            public void onBackStackChanged() {
                Fragment currentFragment = getCurrentFragment();
                String tag = currentFragment.getTag();
                if(tag.equals(News_TAG[0])){
                    navigationView.setCheckedItem(R.id.all_news);
                }
                else if(tag.equals(News_TAG[1])){
                    navigationView.setCheckedItem(R.id.ps4_news);
                }
                else if(tag.equals(News_TAG[2])){
                    navigationView.setCheckedItem(R.id.xboxo_news);
                }
                else if(tag.equals(News_TAG[3])){
                    navigationView.setCheckedItem(R.id.switch_news);
                }
                else if(tag.equals(News_TAG[4])){
                    navigationView.setCheckedItem(R.id.PC_news);
                }
                else if(tag.equals(News_TAG[5])){
                    navigationView.setCheckedItem(R.id.saved_news);
                }

            }
        });
    }

    private Fragment getCurrentFragment() {
        return this.getSupportFragmentManager().findFragmentById(R.id.container);
    }

    private String[] getUrls(int i) {
        String urls[] = null;
        switch (i){
            case 0:
                urls = loadUrls.getAllUrls();
                break;
            case 1:
                urls = loadUrls.getPs4Urls();
                break;
            case 2:
                urls = loadUrls.getXboxOUrls();
                break;
            case 3:
                urls = loadUrls.getSwitchUrls();
                break;
            case 4:
                urls = loadUrls.getPcUrls();
                break;
            case 5:
                urls = new String[]{"SAVEDLIST"};
                break;
        }
        return urls;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    private void makeFragmentTransaction(NewsListFragment fragment, int item, String _TAG){
        if(!currentFrag.equals(_TAG)){
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container,fragment,_TAG)
                    .addToBackStack(_TAG)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                    .commit();
            currentFrag = _TAG;
        }
        navigationView.setCheckedItem(item);
        drawerLayout.closeDrawers();
        if(fragment.getParserMaker() != null)
            fragment.getArticles();
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}