package com.eagskunst.emmanuel.gamingnews.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.eagskunst.emmanuel.gamingnews.Fragments.SettingsFragment;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.Utility.SharedPreferencesLoader;

public class SettingsActivity extends AppCompatActivity {

    private static final String TAG = "SettingsActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        SharedPreferences sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences));
        setContentView(R.layout.activity_settings);
        Toolbar toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.title_activity_settings,true);
        getFragmentManager().beginTransaction().replace(R.id.container_settings,SettingsFragment.newInstance())
                .commit();
    }

    private void showToolbar(Toolbar toolbar, int title, boolean upButton) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        toolbar.getLogo().setBounds(3, 3, 3, 3);
        Log.d(TAG, "Title: " + getSupportActionBar().getTitle().toString());
    }


}
