package com.eagskunst.emmanuel.gamingnews.views;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.eagskunst.emmanuel.gamingnews.fragments.SettingsFragment;
import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.utility.BaseActivity;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "SettingsActivity";
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        toolbar = findViewById(R.id.toolbar);
        showToolbar(toolbar,R.string.title_activity_settings,true,null);
        callLog(TAG, "Title: " + getSupportActionBar().getTitle().toString());
        getFragmentManager().beginTransaction()
                .replace(R.id.container_settings,SettingsFragment.newInstance())
                .commit();
        getSupportActionBar().setTitle(R.string.title_activity_settings);
    }


    public void replaceFragment(Fragment fragment,int title){
        String backStackName = fragment.getClass().getName();
        FragmentManager fm = getFragmentManager();
        boolean fragmentPopped = fm.popBackStackImmediate(backStackName,0);
        if (!fragmentPopped) { //fragment not in back stack, create it.
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.container_settings, fragment);
            ft.addToBackStack(backStackName);
            ft.commit();
            getSupportActionBar().setTitle(title);
        }
    }

}
