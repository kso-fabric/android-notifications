package cz.anderle.fabric.notifications.client.app;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getData() == null) return;
        String id = intent.getData().getLastPathSegment();
        try {
            JSONArray a = Store.read(c);
            for (int i = 0; i < a.length(); i++) {
                JSONObject item = a.getJSONObject(i);
                if (!item.getString("id").equals(id)) continue;
                NotificationManager manager = c.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(new NotificationChannel("fabric.reminders", "Připomínky", NotificationManager.IMPORTANCE_HIGH));
                PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                Notification notification = new Notification.Builder(c, "fabric.reminders").setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("Připomínka").setContentText(item.getString("title"))
                        .setStyle(new Notification.BigTextStyle().bigText(item.getString("title")))
                        .setContentIntent(open).setAutoCancel(true).build();
                manager.notify(id.hashCode(), notification);
                return;
            }
        } catch (Exception e) {
            Log.e("Fabric.Notifications", "Showing notification failed", e);
        }
    }
}
