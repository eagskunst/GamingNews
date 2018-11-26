package com.eagskunst.emmanuel.gamingnews.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.eagskunst.emmanuel.gamingnews.fragments.NewsListFragment;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity;

public class ArticlesFromNotificationActivity extends BaseActivity {

    private static final String TAG = "ArticlesFromNotification";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_articles_from_notification);
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.notification,true,null);
        callLog(TAG, "Title: " + getSupportActionBar().getTitle().toString());
        String[] fakeUrls = {TAG};
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.notificationContainer,NewsListFragment.newInstance(fakeUrls))
                .commit();
    }
}
