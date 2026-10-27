package cz.anderle.fabric.notifications.client.app.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.UUID;

import cz.anderle.fabric.notifications.client.app.MainActivity;
import cz.anderle.fabric.notifications.client.app.Store;
import cz.anderle.fabric.notifications.client.app.config.OffsetConfig;

/**
 * Je volán pri alarm eventu. Zobrazí notifikaci.
 */
public class ReminderReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getData() == null)
            return;

        UUID id = UUID.fromString(intent.getData().getLastPathSegment());
        String offsetIndex = intent.getData().getQueryParameter("offset");
        String notificationTitle = OffsetConfig.findOffset(offsetIndex);

        try {
            var events = Store.read(c);
            for (var event : events) {
                if (!event.id().equals(id)) {
                    continue;
                }

                NotificationManager manager = c.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(new NotificationChannel("fabric.reminders", "Připomínky", NotificationManager.IMPORTANCE_HIGH));
                PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                var notification = new Notification.Builder(c, "fabric.reminders")
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle(notificationTitle).setContentText(event.title())
                        .setStyle(new Notification.BigTextStyle().bigText(event.title()))
                        .setContentIntent(open).setAutoCancel(true).build();
                manager.notify(id.hashCode(), notification);
                return;
            }
        } catch (Exception e) {
            Log.e("Fabric.Notifications", "Showing notification failed", e);
        }
    }
}
