package com.example.lab6;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

public class MyReceiver extends BroadcastReceiver {

    private final String CHANNEL_ID = "my_id";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
        Bundle b = intent.getExtras();
        int id = (int) b.getLong("id");

            Intent activity = new Intent(context, MainNotification.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, id, activity, PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setContentTitle(b.getString("title"))
                .setContentText(b.getString("text"))
                .setSmallIcon(R.drawable.notification_icon)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                builder.setChannelId(CHANNEL_ID);
            }

            context.getSystemService(NotificationManager.class).notify(id, builder.build());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}