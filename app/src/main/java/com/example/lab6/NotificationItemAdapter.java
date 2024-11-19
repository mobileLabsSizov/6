package com.example.lab6;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationItemAdapter extends RecyclerView.Adapter<NotificationItemAdapter.ViewHolder>{

    private final List<NotificationItem> NotificationItems;

    private final Context context;

    NotificationItemAdapter(Context context, List<NotificationItem> NotificationItems) {
        this.NotificationItems = NotificationItems;
        this.context = context;
    }

    @NonNull
    @Override
    public NotificationItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.activity_notification_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationItemAdapter.ViewHolder holder, int position) {
        NotificationItem notification = NotificationItems.get(position);
        holder.title.setText(notification.title);
        holder.text.setText(notification.text);
        holder.notifyAt.setText(notification.notifyAt);

        holder.view.setOnClickListener(view -> {
            Intent intent = new Intent(context, MainNotification.class);
            intent.putExtra("id", notification.id);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return NotificationItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        final View view;
        final TextView title, text, notifyAt;
        ViewHolder(View view){
            super(view);
            this.view = view;
            title = view.findViewById(R.id.title);
            text = view.findViewById(R.id.description);
            notifyAt = view.findViewById(R.id.notifyAt);
        }
    }
}
