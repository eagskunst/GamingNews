package com.example.emmanuel.gamingnews.views;

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

import com.example.emmanuel.gamingnews.R;

public class WebViewActivity extends AppCompatActivity {
    private static final String TAG = "WebViewActivity";

    private WebView webView;
    private ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.loading,true);
        webView = findViewById(R.id.webview);
        String url = getIntent().getExtras().getString("url");
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
                finish();
            }
        });
    }

    private void loadUrl(String url) {
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
                Toast.makeText(WebViewActivity.this, "Oh no! " + description, Toast.LENGTH_SHORT).show();
            }
        });

        webView.loadUrl(url);
        webView.setVisibility(View.VISIBLE);
    }
}
