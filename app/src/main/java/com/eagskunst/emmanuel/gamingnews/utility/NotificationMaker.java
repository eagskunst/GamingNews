package com.eagskunst.emmanuel.gamingnews.utility;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import android.util.Log;

import com.eagskunst.emmanuel.gamingnews.R;
import com.eagskunst.emmanuel.gamingnews.views.MainActivity;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.Locale;


public class NotificationMaker extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();
    private String sessionToken;
    public SharedPreferences sharedPreferences;
    public static NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();

    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG,"New token: "+s);
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"Message: "+remoteMessage);
        String lang = remoteMessage.getData().get("lang");
        if(lang.equals(Locale.getDefault().getLanguage())){
            inboxStyle.addLine(remoteMessage.getData().get("descp").replace("_"," "));
        }
        generateNotification(remoteMessage.getData().get("title"),remoteMessage.getData().get("descp"),
                    lang);
    }

    public void generateNotification(String title, String body, String lang) {
        Log.d(TAG, "Lang: "+lang);
        if(lang.equals(Locale.getDefault().getLanguage())){

            Intent pendingIntent = new Intent(this, MainActivity.class);
            pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            int requestCode = 0;
            PendingIntent notifyPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            requestCode,
                            pendingIntent,
                            0
                    );


            Notification notification = new NotificationCompat.Builder(this,"channelID")
                    .setContentTitle(title)
                    .setContentText(body)
                    .setContentIntent(notifyPendingIntent)
                    .setColor(getResources().getColor(R.color.colorPrimary))
                    .setStyle(inboxStyle)
                    .setSmallIcon(R.drawable.ic_all)
                    .build();

            final NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            createChannel(manager);
            manager.notify(requestCode,notification);
        }
    }

    private void createChannel(final NotificationManager manager) {
        if(Build.VERSION.SDK_INT < 26) return;
        final NotificationChannel channel = new NotificationChannel("channelID", "Articles channel", NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("Articles channel");
        manager.createNotificationChannel(channel);
    }

    public void setToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                if(SharedPreferencesLoader.getFirebaseToken(sharedPreferences).equals("no_token")){
                    String updatedToken = instanceIdResult.getToken();
                    sessionToken = updatedToken;
                    SharedPreferencesLoader.saveFirebaseToken(sharedPreferences.edit(),sessionToken);
                    Log.e("Updated Token",updatedToken);
                }
                else{
                    Log.e(TAG, "Token has already been created");
                }
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
