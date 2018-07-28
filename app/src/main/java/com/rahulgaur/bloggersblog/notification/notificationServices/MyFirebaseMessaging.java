package com.rahulgaur.bloggersblog.notification.notificationServices;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.rahulgaur.bloggersblog.R;

import java.util.Map;
import java.util.Objects;

import static android.support.constraint.Constraints.TAG;
import static com.rahulgaur.bloggersblog.home.PostRecyclerAdapter.context;

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
            String update = data.get("update");

            if (update.equals("yes")){
                Log.e(TAG, "showNotification: update received "+update);
                update();
            }
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

    private void update() {
        Log.e(TAG, "showNotification: entered in update() ");
        final String appPackageName = getPackageName(); // getPackageName() from Context or Activity object
        Log.e(TAG, "update: packageName "+appPackageName);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
        } catch (android.content.ActivityNotFoundException anfe) {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
    }
}
