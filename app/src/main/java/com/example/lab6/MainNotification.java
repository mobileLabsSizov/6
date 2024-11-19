package com.example.lab6;

import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class MainNotification extends AppCompatActivity {

    private Long id;
    private SQLiteDatabase db;

    private NotificationManager notificationManager;

    private final String CHANNEL_ID = "my_id";

    final SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-ddTHH:mm:ss", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main_notification);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        try {
            id = getIntent().getExtras().getLong("id");
        } catch (Exception ignored) {
        }

        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);
        db.execSQL("CREATE TABLE IF NOT EXISTS Notification (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT ," +
                "title TEXT NOT NULL," +
                "text TEXT," +
                "notify_at TIMESTAMP NOT NULL" +
                ");");

        createNotificationChannel();
    }

    @Override
    protected void onStart() {
        super.onStart();

        findViewById(R.id.choose_datetime).setOnClickListener(v -> {
            setDate();
            setTime();
            setDateTime();
        });

        findViewById(R.id.save).setOnClickListener(v -> {
            try {
                if (id == null) {
                    ContentValues cv = new ContentValues();
                    cv.put("title", (String) ((TextView) findViewById(R.id.title)).getText());
                    cv.put("text", (String) ((TextView) findViewById(R.id.description)).getText());
                    cv.put("notify_at", parser.format(givenTimestamp));

                    id = db.insert("Notification", null, cv);
                } else {
                    db.execSQL("UPDATE Notification SET title = ?, text = ?, notify_at = ? WHERE _id = ?", new Object[]{
                            ((TextView) findViewById(R.id.title)).getText(),
                            ((TextView) findViewById(R.id.description)).getText(),
                            new Timestamp(givenTimestamp.getTimeInMillis()),
                            id
                    });
                }

                Intent intent = new Intent(this, MainNotification.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", id);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

                NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                        .setContentTitle(((TextView) findViewById(R.id.title)).getText())
                        .setContentText(((TextView) findViewById(R.id.description)).getText())
                        .setSmallIcon(R.drawable.notification_icon)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setAutoCancel(true)
                        .setWhen(givenTimestamp.getTimeInMillis())
                        .setContentIntent(pendingIntent);

                notificationManager.notify(id.intValue(), builder.build());
            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            onDestroy();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        fill();
    }

    public void fill() {
        try {
            if (id != null) {
                Cursor query = db.rawQuery("SELECT title, text, notify_at FROM Notification WHERE _id = ?", new String[]{id.toString()});
                query.moveToFirst();

                ((TextView) findViewById(R.id.title)).setText(query.getString(0));
                ((TextView) findViewById(R.id.description)).setText(query.getString(1));

                givenTimestamp.setTime(parser.parse(query.getString(2)));

                query.close();
            }
        } catch (Exception e) {
            Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show();
        }

        timestampString = findViewById(R.id.notifyAt);
        givenTimestamp.set(Calendar.SECOND, 0);
        setDateTime();
    }

    TextView timestampString;
    Calendar givenTimestamp = Calendar.getInstance();

    // отображаем диалоговое окно для выбора даты
    public void setDate() {
        new DatePickerDialog(this, d,
                givenTimestamp.get(Calendar.YEAR),
                givenTimestamp.get(Calendar.MONTH),
                givenTimestamp.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    // отображаем диалоговое окно для выбора времени
    public void setTime() {
        new TimePickerDialog(this, t,
                givenTimestamp.get(Calendar.HOUR_OF_DAY),
                givenTimestamp.get(Calendar.MINUTE), true)
                .show();
    }

    // установка обработчика выбора времени
    TimePickerDialog.OnTimeSetListener t = (view, hourOfDay, minute) -> {
        givenTimestamp.set(Calendar.HOUR_OF_DAY, hourOfDay);
        givenTimestamp.set(Calendar.MINUTE, minute);
        setDateTime();
    };

    // установка обработчика выбора даты
    DatePickerDialog.OnDateSetListener d = (view, year, monthOfYear, dayOfMonth) -> {
        givenTimestamp.set(Calendar.YEAR, year);
        givenTimestamp.set(Calendar.MONTH, monthOfYear);
        givenTimestamp.set(Calendar.DAY_OF_MONTH, dayOfMonth);
        setDateTime();
    };

    // установка начальных даты и времени
    private void setDateTime() {
        timestampString.setText(DateUtils.formatDateTime(this,
                givenTimestamp.getTimeInMillis(),
                DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR
                        | DateUtils.FORMAT_SHOW_TIME));
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
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}