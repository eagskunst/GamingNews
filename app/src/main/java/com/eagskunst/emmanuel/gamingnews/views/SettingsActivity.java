package com.eagskunst.emmanuel.gamingnews.views;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.eagskunst.emmanuel.gamingnews.Fragments.SettingsFragment;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.title_activity_settings,true,null);
        callLog(TAG, "Title: " + getSupportActionBar().getTitle().toString());
        getFragmentManager().beginTransaction().replace(R.id.container_settings,SettingsFragment.newInstance())
                .commit();
    }

}
