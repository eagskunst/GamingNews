package com.eagskunst.emmanuel.gamingnews.Utility;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.views.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.TimeUnit;


public class NotificationMaker extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();
    private String sessionToken;
    public SharedPreferences sharedPreferences;

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG,"New token: "+s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"Message: "+remoteMessage);
        generateNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("descp"),
                    remoteMessage.getData().get("lang"));
    }

    private void generateNotification(String title, String body, String lang) {
        Log.d(TAG, "Lang: "+lang);
        if(lang.equals(Locale.getDefault().getLanguage())){
            Random r = new Random();
            Intent pendingIntent = new Intent(this, MainActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int requestCode = r.nextInt((100-1)+1)+1;
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            requestCode,
                            pendingIntent,
                            0
                    );

            Notification notification = new NotificationCompat.Builder(this,"0")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(notifyPendingIntent)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_all)
                    .build();

            NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
            manager.notify(requestCode,notification);
        }
    }

    public void setToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String updatedToken = instanceIdResult.getToken();
                sessionToken = updatedToken;
                SharedPreferencesLoader.saveFirebaseToken(sharedPreferences.edit(),sessionToken);
                Log.e("Updated Token",updatedToken);
            }
        });
    }

    public void setSessionToken(String sessionToken){
        this.sessionToken = sessionToken;
    }

    public String getSessionToken(){
        return sessionToken;
    }
}
