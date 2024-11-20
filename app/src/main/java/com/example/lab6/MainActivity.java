package com.example.lab6;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

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

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }

            db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "title TEXT NOT NULL," +
                "text TEXT," +
                "notify_at TIMESTAMP NOT NULL" +
                ");");
    }

    @Override
    protected void onStart() {
        super.onStart();
        findViewById(R.id.create).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainNotification.class);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fillNotifications(findViewById(R.id.reminderListView));
    }

    public void fillNotifications(RecyclerView reminderList) {
        try {
            Cursor query = db.rawQuery("SELECT _id, title, text, notify_at FROM Notification;", null);
            ArrayList<NotificationItem> itemsList = new ArrayList<>();
            while (query.moveToNext()) {
                itemsList.add(new NotificationItem(
                        query.getLong(0),
                        query.getString(1),
                        query.getString(2),
                        query.getString(3)
                ));
            }
            query.close();

            NotificationItemAdapter adapter = new NotificationItemAdapter(this, itemsList);
            reminderList.setAdapter(adapter);
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}