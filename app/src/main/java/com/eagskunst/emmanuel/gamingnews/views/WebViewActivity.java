package com.eagskunst.emmanuel.gamingnews.views;

import android.support.design.widget.FloatingActionButton;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.eagskunst.emmanuel.gamingnews.Models.NewsModel;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.BaseActivity;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;

import java.util.ArrayList;
import java.util.List;

public class WebViewActivity extends BaseActivity {
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
        setContentView(R.layout.activity_web_view);

        Toolbar toolbar = findViewById(R.id.toolbar);
        progressBar = findViewById(R.id.toolbarProgressBar);
        showToolbar(toolbar,R.string.loading,true,progressBar);
        callLog(TAG, "Title: " + getSupportActionBar().getTitle().toString());


        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callLog(TAG,"Entré a terminar ");
                if(modified){
                    //Saving modifications
                    callLog(TAG,"Enter onDestroy if");
                    // SharedPreferencesLoader.saveList(getSharedPreferences("UserPreferences",0).edit(),newsList);
                    SharedPreferencesLoader.saveList(getUserSharedPreferences().edit(),newsList);
                }
                finish();
            }
        });

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

    private void getSavedList(){
        List<NewsModel> savedList = SharedPreferencesLoader.retrieveList(getUserSharedPreferences());
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
                        callLog(TAG, "onClick: removed? "+removed);
                        fab.setImageResource(R.drawable.ic_star_off);
                        isSaved = false;
                    }
                    else{
                        callLog(TAG,"Added!!: "+newsModel.getTitle());
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
