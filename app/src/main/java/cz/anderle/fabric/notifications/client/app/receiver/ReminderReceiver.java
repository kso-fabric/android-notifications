package cz.anderle.fabric.notifications.client.app.receiver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalField;
import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import cz.anderle.fabric.notifications.client.app.MainActivity;
import cz.anderle.fabric.notifications.client.app.Store;
import cz.anderle.fabric.notifications.client.app.config.OffsetConfig;
import cz.anderle.fabric.notifications.client.app.model.Event;

/**
 * Je volán pri alarm eventu. Zobrazí notifikaci.
 */
public class ReminderReceiver extends BroadcastReceiver {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter
            .ofPattern("HH:mm")
            .withZone(ZoneId.systemDefault());

    @Override
    public void onReceive(Context c, Intent intent) {
        if (intent.getData() == null)
            return;

        UUID id = UUID.fromString(intent.getData().getLastPathSegment());
        String offsetIndex = intent.getData().getQueryParameter("offset");
        OffsetConfig.Offset offset = OffsetConfig.findOffset(offsetIndex);

        try {
            var events = Store.read(c);
            for (var event : events) {
                if (!event.id().equals(id)) {
                    continue;
                }

                NotificationManager manager = c.getSystemService(NotificationManager.class);
                manager.createNotificationChannel(new NotificationChannel("fabric.reminders", "Připomínky", NotificationManager.IMPORTANCE_HIGH));
                PendingIntent open = PendingIntent.getActivity(c, 0, new Intent(c, MainActivity.class), PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                var date = Instant.ofEpochMilli(event.time());
                var notification = new Notification.Builder(c, "fabric.reminders")
                        .setSmallIcon(offset.icon())
                        .setContentTitle(offset.description() + " v " + TIME_FORMATTER.format(date))
                        .setContentText(event.title())
                        .setStyle(new Notification.BigTextStyle().bigText(event.title()))
                        .setContentIntent(open)
                        .setAutoCancel(true)
                        .build();
                manager.notify(id.hashCode(), notification);
                return;
            }
        } catch (Exception e) {
            Log.e("Fabric.Notifications", "Showing notification failed", e);
        }
    }
}
