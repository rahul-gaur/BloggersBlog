package com.rahulgaur.bloggersblog.notification.notificationServices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rahulgaur.bloggersblog.R;

import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessaging extends FirebaseMessagingService {
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Map<String, String> data = remoteMessage.getData();
        Log.e("MyFirebaseMessaging", "Data " + data.toString());
        showNotification(Objects.requireNonNull(remoteMessage.getNotification()), remoteMessage);
    }

    private void showNotification(RemoteMessage.Notification notification, RemoteMessage remoteMessage) {

        String clickAction = notification.getClickAction();

        Map<String, String> data = remoteMessage.getData();

        Intent intent = new Intent(clickAction);
        if (data.size() > 0) {
            String post_id = data.get("id");
            intent.putExtra("id", post_id);
        } else {
            String post_id = "UjK7FheiXbrAv8HVsqq6";
            intent.putExtra("id", post_id);
        }
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivities(this, 0, new Intent[]{intent}, PendingIntent.FLAG_ONE_SHOT);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(R.drawable.icon)
                .setContentTitle(notification.getTitle())
                .setContentText(notification.getBody())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Objects.requireNonNull(notificationManager).notify(0, builder.build());
    }
}
