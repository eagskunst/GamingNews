package com.eagskunst.emmanuel.gamingnews.utility;

import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class BaseActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences));

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SharedPreferencesLoader.saveCurrentTime(getUserSharedPreferences().edit());
    }

    @Override
    protected void onResume() {
        super.onResume();
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        try{
            manager.cancelAll();
            NotificationMaker.inboxStyle = new NotificationCompat.InboxStyle();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    protected void showToolbar(Toolbar toolbar, int title, boolean upButton, ProgressBar progressBar) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(title);
        getSupportActionBar().setDisplayHomeAsUpEnabled(upButton);
        if(progressBar != null){
            progressBar.setVisibility(View.GONE);
            progressBar.setIndeterminate(false);
        }
    }

    protected void setFirebaseToken(){
        if(isPlayServicesAvailable()){
            callLog(getClass().getSimpleName(),"Play services available!");
            NotificationMaker nm = new NotificationMaker();
            nm.sharedPreferences = sharedPreferences;
            nm.setToken();

        }
    }

    protected boolean isPlayServicesAvailable() {
        int resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS){
            GoogleApiAvailability.getInstance().getErrorDialog(this, resultCode, 1).show();
            return false;
        }
        return true;
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
