package com.eagskunst.emmanuel.gamingnews.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity";

    private WebView webView;
    private ProgressBar progressBar;
    private FloatingActionButton fab;
    private List<NewsModel> newsList = new ArrayList<>();
    private NewsModel newsModel;

    private boolean isSaved;
    private boolean modified = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences));
        setContentView(R.layout.activity_web_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.loading,true);
        webView = findViewById(R.id.webview);
        fab = findViewById(R.id.webviewFAB);
        fab.setOnClickListener(saveArticle());
        String url = getIntent().getExtras().getString("url");
        newsModel = getIntent().getExtras().getParcelable("Article");
        getSavedList();
        setFloatingButtonInitialIcon(url);
        loadUrl(url);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        // Otherwise defer to system default behavior.
        webView.loadUrl("about:blank");
        super.onBackPressed();
    }
    private void showToolbar(Toolbar toolbar, int title, boolean upButton){
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        Log.d(TAG,"Title: "+getSupportActionBar().getTitle().toString());
        progressBar = findViewById(R.id.toolbarProgressBar);
        progressBar.setVisibility(View.INVISIBLE);
        progressBar.setIndeterminate(false);


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG,"Entré a terminar ");
                if(modified){
                    //Saving modifications
                    Log.d(TAG,"Enter onDestroy if");
                    SharedPreferencesLoader.saveList(getSharedPreferences("UserPreferences",0).edit(),newsList);
                }
                finish();
            }
        });

    }

    private void getSavedList(){
        List<NewsModel> savedList = SharedPreferencesLoader.retrieveList(getSharedPreferences("UserPreferences",0));
        try{
            newsList.addAll(savedList);
        }catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(WebViewActivity.this, R.string.error_retrieving, Toast.LENGTH_SHORT).show();
        }
    }

    private void setFloatingButtonInitialIcon(String url){
        try{
            for(NewsModel newsModel:newsList){
                if(newsModel.getLink().equals(url)){
                    this.newsModel = newsModel;
                    fab.setImageResource(R.drawable.ic_star_on);
                    isSaved = true;
                    return;
                }
            }
        }catch (NullPointerException e){
            e.printStackTrace();
            Toast.makeText(WebViewActivity.this, R.string.error_relaunch, Toast.LENGTH_SHORT).show();
        }
        fab.setImageResource(R.drawable.ic_star_off);
    }

    private void loadUrl(String url) {
        progressBar.setVisibility(View.VISIBLE);
        WebSettings webSettings = webView.getSettings();
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);
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
                Toast.makeText(WebViewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
    }

    private View.OnClickListener saveArticle() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try{
                    setResult(1);
                    modified = true;
                    if(isSaved){
                        boolean removed = newsList.remove(newsModel);
                        Log.d(TAG, "onClick: removed? "+removed);
                        fab.setImageResource(R.drawable.ic_star_off);
                        isSaved = false;
                    }
                    else{
                        Log.d(TAG,"Added!!: "+newsModel.getTitle());
                        newsList.add(0,newsModel);
                        fab.setImageResource(R.drawable.ic_star_on);
                        isSaved = true;
                        Toast.makeText(WebViewActivity.this, R.string.article_saved, Toast.LENGTH_SHORT).show();
                    }
                }catch(NullPointerException e){

                    e.printStackTrace();
                    Toast.makeText(WebViewActivity.this, R.string.error_relaunch, Toast.LENGTH_SHORT).show();
                }

            }
        };
    }
}
