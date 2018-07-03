package com.example.emmanuel.gamingnews;

import android.content.Context;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.widget.SwipeRefreshLayout;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.emmanuel.gamingnews.Adapter.NewsAdapter;
import com.example.emmanuel.gamingnews.Models.NewsModel;
import com.example.emmanuel.gamingnews.Utility.ParserMaker;


import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private RecyclerView recyclerView;
    private SwipeRefreshLayout refreshLayout;
    private NewsAdapter newsAdapter;
    private List<NewsModel> newsList = new ArrayList<>();
    private ParserMaker parserMaker;
    private WebView webView;
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String[] urls;
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.recyclerview);
        refreshLayout = findViewById(R.id.refreshlayout);
        webView = findViewById(R.id.webview);
        newsAdapter = new NewsAdapter(newsList, clickListener());
        if(Locale.getDefault().getLanguage().equals("es")){
            urls = new String[]{"https://www.eurogamer.es/?format=rss","https://vandal.elespanol.com/xml.cgi","https://www.levelup.com/rss","http://www.tierragamer.com/feed/"};
        }
        else{
            urls = new String[]{"https://www.gamespot.com/feeds/news/","https://www.vg247.com/feed/","https://www.eurogamer.net/?format=rss","https://kotaku.com/tag/gaming/rss","https://www.gameinformer.com/news.xml"};
        }
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.app_name,false);

        manageRecyclerView(true,true);

        parserMaker = new ParserMaker(MainActivity.this,urls,
                    Toast.makeText(MainActivity.this, "Can't get all articles from sites...", Toast.LENGTH_SHORT),
                        this.newsAdapter,this.newsList);

        parserMaker.create();

        refreshLayout.setRefreshing(true);
        manageRefreshLayout(parserMaker);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(newsList!=null){
            newsList.clear();
        }
        parserMaker.setActivity(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        parserMaker.setActivity(parserMaker.isRunning() ? MainActivity.this:null);
    }

    @Override
    protected void onPause() {
        super.onPause();
        parserMaker.setActivity(parserMaker.isRunning() ? MainActivity.this:null);
    }

    @Override
    public void onBackPressed() {
        if(webView.getVisibility() == View.INVISIBLE || webView.getVisibility() == View.GONE){
            if(parserMaker.isRunning()){
                //Bugfix for parserMaker's activity setting to null when onBackPressed. This puts MainActivity on stack, but there's
                //no other activity, so basic funcionality of onBackPressed() stays the same...
                moveTaskToBack(true);
            }
            else{
                super.onBackPressed();
            }
        }
    }

    private void showToolbar(Toolbar toolbar, int title, boolean upButton){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        Log.d(TAG,"Title: "+getSupportActionBar().getTitle().toString());
        progressBar = findViewById(R.id.toolbarProgressBar);
        progressBar.setVisibility(View.GONE);
        progressBar.setIndeterminate(false);
    }

    private void manageRecyclerView(boolean autoMeasure, boolean fixedSize) {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setAutoMeasureEnabled(autoMeasure);

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(newsAdapter);
        recyclerView.setHasFixedSize(fixedSize);

    }

    private void manageRefreshLayout(final ParserMaker parserMaker) {
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if(isNetworkAvailable() && !parserMaker.isRunning()) {
                    refreshLayout.setRefreshing(true);
                    if(parserMaker.getActivity() == null)
                        parserMaker.setActivity(MainActivity.this);
                    parserMaker.create();
                }
                else if(!isNetworkAvailable()){
                    AlertDialog dialog = generateDialog("Check your internet connection");
                    dialog.show();
                    refreshLayout.setRefreshing(false);
                }
                else if(parserMaker.isRunning()){
                    AlertDialog dialog = generateDialog("App is still refreshing");
                    dialog.show();
                    refreshLayout.setRefreshing(false);
                }
            }
        });
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }

    private AlertDialog generateDialog(String message)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        return builder.create();
    }

    private NewsAdapter.NewsViewHolder.OnItemClickListener clickListener() {
        return new NewsAdapter.NewsViewHolder.OnItemClickListener() {
            @Override
            public void OnItemClick(NewsModel item) {

                getSupportActionBar().setTitle("Loading...");
                progressBar.setVisibility(View.VISIBLE);
                WebSettings webSettings = webView.getSettings();
                webSettings.setJavaScriptEnabled(true);

                webView.setWebChromeClient(new WebChromeClient(){
                                               @Override
                                               public void onProgressChanged(WebView view, int newProgress) {
                                                   super.onProgressChanged(view, newProgress);
                                                   progressBar.setProgress(newProgress);
                                                   if(newProgress == 100){
                                                       progressBar.setVisibility(View.GONE);
                                                       getSupportActionBar().setTitle(R.string.app_name);
                                                   }
                                               }
                                           }
                );

                webView.setWebViewClient(new WebViewClient() {
                    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                        Toast.makeText(getApplicationContext(), "Oh no! " + description, Toast.LENGTH_SHORT).show();
                    }
                });

                webView.setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View view, int i, KeyEvent keyEvent) {
                        if(i == KeyEvent.KEYCODE_BACK){
                            webView.loadUrl("about:blank");
                            progressBar.setVisibility(View.GONE);
                            webView.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                            refreshLayout.setEnabled(true);
                            return true;
                        }
                        else{
                            return false;
                        }
                    }
                });

                webView.loadUrl(item.getLink());
                recyclerView.setVisibility(View.INVISIBLE);
                webView.setVisibility(View.VISIBLE);
                refreshLayout.setEnabled(false);
            }
        };
    }

}