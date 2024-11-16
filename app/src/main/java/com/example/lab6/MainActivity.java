package com.example.lab6;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class MainActivity extends AppCompatActivity {

    private SQLiteDatabase db;

    private final String CHANNEL_ID = "my_id";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "title TEXT NOT NULL," +
                "text TEXT," +
                "notify_at TIMESTAMP NOT NULL" +
                ");");
    }

    public void showNotifications(View view) {
        try {
            Cursor query = db.rawQuery("SELECT _id, title, text, notify_at FROM Notification;", null);
            StringBuilder result = new StringBuilder("ID, Title, Text, Created At\n");
            while (query.moveToNext()) {
                result.append(query.getString(0)).append(", ")
                        .append(query.getString(1)).append(", ")
                        .append(query.getString(2)).append(", ")
                        .append(query.getString(3)).append("\n");
            }
            query.close();

            Intent intent = new Intent(this, MainActivity2.class);
            intent.putExtra("data", result.toString());
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void createNotification(View view) {
        try {
            ContentValues values = new ContentValues();
            values.put("title", "");
            values.put("text", "");
            values.put("notify_at", "");
            long id = db.insert("Notification", null, values);


            Intent intent = new Intent(this, Notification.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            intent.putExtra("id", id);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.notification_icon)
                    .setContentTitle(textTitle)
                    .setContentText(textContent)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent);

            // TODO notify

        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    public void deleteNotification(View view) {
        try {
            db.execSQL("DELETE FROM Notification WHERE _id = ?",new Object[]{

            });
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is not in the Support Library.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, "my_name", importance);
            channel.setDescription("my_description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}