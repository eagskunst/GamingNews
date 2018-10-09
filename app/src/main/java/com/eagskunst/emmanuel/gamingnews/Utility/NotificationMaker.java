package com.eagskunst.emmanuel.gamingnews.Utility;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
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


public class NotificationMaker extends FirebaseMessagingService {
    private final String TAG = getClass().getSimpleName();
    private String sessionToken;
    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Log.d(TAG,"New token: "+s);

    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG,"Message: "+remoteMessage);
        generateNotification(remoteMessage.getNotification().getTitle(),remoteMessage.getNotification().getBody());
    }

    private void generateNotification(String title, String body) {

        Intent pendingIntent = new Intent(this, MainActivity.class);
        pendingIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent notifyPendingIntent =
                PendingIntent.getActivity(
                        this,
                        99,
                        pendingIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        Notification notification = new NotificationCompat.Builder(this,"0")
                .setContentTitle(title)
                .setContentText(body)
                .setContentIntent(notifyPendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();

        NotificationManagerCompat manager = NotificationManagerCompat.from(getApplicationContext());
        manager.notify(0,notification);
    }

    public void setToken() {
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String updatedToken = instanceIdResult.getToken();
                sessionToken = updatedToken;
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
