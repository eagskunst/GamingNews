package com.eagskunst.emmanuel.gamingnews.Utility;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.eagskunst.emmanuel.gamingnews.R;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;

public class BaseActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final String PREFERENCES_USER = "UserPreferences";
        sharedPreferences = getSharedPreferences(PREFERENCES_USER, Context.MODE_PRIVATE);
        setTheme(SharedPreferencesLoader.currentTheme(sharedPreferences));
        if(isPlayServicesAvailable()){
            callLog(getClass().getSimpleName(),"Play services available!");
            NotificationMaker nm = new NotificationMaker();
            nm.setToken();
            FirebaseMessaging.getInstance().subscribeToTopic("all");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /* Code for knowing user last session
        DatabaseReference presenceRef = FirebaseDatabase.getInstance().getReference("disconnectmessage");
        presenceRef.onDisconnect().setValue(ServerValue.TIMESTAMP);
*/
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
