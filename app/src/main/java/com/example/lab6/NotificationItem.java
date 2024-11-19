package com.example.lab6;

import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class NotificationItem {

    public long id;
    public String title, text, notifyAt;

    public NotificationItem(long id, String title, String text, String notifyAt) {
        this.id = id;
        this.title = title;
        this.text = text;
        this.notifyAt = notifyAt;
    }
}