package com.example.emmanuel.gamingnews.views;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.example.emmanuel.gamingnews.Fragments.NewsListFragment;
import com.example.emmanuel.gamingnews.Objects.LoadUrls;
import com.example.emmanuel.gamingnews.R;



import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements NewsListFragment.OnFragmentInteractionListener {

    private static final String TAG = "MainActivity";
    private static final String[] News_TAG = {"NewsListFragment_All","NewsListFragment_PS4","NewsListFragment_XboxO",
                                                "NewsListFragment_Switch","NewsListFragment_PC"
                                                };

    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private LoadUrls loadUrls;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        navigationView = findViewById(R.id.navigation_view);
        showToolbar(toolbar, R.string.app_name, false);
        startDrawerLayout(toolbar);
        InputStream is = null;
        loadUrls = null;
        try {
            is = getAssets().open("Urls.json");
            loadUrls = new LoadUrls(Locale.getDefault().getLanguage(), is);
            loadUrls.setUrls();
        } catch (IOException e) {
            e.printStackTrace();
        }
        startNavigationView();
        makeFragmentTransaction(loadUrls.getAllUrls(), R.id.all_news,News_TAG[0]);
    }


    @Override
    public void onBackPressed() {
        NewsListFragment fragment = null;
        for(int i = 0;i<5;i++){
            fragment = (NewsListFragment) getSupportFragmentManager().findFragmentByTag(News_TAG[i]);
            if(fragment!=null){
                break;
            }
        }
        if (getSupportFragmentManager().getBackStackEntryCount() > 1) {
            getSupportFragmentManager().popBackStack();
        } else {
            if (fragment.getParserMaker().isRunning()) {
                moveTaskToBack(true);
            } else {
                finish();
            }
        }
    }

    private void showToolbar(Toolbar toolbar, int title, boolean upButton) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        toolbar.getLogo().setBounds(3, 3, 3, 3);
        Log.d(TAG, "Title: " + getSupportActionBar().getTitle().toString());
        progressBar = findViewById(R.id.toolbarProgressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
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
                        makeFragmentTransaction(loadUrls.getAllUrls(), R.id.all_news,News_TAG[0]);
                        break;
                    case R.id.ps4_news:
                        makeFragmentTransaction(loadUrls.getPs4Urls(), R.id.all_news,News_TAG[1]);
                        break;
                    case R.id.xboxo_news:
                        makeFragmentTransaction(loadUrls.getXboxOUrls(), R.id.all_news,News_TAG[2]);
                        break;
                    case R.id.switch_news:
                        makeFragmentTransaction(loadUrls.getSwitchUrls(), R.id.all_news,News_TAG[3]);
                        break;
                    case R.id.PC_news:
                        makeFragmentTransaction(loadUrls.getPcUrls(), R.id.all_news,News_TAG[4]);
                        break;
                }
                return true;
            }
        });
    }

    private void makeFragmentTransaction(String[] urls, int item, String TAG) {
        Bundle bundle = new Bundle();
        bundle.putStringArray("urls", urls);
        NewsListFragment newsFragment = (NewsListFragment) getSupportFragmentManager().findFragmentByTag(TAG);
        if(newsFragment == null){
            newsFragment = new NewsListFragment();
        }
        newsFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, newsFragment, TAG)
                .addToBackStack(null)
                .commit();
        navigationView.setCheckedItem(item);
        drawerLayout.closeDrawers();
    }

    @Override
    public void onFragmentInteraction(Uri uri) {

    }
}