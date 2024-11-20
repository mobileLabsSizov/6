package com.example.lab6;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
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

    private final String CHANNEL_ID = "my_id";

    private AlarmManager alarm;

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
            findViewById(R.id.delete).setOnClickListener(v -> {
                try {
                    db.execSQL("DELETE FROM Notification WHERE _id = ?", new Object[]{id});

                    Intent intent = new Intent(this, MyReceiver.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    PendingIntent pendingIntent = PendingIntent.getBroadcast
                            (this, id.intValue(), intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);

                    alarm.cancel(pendingIntent);

                } catch (Exception e) {
                    e.printStackTrace();
                }
                this.finish();
            });
        } catch (Exception ignored) {
            findViewById(R.id.delete).setVisibility(View.GONE);
        }

        db = getBaseContext().openOrCreateDatabase("app.db", MODE_PRIVATE, null);

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

            String title = ((EditText) findViewById(R.id.title)).getText().toString();
            String text = ((EditText) findViewById(R.id.description)).getText().toString();

            try {
                if (id == null) {
                    SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());

                    ContentValues cv = new ContentValues();
                    cv.put("title", title);
                    cv.put("text", text);
                    cv.put("notify_at", parser.format(givenTimestamp.getTimeInMillis()));

                    id = db.insert("Notification", null, cv);
                } else {
                    db.execSQL("UPDATE Notification SET title = ?, text = ?, notify_at = datetime(?, 'localtime') WHERE _id = ?", new Object[]{
                            title,
                            text,
                            new Timestamp(givenTimestamp.getTimeInMillis()),
                            id
                    });
                }

                Intent intent = new Intent(this, MyReceiver.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                intent.putExtra("id", id);
                intent.putExtra("title", title);
                intent.putExtra("text", text);
                PendingIntent pendingIntent = PendingIntent.getBroadcast
                        (this, id.intValue(), intent, PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

                alarm.set(AlarmManager.RTC_WAKEUP, givenTimestamp.getTimeInMillis(), pendingIntent);

            } catch (Exception e) {
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }
            this.finish();
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

                SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
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
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("my_description");
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this.
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            alarm = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        }
    }
}