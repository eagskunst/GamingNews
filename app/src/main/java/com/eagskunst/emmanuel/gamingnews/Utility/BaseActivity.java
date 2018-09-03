package com.eagskunst.emmanuel.gamingnews.Utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.eagskunst.emmanuel.gamingnews.R;

public class BaseActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences));
    }

    protected void showToolbar(Toolbar toolbar, int title, boolean upButton, ProgressBar progressBar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        getSupportActionBar().setLogo(R.mipmap.ic_launcher_round);
        toolbar.getLogo().setBounds(3, 3, 3, 3);
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
            progressBar.setIndeterminate(false);
        }
    }

    protected void callLog(String TAG, String message){
        Log.d(TAG,message);
    }

    public void setUserPreferences(SharedPreferences sharedPreferences){
        this.sharedPreferences = sharedPreferences;
    }
    public SharedPreferences getUserSharedPreferences(){
        return sharedPreferences;
    }
}
